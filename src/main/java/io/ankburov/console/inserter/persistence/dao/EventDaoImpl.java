package io.ankburov.console.inserter.persistence.dao;

import io.ankburov.console.inserter.model.Event;
import lombok.RequiredArgsConstructor;
import org.jdbi.v3.core.Jdbi;

import java.util.List;

@RequiredArgsConstructor
public class EventDaoImpl implements EventDao {

    private final Jdbi jdbi;

    @Override
    public Event insert(Event task) {
        return jdbi.withHandle(handle -> {
            handle.createUpdate("INSERT INTO Event(time) VALUES (:time) ON DUPLICATE KEY UPDATE time=time")
                  .bind("time", task.getTime())
                  .execute();
            return task;
        });
    }

    @Override
    public List<Event> findAll() {
        return jdbi.withHandle(handle -> handle.createQuery("SELECT * FROM Event")
                                               .mapToBean(Event.class)
                                               .list());
    }
}
