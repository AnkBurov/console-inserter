package io.ankburov.console.inserter;

import io.ankburov.console.inserter.config.ApplicationProperties;
import io.ankburov.console.inserter.factory.GenerateEventsProcessFactory;
import io.ankburov.console.inserter.factory.ReadEventsProcessFactory;
import io.ankburov.console.inserter.process.ApplicationProcess;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Starter {

    private static final String SHOW_DATABASE_OPTION = "p";
    private static final String JDBC_URL_OPTION = "jdbc";
    private static final String CREATION_RATE_OPTION = "rate";
    private static final String WAIT_ON_FAIL_OPTION = "waitOnFail";
    private static final String USERNAME_OPTION = "username";
    private static final String PASSWORD_OPTION = "password";
    private static final String INSERT_TIMEOUT_OPTION = "insertTimeout";
    private static final String DEFAULT_JDBC_URL = "jdbc:mysql://localhost:3306/test";

    public static void main(String[] args) {
        Options options = new Options();

        options.addOption(createAnOption(SHOW_DATABASE_OPTION, false, "show database content", false));
        options.addOption(createAnOption(JDBC_URL_OPTION, true, "specify JDBC url for a database", false));
        options.addOption(createAnOption(CREATION_RATE_OPTION, true, "how often create events in seconds", false));
        options.addOption(createAnOption(WAIT_ON_FAIL_OPTION, true, "how many seconds need to wait if previous db insert has failed", false));
        options.addOption(createAnOption(USERNAME_OPTION, true, "database username", false));
        options.addOption(createAnOption(PASSWORD_OPTION, true, "database password", false));
        options.addOption(createAnOption(INSERT_TIMEOUT_OPTION, true, "database insert timeout in seconds", false));

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        try {
            CommandLine cmd = parser.parse(options, args);

            String jdbcUrl = cmd.getOptionValue(JDBC_URL_OPTION, DEFAULT_JDBC_URL);
            String username = cmd.getOptionValue(USERNAME_OPTION);
            String password = cmd.getOptionValue(PASSWORD_OPTION);
            int creationRate = Integer.parseInt(cmd.getOptionValue(CREATION_RATE_OPTION, "1"));
            int waitOnFail = Integer.parseInt(cmd.getOptionValue(WAIT_ON_FAIL_OPTION, "5"));
            int insertTimeout = Integer.parseInt(cmd.getOptionValue(INSERT_TIMEOUT_OPTION, "1"));

            if (DEFAULT_JDBC_URL.equals(jdbcUrl)) {
                String warnMessage = String.format("Arg -%s is not set, default jdbc url is used. " +
                                                           "Consider setting JDBC-related arguments to use specific database", JDBC_URL_OPTION);
                System.out.println(warnMessage);
            }

            ApplicationProperties properties = ApplicationProperties.builder()
                                                                    .jdbcUrl(jdbcUrl)
                                                                    .username(username)
                                                                    .password(password)
                                                                    .creationRate(creationRate)
                                                                    .waitOnFail(waitOnFail)
                                                                    .insertTimeout(insertTimeout)
                                                                    .build();

            ApplicationProcess applicationProcess;
            if (cmd.hasOption(SHOW_DATABASE_OPTION)) {
                applicationProcess = new ReadEventsProcessFactory().build(properties);
            } else {
                applicationProcess = new GenerateEventsProcessFactory().build(properties);
            }
            applicationProcess.execute(properties);

        } catch (ParseException | NumberFormatException e) {
            System.err.println(e.getMessage());
            formatter.printHelp("java -jar console-inserter.jar", options);

            System.exit(1);
        }
    }

    private static Option createAnOption(String opt, boolean hasArg, String description, boolean required) {
        Option option = new Option(opt, hasArg, description);
        option.setRequired(required);
        return option;
    }
}
