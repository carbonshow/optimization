package dev.carbonshow.algorithm.partition;

import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class MaxPartitionBenchmark {

    final private Map<Long, Long> ADDENDS = Map.of(1L, 100L, 2L, 40L, 5L, 10L);
    final private long PARTITIONED = 10;
    final private ArrayList<Map<Long, Long>> PARTITION_PLANS = new ArrayList<>(Arrays.asList(Map.of(1L, 6L, 2L, 2L),
            Map.of(1L, 1L, 2L, 2L, 5L, 1L),
            Map.of(5L, 2L),
            Map.of(1L, 5L, 5L, 1L)
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
