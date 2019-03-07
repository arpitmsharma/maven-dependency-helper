package com.github.arpitmsharma.mdh.config;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    private static final String ROOT_URI = "https://search.maven.org";

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder()
                .messageConverters(mappingJackson2HttpMessageConverter())
                .rootUri(ROOT_URI).build();
    }

    @Bean
    MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(jacksonObjectMapper());

        return converter;
    }

    @Bean
    ObjectMapper jacksonObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return mapper;
    }
}
