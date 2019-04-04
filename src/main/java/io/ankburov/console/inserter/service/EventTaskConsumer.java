package io.ankburov.console.inserter.service;

import io.ankburov.console.inserter.model.Event;
import io.ankburov.console.inserter.persistence.dao.EventDao;
import io.ankburov.console.inserter.utils.ServiceUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class EventTaskConsumer {

    private final BlockingQueue<Event> queue;

    private final EventDao taskDao;

    private final int waitOnFail;

    private final int insertTimeout;

    /**
     * Consume events from the queue and insert each into the database
     * <p>
     * If insert fails, then wait a specified time interval and try again
     * If insert is successful, then remove the event from the queue
     */
    @SneakyThrows
    public void consumeEvents() {
        while (true) {
            if (queue.size() == 0) {
                TimeUnit.MILLISECONDS.sleep(100);
                continue;
            }
            Event takenTask = queue.peek();
            if (takenTask == null) {
                continue;
            }

            Mono.just(takenTask)
                .publishOn(Schedulers.elastic())
                .map(taskDao::insert)
                .timeout(Duration.ofSeconds(insertTimeout))
                .doOnNext(insertedTask -> System.out.println(String.format("Inserted a task into db: %s", insertedTask.getTime())))
                .map(queue::remove)
                .doOnError(throwable -> {
                    log.error("Failed to insert a task {} into the database", takenTask.getTime(), throwable);
                    System.err.println(String.format("Failed to insert a task %s into the database, waiting %s seconds",
                                                     takenTask.getTime(), waitOnFail));
                    ServiceUtils.sleepSafely(waitOnFail);
                })
                .onErrorReturn(false)
                .block();
        }
    }
}
