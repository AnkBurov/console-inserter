package io.ankburov.console.inserter.integration;

import io.ankburov.console.inserter.base.AbstractAsynchronousTest;
import io.ankburov.console.inserter.testcontainers.FixedPortSqlContainer;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.junit.Before;
import org.junit.ClassRule;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;

@Slf4j
public class AbstractIntegrationTest extends AbstractAsynchronousTest {
    @ClassRule
    public static MySQLContainer database = new FixedPortSqlContainer<>()
            .withFixedExposedPort(10536, 3306)
            .waitingFor(new HostPortWaitStrategy())
            .withInitScript("script/initscript.sql")
            .withLogConsumer(new Slf4jLogConsumer(log));

    protected static String jdbcUrl;
    protected static String username;
    protected static String password;
    protected static Jdbi jdbi;

    @Before
    public void setUp() {
        jdbcUrl = database.getJdbcUrl();
        username = database.getUsername();
        password = database.getPassword();
        jdbi = Jdbi.create(jdbcUrl, username, password);

        jdbi.withHandle(handle -> handle.createUpdate("delete from Event").execute());
        super.setUp();
    }
}
