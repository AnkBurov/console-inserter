package io.ankburov.console.inserter.factory;

import io.ankburov.console.inserter.config.ApplicationProperties;
import io.ankburov.console.inserter.persistence.dao.EventDao;
import io.ankburov.console.inserter.persistence.dao.EventDaoImpl;
import io.ankburov.console.inserter.process.ApplicationProcess;
import org.jdbi.v3.core.Jdbi;

import java.util.Optional;

public abstract class AbstractApplicationProcessFactory<T extends ApplicationProcess> implements ApplicationProcessFactory<T> {

    protected EventDao buildTaskDao(ApplicationProperties properties) {
        Jdbi jdbi;
        if (properties.getUsername() != null) {
            jdbi = Jdbi.create(properties.getJdbcUrl(), properties.getUsername(), Optional.ofNullable(properties.getPassword()).orElse(""));
        } else {
            jdbi = Jdbi.create(properties.getJdbcUrl());
        }

        return new EventDaoImpl(jdbi);
    }
}
