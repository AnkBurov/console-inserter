package io.ankburov.console.inserter.utils;

import io.ankburov.console.inserter.model.Event;
import org.jdbi.v3.core.Jdbi;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class TestUtils {

    public static Stream<Timestamp> getAllEventTimestamps(Jdbi jdbi) {
        return jdbi.withHandle(handle -> handle.createQuery("select * from Event")
                                               .mapToBean(Event.class)
                                               .list())
                   .stream()
                   .map(Event::getTime);
    }

    public static long getAverageTimeDifference(List<Timestamp> timestamps) {
        List<Long> timeDifferences = getTimeDifferences(timestamps);

        long averageTimeDifference = timeDifferences.stream()
                .mapToLong(it -> it)
                .sum() / timeDifferences.size();
        return TimeUnit.SECONDS.convert(averageTimeDifference, TimeUnit.MILLISECONDS);
    }

    private static List<Long> getTimeDifferences(List<Timestamp> timestamps) {
        List<Long> differences = new ArrayList<>();

        long previousTime = -1;
        for (Timestamp timestamp : timestamps) {
            if (previousTime != -1) {
                differences.add(timestamp.getTime() - previousTime);
            }
            previousTime = timestamp.getTime();
        }
        return differences;
    }
}
