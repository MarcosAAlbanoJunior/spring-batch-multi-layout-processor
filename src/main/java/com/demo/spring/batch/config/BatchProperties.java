package com.demo.spring.batch.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "batch.logistica")
public class BatchProperties {
    private int chunkSize;
    private int skipLimit;
    private Arquivo arquivo = new Arquivo();

    @Data
    public static class Arquivo {
        private String correios;
        private String jadlog;
        private String totalExpress;
    }
}