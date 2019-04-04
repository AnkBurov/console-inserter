package io.ankburov.console.inserter.config;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApplicationProperties {

    private String jdbcUrl;

    private String username;

    private String password;

    private int creationRate;

    private int waitOnFail;

    private int insertTimeout;
}
