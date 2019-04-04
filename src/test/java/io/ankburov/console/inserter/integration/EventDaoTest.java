package io.ankburov.console.inserter.integration;

import io.ankburov.console.inserter.model.Event;
import io.ankburov.console.inserter.persistence.dao.EventDao;
import io.ankburov.console.inserter.persistence.dao.EventDaoImpl;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

import static io.ankburov.console.inserter.utils.TestUtils.getAllEventTimestamps;
import static org.junit.Assert.assertEquals;

public class EventDaoTest extends AbstractIntegrationTest {

    @Test
    public void testDuplicateInsert() {
        EventDao eventDao = new EventDaoImpl(jdbi);
        Event event = new Event(new Timestamp(System.currentTimeMillis()));

        eventDao.insert(event);
        eventDao.insert(event);
        eventDao.insert(event);

        List<Timestamp> readTimestamps = getAllEventTimestamps(jdbi).collect(Collectors.toList());

        assertEquals(1, readTimestamps.size());
    }
}
