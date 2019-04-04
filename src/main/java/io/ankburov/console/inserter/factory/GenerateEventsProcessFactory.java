package io.ankburov.console.inserter.factory;

import io.ankburov.console.inserter.config.ApplicationProperties;
import io.ankburov.console.inserter.model.Event;
import io.ankburov.console.inserter.persistence.dao.EventDao;
import io.ankburov.console.inserter.process.GenerateEventsProcess;
import io.ankburov.console.inserter.service.EventTaskConsumer;
import io.ankburov.console.inserter.service.EventTaskProducer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class GenerateEventsProcessFactory extends AbstractApplicationProcessFactory<GenerateEventsProcess> {

    @Override
    public GenerateEventsProcess build(ApplicationProperties properties) {
        BlockingQueue<Event> queue = new LinkedBlockingQueue<>();
        EventTaskProducer taskProducer = new EventTaskProducer(queue, properties.getCreationRate());

        EventDao taskDao = buildTaskDao(properties);
        EventTaskConsumer taskConsumer = new EventTaskConsumer(queue, taskDao, properties.getWaitOnFail(), properties.getInsertTimeout());

        return new GenerateEventsProcess(taskProducer, taskConsumer);
    }
}
