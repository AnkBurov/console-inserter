package io.ankburov.console.inserter.integration;

import com.github.dockerjava.api.DockerClient;
import io.ankburov.console.inserter.Starter;
import io.ankburov.console.inserter.persistence.dao.EventDaoImpl;
import io.ankburov.console.inserter.process.ReadEventsProcess;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.testcontainers.DockerClientFactory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.ankburov.console.inserter.utils.TestUtils.getAllEventTimestamps;
import static io.ankburov.console.inserter.utils.TestUtils.getAverageTimeDifference;
import static org.junit.Assert.*;

@Slf4j
public class IntegrationTest extends AbstractIntegrationTest {

    private static Long creationRate = 1L;

    /**
     * Test that events are creating and written into the database in expected way
     */
    @Test
    public void testCreateEvents() throws InterruptedException {
        executor.submit(() -> {
            Starter.main(new String[]{"-jdbc", jdbcUrl, "-username", username, "-password", password, "-rate", creationRate.toString()});
        });

        TimeUnit.SECONDS.sleep(7);

        List<Timestamp> readTimestamps = getAllEventTimestamps(jdbi).collect(Collectors.toList());

        assertTrue(readTimestamps.size() > 2);

        // make sure that the order is correct
        List<Timestamp> sortedTimestamps = new ArrayList<>(readTimestamps);
        sortedTimestamps.sort(Comparator.comparing(Timestamp::getTime));
        assertEquals(sortedTimestamps, readTimestamps);

        // make sure that comparing lists are different objects
        assertNotEquals(System.identityHashCode(sortedTimestamps), System.identityHashCode(readTimestamps));

        // make sure that creation rate is expected
        Long averageTimeDifference = getAverageTimeDifference(readTimestamps);
        assertEquals(creationRate, averageTimeDifference);
    }

    /**
     * Create events and read them via application API
     */
    @Test
    public void testReadEvents() throws InterruptedException {
        // create some tasks
        Future<?> future = executor.submit(() -> {
            Starter.main(new String[]{"-jdbc", jdbcUrl, "-username", username, "-password", password});
        });
        TimeUnit.SECONDS.sleep(5);
        future.cancel(true);

        // read tasks
        Starter.main(new String[]{"-p", "-jdbc", jdbcUrl, "-username", username, "-password", password});

        // assert content of shown events
        ReadEventsProcess readEventsProcess = new ReadEventsProcess(new EventDaoImpl(jdbi));

        List<String> timestamps = readEventsProcess.findAllEventTimestamps();

        assertTrue(timestamps.size() > 2);

        List<String> databaseTimestamps = getAllEventTimestamps(jdbi)
                .map(Timestamp::toString)
                .collect(Collectors.toList());
        assertEquals(databaseTimestamps, timestamps);
    }

    /**
     * Start events creation, then stop the database, wait a bit and start it again and make sure that
     * previously not written into the database events have been successfully written into the database in expected way
     */
    @Test
    public void testFailingDatabase() throws InterruptedException {
        long waitOnFail = 2L;
        executor.submit(() -> {
            Starter.main(new String[]{"-jdbc", jdbcUrl, "-username", username, "-password", password,
                    "-rate", creationRate.toString(), "-waitOnFail", Long.toString(waitOnFail)});
        });
        TimeUnit.SECONDS.sleep(2);

        DockerClient client = DockerClientFactory.instance().client();
        try {
            List<Timestamp> readTimestampsBeforeStop = getAllEventTimestamps(jdbi).collect(Collectors.toList());

            // stop the database
            client.stopContainerCmd(database.getContainerId()).exec();
            TimeUnit.SECONDS.sleep(2);

            // start the database
            client.startContainerCmd(database.getContainerId()).exec();
            TimeUnit.SECONDS.sleep(waitOnFail * 4);

            // assert that tasks are still inserting into the database
            List<Timestamp> readTimestamps = getAllEventTimestamps(jdbi).collect(Collectors.toList());

            assertTrue(readTimestamps.size() > 4);

            assertTrue(readTimestamps.size() > readTimestampsBeforeStop.size());
            assertTrue(readTimestamps.get(readTimestamps.size() - 1).after(readTimestampsBeforeStop.get(readTimestampsBeforeStop.size() - 1)));

            // assert that there were no gaps in events order
            Long averageTimeDifference = getAverageTimeDifference(readTimestamps);
            assertEquals(creationRate, averageTimeDifference);
        } finally {
            if (!database.isRunning()) {
                client.startContainerCmd(database.getContainerId()).exec();
            }
        }
    }
}
