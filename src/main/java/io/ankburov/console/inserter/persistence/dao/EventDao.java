package io.ankburov.console.inserter.persistence.dao;

import io.ankburov.console.inserter.model.Event;

import java.util.List;

public interface EventDao {

    /**
     * Insert a task into the database in idempotent way
     */
    Event insert(Event task);

    List<Event> findAll();
}
