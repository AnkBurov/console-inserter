package io.ankburov.console.inserter.process;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.ankburov.console.inserter.config.ApplicationProperties;
import io.ankburov.console.inserter.service.EventTaskConsumer;
import io.ankburov.console.inserter.service.EventTaskProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Slf4j
@RequiredArgsConstructor
public class GenerateEventsProcess implements ApplicationProcess {

    private final EventTaskProducer taskProducer;

    private final EventTaskConsumer taskConsumer;

    /**
     * Start event consumer thread and block on event producing
     */
    @Override
    public void execute(ApplicationProperties properties) {
        // run a task consumer thread
        ExecutorService taskConsumerExecutor = null;
        try {
            ThreadFactory taskConsumerThreadFactory = new ThreadFactoryBuilder().setNameFormat("task-consumer-%d").build();
            taskConsumerExecutor = Executors.newSingleThreadExecutor(taskConsumerThreadFactory);
            taskConsumerExecutor.submit(taskConsumer::consumeEvents);

            // block on task creation
            taskProducer.produceEvents();
        } finally {
            if (taskConsumerExecutor != null) {
                taskConsumerExecutor.shutdownNow();
            }
        }
    }
}
