package dev.carbonshow.algorithm.partition;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

/**
 * 使用动态规划和 Constraint-Programming 来解决整数划分问题，性能对比如下所示：
 * <ul>
 *     <li>拆分整数 10。Constraint-Programming 的开销相比动态规划，性能开销将近 1000 倍
 *         <ul>
 *             <li>IntegerPartitionBenchmark.solveWithCP: 6.485±0.199 ms/op</li>
 *             <li>IntegerPartitionBenchmark.solveWithDP: 0.006±0.001 ms/op</li>
 *         </ul>
 *     </li>
 *     <li>拆分整数 100。由于要所有可用方案，Constraint-Programming 的开销相比拆分 10，增加了近 3000 倍，每次运算约 20s</li>
 * </ul>
 */
@State(Scope.Benchmark)
public class IntegerPartitionBenchmark {
    static long[] group10 = new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    static long[] group100 = LongStream.rangeClosed(1, 100).toArray();

    final private DPIntegerPartition dpSolver = new DPIntegerPartition();
    final private CPIntegerPartition cpSolver = new CPIntegerPartition();

    @Fork(value = 1, warmups = 1)
    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void solveWithDP10() {
        dpSolver.solveWithPartitions(group10, 10);
    }

    @Fork(value = 1, warmups = 0)
    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void solveWithCP10() {
        cpSolver.solveWithPartitions(group10, 10);
    }

    @Fork(value = 1, warmups = 1)
    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void solveWithDP100() {
        dpSolver.solveWithPartitions(group100, 100);
    }

    @Fork(value = 1, warmups = 0)
    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void solveWithCP100() {
        cpSolver.solveWithPartitions(group10, 100);
    }
}
