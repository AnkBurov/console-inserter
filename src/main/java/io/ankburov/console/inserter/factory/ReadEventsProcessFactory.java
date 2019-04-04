package io.ankburov.console.inserter.factory;

import io.ankburov.console.inserter.config.ApplicationProperties;
import io.ankburov.console.inserter.persistence.dao.EventDao;
import io.ankburov.console.inserter.process.ReadEventsProcess;

public class ReadEventsProcessFactory extends AbstractApplicationProcessFactory<ReadEventsProcess> {

    @Override
    public ReadEventsProcess build(ApplicationProperties properties) {
        EventDao taskDao = buildTaskDao(properties);

        return new ReadEventsProcess(taskDao);
    }
}
