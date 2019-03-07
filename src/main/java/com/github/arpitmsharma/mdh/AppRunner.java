package com.github.arpitmsharma.mdh;

import com.github.arpitmsharma.mdh.model.ArtifactMetaData;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
public class AppRunner implements ApplicationRunner {
    @Autowired
    private RestTemplate restTemplate;

    private static Map<String, String> artifactLatestVersionMap = new HashMap<>();

    private static Map<String, String> artifactCurrentVersionMap = new HashMap<>();

    @Override
    public void run(ApplicationArguments args) throws Exception {
        checkForUpdatesInPom("/home/arpit/Documents/repos/teckportal-server");
    }

    private void checkForUpdatesInPom(String path) {
        System.out.println("Reading POM from path: " + path);
        Path pomPath = Paths.get(path + "/pom.xml");

        try (BufferedReader reader = Files.newBufferedReader(pomPath, Charset.forName("UTF-8"))) {
            MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
            Model model = xpp3Reader.read(reader);

            Parent parent = model.getParent();
            printArtifactDetails("Parent", parent.getGroupId(), parent.getArtifactId(), parent.getVersion(), checkForVersionUpdate(parent.getGroupId(), parent.getArtifactId()));

            Properties props = model.getProperties();
            for (Dependency dependency : model.getDependencies()) {
                String artifactId = getArtifactId(dependency.getGroupId(),dependency.getArtifactId());
                if(isNull(dependency.getVersion()) && isNull(artifactCurrentVersionMap.get(artifactId))){
                    artifactCurrentVersionMap.put(artifactId, parent.getVersion());
                }else if(dependency.getVersion().matches("(\\$\\{.*\\})")){
                    String dependencyVersion = dependency.getVersion();
                    dependencyVersion = dependencyVersion.substring(2, dependencyVersion.length()-1);
                    dependencyVersion = props.getProperty(dependencyVersion);
                    artifactCurrentVersionMap.put(artifactId, dependencyVersion);
                }
                printArtifactDetails("Dependency:", dependency.getGroupId(), dependency.getArtifactId(), artifactCurrentVersionMap.get(artifactId), checkForVersionUpdate(dependency.getGroupId(), dependency.getArtifactId()));
            }

            for (String moduleName : model.getModules()) {
                checkForUpdatesInPom(path + "/" + moduleName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String checkForVersionUpdate(final String groupId, final String artifactId) {
        String id = getArtifactId(groupId, artifactId);

        if (nonNull(artifactLatestVersionMap.get(id))) {
            return artifactLatestVersionMap.get(id);
        }

        ResponseEntity<ArtifactMetaData> responseEntity = restTemplate.getForEntity("/solrsearch/select?q=id:\"" + id + "\"", ArtifactMetaData.class);
        ArtifactMetaData artifact =  responseEntity.getBody();
        if(artifact.getResponse().getDocs().size()> 0){
            return artifact.getResponse().getDocs().get(0).getLatestVersion();
        }else {
            return "Repo unavailable on Maven";
        }
    }

    private void printArtifactDetails(String type, String groupId, String artifactId, String currentVersion, String latestVersion) {
        System.out.println(type + "  " + groupId + "  " + artifactId + "  " + currentVersion + "  " + latestVersion);
    }

    private String getArtifactId(final String groupId, final String artifactId) {
        return groupId + ":" + artifactId;
    }
}
