package io.ankburov.console.inserter.testcontainers;

import org.testcontainers.containers.MySQLContainer;

public class FixedPortSqlContainer<SELF extends MySQLContainer<SELF>> extends MySQLContainer<SELF> {

    public FixedPortSqlContainer() {
        super();
    }

    public FixedPortSqlContainer(String dockerImageName) {
        super(dockerImageName);
    }

    public SELF withFixedExposedPort(int hostPort, int containerPort) {
        super.addFixedExposedPort(hostPort, containerPort);
        return self();
    }
}
