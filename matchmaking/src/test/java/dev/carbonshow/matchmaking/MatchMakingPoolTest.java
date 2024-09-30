package dev.carbonshow.matchmaking;

import dev.carbonshow.matchmaking.config.SolverParameters;
import dev.carbonshow.matchmaking.config.TimeVaryingConfig;
import dev.carbonshow.matchmaking.pool.MatchMakingPoolGraph;
import dev.carbonshow.matchmaking.solver.MatchMakingCPSolver;
import dev.carbonshow.matchmaking.solver.MatchMakingDecomposeSolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class MatchMakingPoolTest {

    private final SolverParameters SOLVER_PARAMETERS_LIMIT = new SolverParameters(1, 50, 30);
    private final SolverParameters SOLVER_PARAMETERS = new SolverParameters(-1, -1, 999999);

    @Tag("100Units")
    @Test
    void matchMakingWithTimeLimit() {
        Assertions.assertDoesNotThrow(() -> {
            var pool = new MatchMakingPoolGraph(TestUtilities.CRITERIA, "test", TimeVaryingConfig.defaultVal());

            System.out.println("[Create Units]");
            for (int i = 0; i < 100; i++) {
                var unit = TestUtilities.createMatchUnit();
                System.out.println(unit);
                assertTrue(pool.addMatchUnit(unit));
            }

            long start = System.currentTimeMillis();
            var solver = new MatchMakingCPSolver(TestUtilities.CRITERIA, "test", TimeVaryingConfig.defaultVal());
            var solutions = solver.solve(pool, SOLVER_PARAMETERS_LIMIT, Instant.now().getEpochSecond());
            System.out.println("\nElapsed time of solving: " + (System.currentTimeMillis() - start) + "ms");
            int i = 1;
            ArrayList<Long> unitIds = new ArrayList<>();

            System.out.println("\n[Solutions]");
            for (var game : solutions.results()) {
                System.out.println("\nGame " + i);
                int j = 1;
                for (var team : game) {
                    System.out.println("  Team " + j);
                    int userCountPerTeam = 0;
                    for (var unitId : team) {
                        var unit = pool.getMatchUnit(unitId);
                        assertNotNull(unit);
                        userCountPerTeam += unit.userCount();
                        unitIds.add(unitId);
                        System.out.println("    Unit " + unit);
                    }
                    j++;

                    assertEquals(userCountPerTeam, TestUtilities.CRITERIA.userCountPerTeam(), "user count per team should be correct");
                }
                i++;

                assertEquals(game.size(), TestUtilities.CRITERIA.teamCountPerGame(), "team count per game should be correct");
            }

            // match unit should be unique
            assertEquals(new HashSet<>(unitIds).size(), unitIds.size(), "match unit should be allocated in one game once");
        });
    }

    @Test
    void testDecomposeSolver() {
        Assertions.assertDoesNotThrow(() -> {
            var pool = new MatchMakingPoolGraph(TestUtilities.CRITERIA, "test", TimeVaryingConfig.defaultVal());
            System.out.println("[Create Units]");
            for (int i = 0; i < 100; i++) {
                var unit = TestUtilities.createMatchUnit();
                assertTrue(pool.addMatchUnit(unit));
                System.out.println("Unit :" + unit);
            }

            long start = System.currentTimeMillis();
            var solver = new MatchMakingDecomposeSolver(TestUtilities.CRITERIA, "test", TimeVaryingConfig.defaultVal());
            var solutions = solver.solve(pool, SOLVER_PARAMETERS, Instant.now().getEpochSecond());
            assertNull(solutions);

            var end = System.currentTimeMillis();
            System.out.println("\nElapsed time of solving: " + (end - start) + "ms");
        });
    }
}
