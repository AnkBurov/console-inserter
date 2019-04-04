package io.ankburov.console.inserter.process;

import io.ankburov.console.inserter.config.ApplicationProperties;
import io.ankburov.console.inserter.model.Event;
import io.ankburov.console.inserter.persistence.dao.EventDao;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ReadEventsProcess implements ApplicationProcess {

    private final EventDao taskDao;

    @Override
    public void execute(ApplicationProperties properties) {
        List<String> timestamps = findAllEventTimestamps();

        System.out.println("Database content:\n" + String.join("\n", timestamps));
    }

    public List<String> findAllEventTimestamps() {
        return taskDao.findAll()
                .stream()
                .map(Event::getTime)
                .map(Timestamp::toString)
                .collect(Collectors.toList());
    }
}
