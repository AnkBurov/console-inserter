package io.ankburov.console.inserter.utils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.util.concurrent.TimeUnit;

@UtilityClass
public class ServiceUtils {

    @SneakyThrows
    public void sleepSafely(int seconds) {
        TimeUnit.SECONDS.sleep(seconds);
    }
}
