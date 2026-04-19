package com.github.eirikma.appstate.test;

import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Base class for integration tests providing a PostgreSQL testcontainer and Liquibase schema setup.
 */
public abstract class IntegrationTestBase {

    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17")
                    .withDatabaseName("appstate_test")
                    .withUsername("test")
                    .withPassword("test");

    private static DataSource dataSource;

    @BeforeAll
    static void startContainer() throws Exception {
        if (!POSTGRES.isRunning()) {
            POSTGRES.start();
        }
        dataSource = createDataSource();
        runLiquibase();
    }

    protected static DataSource dataSource() {
        return dataSource;
    }

    private static DataSource createDataSource() {
        org.postgresql.ds.PGSimpleDataSource ds = new org.postgresql.ds.PGSimpleDataSource();
        ds.setUrl(POSTGRES.getJdbcUrl());
        ds.setUser(POSTGRES.getUsername());
        ds.setPassword(POSTGRES.getPassword());
        return ds;
    }

    private static void runLiquibase() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            liquibase.database.Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            try (Liquibase liquibase = new Liquibase(
                    "db/changelog/db.changelog-master.xml",
                    new ClassLoaderResourceAccessor(),
                    database)) {
                liquibase.update("");
            }
        }
    }
}
