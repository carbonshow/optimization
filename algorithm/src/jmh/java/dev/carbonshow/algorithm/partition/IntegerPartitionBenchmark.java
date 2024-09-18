package dev.carbonshow.algorithm.partition;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * 使用动态规划和 Constraint-Programming 来解决整数划分问题，性能对比如下所示：
 * <ul>
 *     <li>IntegerPartitionBenchmark.solveWithCP: 6.485±0.199 ms/op</li>
 *     <li>IntegerPartitionBenchmark.solveWithDP: 0.006±0.001 ms/op</li>
 * </ul>
 */
@State(Scope.Benchmark)
public class IntegerPartitionBenchmark {
    static long[] group10 = new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

    final private DPIntegerPartition dpSolver = new DPIntegerPartition();
    final private CPIntegerPartition cpSolver = new CPIntegerPartition();

    @Fork(value = 1, warmups = 1)
    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void solveWithDP() {
        dpSolver.solveWithPartitions(group10, 10);
    }

    @Fork(value = 1, warmups = 1)
    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void solveWithCP() {
        cpSolver.solveWithPartitions(group10, 10);
    }
}
