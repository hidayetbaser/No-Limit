import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class MyJDBC {
    private static final DateTimeFormatter SQL_FORMAT = // For local date time
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter SQL_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");// For local date

    Connection connection;
    Statement statement;
    ResultSet resultSet;

    MyJDBC() throws SQLException {
        connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/myDB", "root", "22042005Hid.");
        statement = connection.createStatement();
    }

    void createStudyBlocksTable() throws SQLException {

        String sql = "CREATE TABLE IF NOT EXISTS StudyBlocks (" +
                "  start_time DATETIME NOT NULL," +
                "  finish_time DATETIME NOT NULL" +
                ")";

        statement.executeUpdate(sql); // will be 0 for DDL like CREATE TABLE
    }

    void createDaysTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS Days (" +
                "  day_date DATE PRIMARY KEY," +
                "  total_time TIME NOT NULL DEFAULT '00:00:00'," +
                "  goal_time  TIME NOT NULL DEFAULT '00:00:00'" +
                ")";

        statement.executeUpdate(sql);
    }

    public void addStudyBlock(LocalDateTime start, LocalDateTime end) throws SQLException {
        Timestamp startTs = Timestamp.valueOf(start);
        Timestamp endTs = Timestamp.valueOf(end);

        String sql = "INSERT INTO StudyBlocks (start_time, finish_time) VALUES ('"
                + startTs + "', '" + endTs + "')";

        statement.executeUpdate(sql);
    }

    void addToDay(LocalDate day, int hours, int minutes, int seconds) throws SQLException {
        String delta = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        // Ensure the row exists (so UPDATE always works)
        statement.executeUpdate("INSERT IGNORE INTO Days (day_date) VALUES ('" + day.format(SQL_DATE) + "')"); // create

        String sql = "UPDATE Days " +
                "SET total_time = ADDTIME(total_time, '" + delta + "') " +
                "WHERE day_date = '" + day.format(SQL_DATE) + "'";

        statement.executeUpdate(sql);

    }

    public ArrayList<StudyBlock> makeAListOfADaysStudyBlocks(LocalDate whichDay) throws SQLException {
        // start of the day
        LocalDateTime startOfDay = whichDay.atStartOfDay(); // yyyy-MM-dd 00:00:00
        // start of the next day
        LocalDateTime endOfDay = whichDay.plusDays(1).atStartOfDay(); // yyyy-MM-dd+1 00:00:00

        // Build SQL string
        String query = "SELECT start_time, finish_time " +
                "FROM StudyBlocks " +
                "WHERE start_time >= '" + startOfDay.format(SQL_FORMAT) + "' " +
                "AND start_time < '" + endOfDay.format(SQL_FORMAT) + "' " +
                "ORDER BY start_time ASC, finish_time ASC";// id word was causing problem

        ArrayList<StudyBlock> blocks = new ArrayList<>();
        // Execute
        resultSet = statement.executeQuery(query);

        while (resultSet.next()) {
            // read columns

            java.sql.Timestamp startTs = resultSet.getTimestamp("start_time");
            java.sql.Timestamp endTs = resultSet.getTimestamp("finish_time");

            // Convert to LocalDateTime
            LocalDateTime start = startTs.toLocalDateTime();
            LocalDateTime end = endTs.toLocalDateTime();

            StudyBlock temp = new StudyBlock(start);
            temp.endTime = end;
            blocks.add(temp);

        }
        return blocks;

    }

    public LocalTime getDayGoal(LocalDate day) throws SQLException {
        String sql = "SELECT goal_time " +
                "FROM Days " +
                "WHERE day_date = '" + day.format(SQL_DATE) + "'";
        resultSet = statement.executeQuery(sql);

        if (resultSet.next()) {
            java.sql.Time t = resultSet.getTime("goal_time");
            if (t != null) {
                return t.toLocalTime();
            } else {
                return null;
            }
        }
        return null;
    }

    public void setDailyGoal(LocalDate day, LocalTime time) throws SQLException {
        Time sqlTime = Time.valueOf(time);
        String sql = "UPDATE Days " +
                "SET goal_time = '" + sqlTime + "'"
                + " WHERE day_date = '" + day.format(SQL_DATE) + "'";
        statement.executeUpdate(sql);
    }

    public LocalTime getDayTotalTime(LocalDate day) throws SQLException {
        String sql = "SELECT total_time " +
                "FROM Days " +
                "WHERE day_date = '" + day.format(SQL_DATE) + "'";
        resultSet = statement.executeQuery(sql);

        if (resultSet.next()) {
            java.sql.Time t = resultSet.getTime("total_time");
            if (t != null) {
                return t.toLocalTime();
            } else {
                return null;
            }
        }
        return null;
    }

    public void deleteBlock(LocalDate day, LocalTime time) throws SQLException {
        LocalDateTime combined = LocalDateTime.of(day, time);
        System.out.println(combined);
        String sql = "DELETE FROM StudyBlocks WHERE start_time = '"
                + combined.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "'";
        statement.executeUpdate(sql);
    }

    void removeFromTheDay(LocalDate day, int hours, int minutes, int seconds) throws SQLException {
        String delta = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        String sql = "UPDATE Days " +
                "SET total_time = SUBTIME(total_time, '" + delta + "') " +
                "WHERE day_date = '" + day.format(SQL_DATE) + "'";
kjij
        statement.executeUpdate(sql);

    }

}
