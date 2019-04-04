package io.ankburov.console.inserter.service;

import io.ankburov.console.inserter.model.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Stream;

/**
 * Event generator
 */
@Slf4j
@RequiredArgsConstructor
public class EventTaskProducer {

    private final BlockingQueue<Event> queue;

    private final Integer creationRate;

    /**
     * Blockingly produce events into the queue with specified rate
     */
    public void produceEvents() {
        Stream<Timestamp> timeStream = Stream.generate(() -> new Timestamp(System.currentTimeMillis()));

        Flux.fromStream(timeStream)
            .delayElements(Duration.ofSeconds(creationRate))
            .doOnNext(this::addTask)
            .onErrorStop()
            .onErrorResume(throwable -> {
                log.error("Exception happened on generation create events", throwable);
                System.err.println("Could not generate a create event task");
                return Mono.just(new Timestamp(System.currentTimeMillis()));
            })
            .blockLast();
    }

    /**
     * Add task into the queue
     *
     * @throws IllegalStateException if the queue is full
     */
    private void addTask(Timestamp timestamp) {
        Event task = new Event(timestamp);

        queue.add(task);
    }
}
