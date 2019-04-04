# Console inserter

A console-based application that constantly inserts timestamps into a Mysql database.

### Test

Execute `./gradlew test`

Integration tests require installed Docker client.

### Build

Execute `./gradlew test clean build jar -x test`

### Run

To insert timestamps into the database execute:

`java -jar build/libs/console-inserter.jar`

Note that by default an application uses default JDBC-url such as `jdbc:mysql://localhost:3306/test`
so you would rather want to specify JDBC-url and optionally login and password. Like this:

`java -jar build/libs/console-inserter.jar -jdbc jdbc:mysql://localhost:10536/test -username test -password test`

To read written content from the database, run the application with `-p` argument. Specify database credentials if needed:

`java -jar build/libs/console-inserter.jar -jdbc jdbc:mysql://localhost:10536/test -username test -password test -p`

Note that application doesn't do DDL-operations to create needed tables because any good application that works with
databases must work under DML-only user rights. DDL operations must be executed only by humans or under specialized 
libraries such as FlyWay, Liquibase, thousands of them, within CI/CD pipelines.

Required DDL for the application:
```mysql
CREATE TABLE Event (
  time TIMESTAMP,
  PRIMARY KEY (time)
);
```