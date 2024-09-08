package dev.carbonshow.algorithm.partition;

import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class MaxPartitionBenchmark {

    final private Map<Integer, Integer> ADDENDS = Map.of(1, 100, 2, 40, 5, 10);
    final private int PARTITIONED = 10;
    final private ArrayList<Map<Integer, Long>> PARTITION_PLANS = new ArrayList<>(Arrays.asList(Map.of(1, 6L, 2, 2L),
            Map.of(1, 1L, 2, 2L, 5, 1L),
            Map.of(5, 2L),
            Map.of(1, 5L, 5, 1L)
    ));

    final private DefaultMaxPartitions solver = new DefaultMaxPartitions();
    final private IntegerProgrammingMaxPartitions ipSolver = new IntegerProgrammingMaxPartitions();

    @Fork(value = 1, warmups = 1)
    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void solveTwoPhase() {
        solver.solve(ADDENDS, PARTITIONED);
    }

    @Fork(value = 1, warmups = 1)
    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void solveWithPartitionsTwoPhase() {
        solver.solveWithPartitionPlan(ADDENDS, PARTITIONED, PARTITION_PLANS);
    }

    @Fork(value = 1, warmups = 1)
    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void solveWithIntegerProgramming() {
        ipSolver.solve(ADDENDS, PARTITIONED);
    }
}
