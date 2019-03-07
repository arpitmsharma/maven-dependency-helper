package com.github.arpitmsharma.mdh.model;

import lombok.Data;

@Data
public class Doc {
    private String id;
    private String g;
    private String a;
    private String v;
    private String p;
    private Integer versionCount;
    private String latestVersion;
    private String repositoryId;
    private Long timestamp;
}
