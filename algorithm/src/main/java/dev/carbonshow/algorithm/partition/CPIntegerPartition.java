package dev.carbonshow.algorithm.partition;

import com.google.ortools.Loader;
import com.google.ortools.sat.*;

import java.util.ArrayList;

/**
 * 基于 CP-SAT 的解决方案，全称是 Constraint Programming Satisfiability。即不指定优化目标而是给出约束条件，来找到可行解。
 * <p>
 *     具体到整数划分问题，约束条件只有一个即加数之和等于被划分的整数
 *     性能比动态规划会低很多，面对整数 10 使用[1,10]这个具体场景，每次操作的时间开销相差 1000 倍
 * </p>
 * <p>
 *     <b>注意:</b>
 *     <p>如果用于大规模问题，比如将 100 用[1,100]分拆，结果有数亿，这时使用 CP 方法，性能非常低，不建议使用</p>
 * </p>
 */
public class CPIntegerPartition implements IntegerPartition {
    // 定义划分结果记录器，在找到合适解后保存
    static class PartitionRecorder extends CpSolverSolutionCallback {
        // 保存加数数组
        private final long[] addends;

        // 保存加数数量变量数组
        private final IntVar[] variableArray;

        // 保存每个划分结果各个加数的使用数量
        private final ArrayList<ArrayList<Long>> partitions = new ArrayList<>();

        // 构造函数，获取加数变量
        public PartitionRecorder(long[] addendSet, IntVar[] x) {
            addends = addendSet;
            variableArray = x;
        }

        // 每找到可用解则添加到本地
        @Override
        public void onSolutionCallback() {
            ArrayList<Long> partition = new ArrayList<>();
            for (int i = 0; i < addends.length; i++) {
                long addend = addends[i];
                IntVar v = variableArray[i];

                var addendCount = value(v);
                while (addendCount > 0) {
                    partition.add(addend);
                    addendCount--;
                }
            }
            partitions.add(partition);
        }

        // 返回所有可用解
        public ArrayList<ArrayList<Long>> getSolutions() {
            return partitions;
        }
    }

    // 定义划分结果计数器，只记录解的数量而非细节
    static class PartitionCounter extends CpSolverSolutionCallback {
        // 保存数量
        private long count = 0;

        // 每找到可用解则添加到本地
        @Override
        public void onSolutionCallback() {
            count++;
        }

        // 返回所有可用解
        public long getSolutionsCount() {
            return count;
        }
    }

    CPIntegerPartition() {
        Loader.loadNativeLibraries();
    }

    @Override
    public long solve(long[] addendSet, long partitioned) {
        PartitionCounter cb = new PartitionCounter();
        IntVar[] x = new IntVar[addendSet.length];
        solveImpl(addendSet, partitioned, cb, x);
        return cb.getSolutionsCount();
    }

    /**
     * 获取所有满足约束条件的划分——即每个划分包含的加数之和等于被划分的整数
     *
     * @param addendSet   加数集合，内部不能有重复的元素，必须均为正整数。不同数量的不同加数之和应该等于 `partitioned`
     * @param partitioned 被划分的正整数
     * @return 返回所有符合要求的划分
     */
    @Override
    public ArrayList<ArrayList<Long>> solveWithPartitions(long[] addendSet, long partitioned) {
        IntVar[] x = new IntVar[addendSet.length];
        PartitionRecorder cb = new PartitionRecorder(addendSet, x);
        solveImpl(addendSet, partitioned, cb, x);
        return cb.getSolutions();
    }

    /**
     * 获取所有满足约束条件的划分——即每个划分包含的加数之和等于被划分的整数
     *
     * @param addendSet   加数集合，内部不能有重复的元素，必须均为正整数。不同数量的不同加数之和应该等于 `partitioned`
     * @param partitioned 被划分的正整数
     */
    private void solveImpl(long[] addendSet, long partitioned, CpSolverSolutionCallback cb, IntVar[] x) {
        var cpModel = new CpModel();

        for (int i = 0; i < x.length; i++) {
            x[i] = cpModel.newIntVar(0, partitioned / addendSet[i], "x" + i);
        }

        cpModel.addEquality(LinearExpr.weightedSum(x, addendSet), partitioned);

        CpSolver solver = new CpSolver();
        solver.getParameters().setEnumerateAllSolutions(true);
        solver.solve(cpModel, cb);
    }
}
