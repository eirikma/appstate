package com.github.eirikma.appstate.calendar.repository;

import com.github.eirikma.appstate.calendar.model.CalendarDate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

/**
 * JDBC-based implementation of CalendarRepository using plain SQL against PostgreSQL.
 */
public class JdbcCalendarRepository implements CalendarRepository {

    private final DataSource dataSource;

    public JdbcCalendarRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void deleteMonth(YearMonth yearMonth) {
        String yearMonthStr = yearMonth.toString();
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM calendar_date_tag WHERE date IN (SELECT date FROM calendar_date WHERE year_month = ?)")) {
                ps.setString(1, yearMonthStr);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM calendar_date WHERE year_month = ?")) {
                ps.setString(1, yearMonthStr);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete month " + yearMonth, e);
        }
    }

    @Override
    public void saveDates(List<LocalDate> dates, YearMonth yearMonth) {
        String yearMonthStr = yearMonth.toString();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "INSERT INTO calendar_date (date, year_month) VALUES (?, ?)")) {
            for (LocalDate date : dates) {
                ps.setObject(1, date);
                ps.setString(2, yearMonthStr);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save dates for " + yearMonth, e);
        }
    }

    @Override
    public void saveTags(Map<LocalDate, Set<String>> tagsByDate, String tagSet) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "INSERT INTO calendar_date_tag (date, tag_set, tag) VALUES (?, ?, ?)")) {
            for (Map.Entry<LocalDate, Set<String>> entry : tagsByDate.entrySet()) {
                LocalDate date = entry.getKey();
                for (String tag : entry.getValue()) {
                    ps.setObject(1, date);
                    ps.setString(2, tagSet);
                    ps.setString(3, tag);
                    ps.addBatch();
                }
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save tags for tag-set " + tagSet, e);
        }
    }

    @Override
    public List<CalendarDate> findDatesByRange(LocalDate from, LocalDate to) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT cd.date, cdt.tag_set, cdt.tag " +
                     "FROM calendar_date cd " +
                     "LEFT JOIN calendar_date_tag cdt ON cd.date = cdt.date " +
                     "WHERE cd.date >= ? AND cd.date <= ? " +
                     "ORDER BY cd.date, cdt.tag_set, cdt.tag")) {
            ps.setObject(1, from);
            ps.setObject(2, to);
            return collectCalendarDates(ps.executeQuery());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query dates in range", e);
        }
    }

    @Override
    public List<CalendarDate> findDatesByTagSetInRange(String tagSet, LocalDate from, LocalDate to) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT cd.date, cdt.tag_set, cdt.tag " +
                     "FROM calendar_date cd " +
                     "JOIN calendar_date_tag cdt ON cd.date = cdt.date " +
                     "WHERE cdt.tag_set = ? AND cd.date >= ? AND cd.date <= ? " +
                     "ORDER BY cd.date, cdt.tag")) {
            ps.setString(1, tagSet);
            ps.setObject(2, from);
            ps.setObject(3, to);
            return collectCalendarDates(ps.executeQuery());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query dates by tag-set in range", e);
        }
    }

    private static List<CalendarDate> collectCalendarDates(ResultSet rs) throws SQLException {
        Map<LocalDate, Map<String, Set<String>>> dateMap = new LinkedHashMap<>();
        while (rs.next()) {
            LocalDate date = rs.getObject("date", LocalDate.class);
            String tagSet = rs.getString("tag_set");
            String tag = rs.getString("tag");
            Map<String, Set<String>> tagsByTagSet = dateMap.computeIfAbsent(date, k -> new LinkedHashMap<>());
            if (tagSet != null && tag != null) {
                tagsByTagSet.computeIfAbsent(tagSet, k -> new LinkedHashSet<>()).add(tag);
            }
        }
        return dateMap.entrySet().stream()
                .map(entry -> new CalendarDate(entry.getKey(), Map.copyOf(entry.getValue())))
                .toList();
    }
}
