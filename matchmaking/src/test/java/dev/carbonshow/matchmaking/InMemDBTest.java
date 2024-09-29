package dev.carbonshow.matchmaking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

import java.sql.Connection;
import java.sql.DriverManager;

public class InMemDBTest {
    private static Connection connection;

    @BeforeAll
    static void init() {
        Assertions.assertDoesNotThrow(() -> {
            connection = DriverManager.getConnection("jdbc:h2:mem:matchmaking", "user", "password");
            var statement = connection.createStatement();

            // 创建数据库
            var dbTableName = "pool";
            final String sqlCreateTbl = "CREATE TABLE IF NOT EXISTS " + dbTableName +
                    " (id BIGINT PRIMARY KEY , enter_timestamp BIGINT, usercnt INT, rank INT, positions BIGINT, skill NUMERIC, ewp NUMERIC)";
            statement.addBatch(sqlCreateTbl);

            // 建索引
            final String sqlIndexRank = "CREATE INDEX index_rank ON " + dbTableName + " (rank)";
            statement.addBatch(sqlIndexRank);
            final String sqlIndexSkill = "CREATE INDEX index_skill ON " + dbTableName + " (skill)";
            statement.addBatch(sqlIndexSkill);
            final String sqlIndexEwp = "CREATE INDEX index_ewp ON " + dbTableName + " (ewp)";
            statement.addBatch(sqlIndexEwp);
            final String sqlIndexUserCount = "CREATE INDEX index_user_cnt ON " + dbTableName + " (usercnt)";
            statement.addBatch(sqlIndexUserCount);
            final String sqlIndexEnterTime = "CREATE INDEX index_enter_time ON " + dbTableName + " (enter_timestamp)";
            statement.addBatch(sqlIndexEnterTime);
            statement.executeBatch();
        });
    }
}
