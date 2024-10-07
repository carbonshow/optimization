package dev.carbonshow.matchmaking;

import dev.carbonshow.matchmaking.config.SolverParameters;
import dev.carbonshow.matchmaking.config.TimeVaryingConfig;
import dev.carbonshow.matchmaking.pool.MatchMakingPool;
import dev.carbonshow.matchmaking.pool.MatchMakingPoolGraph;
import dev.carbonshow.matchmaking.solver.MatchMakingCPSolver;
import dev.carbonshow.matchmaking.solver.MatchMakingDecomposeSolver;
import dev.carbonshow.matchmaking.solver.MatchMakingSolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class MatchMakingTest {

    private final SolverParameters SOLVER_PARAMETERS_LIMIT = new SolverParameters(5000, 5000, 9999);
    private final SolverParameters SOLVER_PARAMETERS = new SolverParameters(-1, -1, 999999);
    private final int UNIT_COUNT = 200;

    @Test
    void testCPSolverWithFeasibleSolutions() {
        Assertions.assertDoesNotThrow(() -> {
            var pool = new MatchMakingPoolGraph(TestUtilities.CRITERIA, "test", TimeVaryingConfig.defaultVal());

            System.out.println("[Create Units]");
            for (var unit : TestUtilities.getFeasibleMatchUnit()) {
                System.out.println(unit);
                assertTrue(pool.addMatchUnit(unit));
            }

            var solver = new MatchMakingCPSolver(TestUtilities.CRITERIA, "test", TimeVaryingConfig.defaultVal());

            solveAndValidate(solver, pool);
        });
    }

    @Test
    void testCPSolverWithRandomUnits() {
        Assertions.assertDoesNotThrow(() -> {
            var pool = new MatchMakingPoolGraph(TestUtilities.CRITERIA, "test", TimeVaryingConfig.defaultVal());

            System.out.println("[Create Units]");
            for (int i = 0; i < UNIT_COUNT; i++) {
                var unit = TestUtilities.createMatchUnit();
                System.out.println(unit);
                assertTrue(pool.addMatchUnit(unit));
            }

            var solver = new MatchMakingCPSolver(TestUtilities.CRITERIA, "test", TimeVaryingConfig.defaultVal());

            solveAndValidate(solver, pool);
        });
    }

    @Test
    void testDecomposeSolver() {
        Assertions.assertDoesNotThrow(() -> {
            var pool = new MatchMakingPoolGraph(TestUtilities.CRITERIA, "test", TimeVaryingConfig.defaultVal());

            for (int i = 0; i < UNIT_COUNT; i++) {
                var unit = TestUtilities.createMatchUnit();
                assertTrue(pool.addMatchUnit(unit));
            }

            var solver = new MatchMakingDecomposeSolver(TestUtilities.CRITERIA, "test", TimeVaryingConfig.defaultVal());

            solveAndValidate(solver, pool);
        });
    }

    void solveAndValidate(MatchMakingSolver solver, MatchMakingPool pool) {
        System.out.println("Unit Count: " + pool.matchUnitCount());
        System.out.println("User Count: " + pool.userCount());
        System.out.println("Max Game Count: " + pool.maxGameCount());
        long start = System.currentTimeMillis();
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
    }
}
