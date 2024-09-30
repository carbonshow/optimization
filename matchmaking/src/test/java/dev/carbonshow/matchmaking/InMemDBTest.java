package dev.carbonshow.matchmaking;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObject;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;


public class InMemDBTest {
    // 创建 jdbi 连接到 in-mem 模式的 H2
    static Jdbi createJdbi() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        dataSource.setUser("none");
        dataSource.setPassword("");
        return Jdbi.create(dataSource);
    }

    public record MatchUnitMinimal(int id, int memberCount, int rank, double skill) {}

    interface MatchUnitDao extends SqlObject {
        @SqlUpdate("CREATE TABLE units (id INT PRIMARY KEY, member_count INT, rank INT, skill DOUBLE)")
        void createTable();

        @SqlUpdate("INSERT INTO units (id, member_count, rank, skill) VALUES (:id, :memberCount, :rank, :skill)")
        void addUnit(@BindMethods MatchUnitMinimal unit);

        @SqlQuery("SELECT * FROM units WHERE id = :id")
        @RegisterConstructorMapper(MatchUnitMinimal.class)
        List<MatchUnitMinimal> getUnitById(@Bind("id") int id);
    }

    @Test
    void testJdbi() {
        Jdbi jdbi = createJdbi();
        jdbi.installPlugin(new SqlObjectPlugin());

        try {
            MatchUnitDao matchUnitDao = jdbi.onDemand(MatchUnitDao.class);
            matchUnitDao.createTable();

            long start = System.currentTimeMillis();
            ArrayList<MatchUnitMinimal> units = new ArrayList<>();
            for (int i = 1; i < 10000; i++) {
                units.add(new MatchUnitMinimal(i, TestUtilities.RANDOM.nextInt(5) + 1,
                        TestUtilities.RANDOM.nextInt(10), TestUtilities.RANDOM.nextDouble(100.0)));
            }
            System.out.println("create time(ms): " + (System.currentTimeMillis() - start));

            start = System.currentTimeMillis();
            units.forEach(matchUnitDao::addUnit);
            System.out.println("insert time(ms): " + (System.currentTimeMillis() - start));

            var unit = matchUnitDao.getUnitById(100);
            System.out.println("Retrieved unit: " + unit);
        } finally {
            System.out.println("jdbi closed");
        }
    }
}
