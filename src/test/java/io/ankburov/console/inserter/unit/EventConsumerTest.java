package io.ankburov.console.inserter.unit;

import io.ankburov.console.inserter.base.AbstractAsynchronousTest;
import io.ankburov.console.inserter.model.Event;
import io.ankburov.console.inserter.persistence.dao.EventDao;
import io.ankburov.console.inserter.service.EventTaskConsumer;
import io.ankburov.console.inserter.utils.ServiceUtils;
import lombok.val;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.Timestamp;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EventConsumerTest extends AbstractAsynchronousTest {

    /**
     * Test that events are consumed from the queue
     */
    @Test
    public void testConsumeEvents() throws InterruptedException {
        val queue = new LinkedBlockingQueue<Event>();
        int waitOnFailSeconds = 3;
        Event task = new Event(new Timestamp(System.currentTimeMillis()));

        EventDao mockDao = Mockito.mock(EventDao.class);
        Mockito.when(mockDao.insert(Mockito.eq(task))).thenReturn(task);

        queue.add(task);

        EventTaskConsumer taskConsumer = new EventTaskConsumer(queue, mockDao, waitOnFailSeconds, 1);

        executor.submit(taskConsumer::consumeEvents);

        TimeUnit.SECONDS.sleep(1);

        Mockito.verify(mockDao, Mockito.times(1)).insert(Mockito.eq(task));
        assertFalse(queue.contains(task));
    }

    /**
     * Test that consumer doesn't lost events when dao fails
     */
    @Test
    public void testConsumeEventsWithFailingDao() throws InterruptedException {
        val queue = new LinkedBlockingQueue<Event>();
        int waitOnFailSeconds = 1;
        Event task = new Event(new Timestamp(System.currentTimeMillis()));

        EventDao mockDao = Mockito.mock(EventDao.class);
        Mockito.when(mockDao.insert(Mockito.any())).thenThrow(new RuntimeException("Expected"));

        queue.add(task);

        EventTaskConsumer taskConsumer = new EventTaskConsumer(queue, mockDao, waitOnFailSeconds, 1);

        executor.submit(taskConsumer::consumeEvents);

        TimeUnit.SECONDS.sleep(waitOnFailSeconds * 3);

        assertTrue(queue.contains(task));
        Mockito.verify(mockDao, Mockito.atLeast(waitOnFailSeconds * 2)).insert(Mockito.eq(task));
    }

    @Test
    public void testSlowDatabase() throws InterruptedException {
        val queue = new LinkedBlockingQueue<Event>();
        int waitOnFailSeconds = 1;
        Event task = new Event(new Timestamp(System.currentTimeMillis()));

        EventDao mockDao = Mockito.mock(EventDao.class);
        Mockito.when(mockDao.insert(Mockito.any())).thenAnswer(invocation -> {
            ServiceUtils.sleepSafely(1000);
            return 0;
        });

        queue.add(task);

        EventTaskConsumer taskConsumer = new EventTaskConsumer(queue, mockDao, waitOnFailSeconds, 1);

        executor.submit(taskConsumer::consumeEvents);

        TimeUnit.SECONDS.sleep(waitOnFailSeconds * 3);

        assertTrue(queue.contains(task));

        Mockito.reset(mockDao);
        Mockito.when(mockDao.insert(Mockito.eq(task))).thenReturn(task);
        TimeUnit.SECONDS.sleep(waitOnFailSeconds * 2);

        assertFalse(queue.contains(task));
    }
}
