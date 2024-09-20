package dev.carbonshow.matchmaking;

import java.sql.*;

/**
 * 匹配池中多个匹配单元，存在频繁的更新、删除，以及复杂的过滤、排序。
 * 在这种情况下基于 In-Memory Database Engine 进行匹配单元的管理
 */
public abstract class MatchMakingPoolInMemDB implements MatchMakingPool {
    final private MatchMakingCriteria criteria;
    final private Connection connection;
    final private String name;
    final private String dbTableName;

    /**
     * 设置匹配配置参数和指标信息，并创建内存数据库用于管理、查询匹配单元
     *
     * @param criteria 匹配参数和指标
     * @param name     当前匹配池的名称
     * @throws SQLException 创建内存数据库可能出错
     */
    MatchMakingPoolInMemDB(MatchMakingCriteria criteria, String name) throws SQLException {
        this.criteria = criteria;
        this.name = name;
        dbTableName = "mmpool_" + name.toLowerCase();
        connection = DriverManager.getConnection("jdbc:h2:mem:matchmaking", "user", "password");

        initializeDatabase();
    }

    @Override
    public boolean addMatchUnit(MatchUnit matchUnit) {
        final String addMatchUnit = "INSERT INTO " + dbTableName + " VALUES(?, ?)";
        try {
            PreparedStatement statement = connection.prepareStatement(addMatchUnit);
            statement.setLong(1, matchUnit.matchUnitId());
            statement.setInt(2, matchUnit.rank());
            int count = statement.executeUpdate();
            return count > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public int removeMatchUnit(long matchUnitId) {
        final String removeMatchUnit = "DELETE FROM " + dbTableName + " WHERE ID = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(removeMatchUnit);
            statement.setLong(1, matchUnitId);
            return statement.executeUpdate();
        } catch (SQLException e) {
            return 0;
        }
    }

    /**
     * 数据库初始化：建表、建索引
     *
     * @throws SQLException 创建过程可能出现异常
     */
    private void initializeDatabase() throws SQLException {
        // 创建数据库
        final String stmtCreateTable = "CREATE TABLE IF NOT EXISTS " + dbTableName + " (id BIGINT PRIMARY KEY , rank INT)";
        Statement statement = connection.createStatement();
        statement.execute(stmtCreateTable);

        // 建索引
        final String stmtIndexRank = "CREATE INDEX index_rank ON " + dbTableName + " (rank)";
        statement.execute(stmtIndexRank);
    }
}
