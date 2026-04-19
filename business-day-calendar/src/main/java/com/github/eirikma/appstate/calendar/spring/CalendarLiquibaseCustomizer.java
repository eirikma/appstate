package com.github.eirikma.appstate.calendar.spring;

import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Exports the business-day-calendar Liquibase changelog for schema management.
 */
@Configuration
@EnableConfigurationProperties(LiquibaseProperties.class)
public class CalendarLiquibaseCustomizer {

    public static final String CHANGELOG_PATH = "db/changelog/db.changelog-master.xml";

    @Bean
    public LiquibaseProperties calendarLiquibaseProperties() {
        LiquibaseProperties properties = new LiquibaseProperties();
        properties.setChangeLog("classpath:" + CHANGELOG_PATH);
        return properties;
    }
}
