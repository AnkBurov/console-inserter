package io.ankburov.console.inserter.factory;

import io.ankburov.console.inserter.config.ApplicationProperties;
import io.ankburov.console.inserter.process.ApplicationProcess;

public interface ApplicationProcessFactory<T extends ApplicationProcess> {

    T build(ApplicationProperties properties);
}
