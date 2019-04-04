package io.ankburov.console.inserter.unit;

import io.ankburov.console.inserter.base.AbstractAsynchronousTest;
import io.ankburov.console.inserter.model.Event;
import io.ankburov.console.inserter.service.EventTaskProducer;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static io.ankburov.console.inserter.utils.TestUtils.getAverageTimeDifference;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EventProducerTest extends AbstractAsynchronousTest {

    @Test
    public void testProduceEvents() throws InterruptedException {
        int creationRate = 1;
        int sleepingTime = creationRate * 5;

        BlockingQueue<Event> queue = new LinkedBlockingQueue<>();
        EventTaskProducer taskProducer = new EventTaskProducer(queue, creationRate);

        // run a generator
        Future<?> future = executor.submit(taskProducer::produceEvents);

        TimeUnit.SECONDS.sleep(sleepingTime);
        future.cancel(true);

        assertTrue(queue.size() >= sleepingTime - 1);

        // make sure that creation rate is expected
        List<Timestamp> readTimestamps = queue.stream()
                .map(Event::getTime)
                .collect(Collectors.toList());

        Long averageTimeDifference = getAverageTimeDifference(readTimestamps);
        assertEquals(creationRate, averageTimeDifference.intValue());
    }
}
