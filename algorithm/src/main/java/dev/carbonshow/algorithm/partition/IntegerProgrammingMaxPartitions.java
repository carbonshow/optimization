package dev.carbonshow.algorithm.partition;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 基于整数线性规划实现的解决方案，具体是：
 * <ul>
 *     <li>建立二维决策变量 Variables[MAX_ADDEND_KINDS][MAX_PARTITIONS_NUM]，表示各加数在各划分方案中使用次数
 *         <ul>
 *             <li>MAX_ADDEND_KINDS，表示加数的不同种类的数量</li>
 *             <li>MAX_PLANS_NUM，表示划分的最大数量，可以划分可以重复，实际上不大于单个加数的最大数量</li>
 *         </ul>
 *     </li>
 *     <li>建立一维决策变量 Variables[MAX_PARTITIONS_NUM]，用于记录实际每个方案划分，要么存在是 1，要么不存在是 0</li>
 *     <li>约束条件有：
 *         <ul>
 *             <li>每个划分使用的加数之和必须等于被划分数</li>
 *             <li>每种加数在所有划分中的实际使用次数不能超过其总数</li>
 *         </ul>
 *     </li>
 *     <li>优化目标是：Variables[MAX_PARTITIONS_NUM] 中各变量之和最大化 </li>
 * </ul>
 */
public class IntegerProgrammingMaxPartitions implements MaxPartitions {
    // 基于 Google OR-Tools 的数学优化器
    final private MPSolver solver;

    IntegerProgrammingMaxPartitions() {
        Loader.loadNativeLibraries();
        solver = MPSolver.createSolver("SCIP");
        if (solver == null) {
            throw new RuntimeException("fail to create solver for IntegerProgramming");
        }
    }

    /**
     * 基于整数线性规划一步完成整个计算过程，不再区分划分方案和划分实例，而是直接评估划分实例：
     * <ul>
     *     <li>决策变量：加数在不同划分中的出现次数；以及每个划分方案是否实际使用的标记 0 或 1</li>
     *     <li>约束条件：每个划分的加数之和等于被划分数；每种加数的使用次数不超过加数总数</li>
     *     <li>优化目标：每个划分方案的标记标记之和最大化，即有效划分的数量最大化</li>
     * </ul>
     *
     * @param addends     加数集合，这是一个二维数组，一维表示不同的加数，二维应该具有两个元素，分别为加数的值，以及当前加数的数量
     * @param partitioned 对上面的加数集合进行划分，每个划分所含加数之和应该等于该值。
     * @return 返回最终划分结果的详细数据，将所有划分方案的组成以及实施数量罗列出来，不存在则返回 null
     */
    @Override
    public ArrayList<PartitionData> solve(Map<Long, Long> addends, long partitioned) {
        // 清理求解器
        solver.clear();

        // 确定加数种类的最大数量和划分的最大数量，划分最大数量一定不超过所有加数数量之和
        final int maxAddendKinds = addends.size();
        final int maxPartitions = addends.values().stream().mapToInt(Long::intValue).sum();

        // 将加数和对应总数拆分到两个不同的数组中，并按加数大小升序排列
        var orderedAddends = new long[addends.size()];
        var orderedAddendCounts = new long[addends.size()];
        MaxPartitionsUtils.addendsToArray(addends, orderedAddends, orderedAddendCounts);

        // 定义加数决策变量， 即每个划分中该加数使用的数量，每个加数决策变量的上限必定不能超过当前加数的总数
        MPVariable[][] addendVariables = new MPVariable[maxAddendKinds][maxPartitions];
        for (int i = 0; i < maxAddendKinds; i++) {
            for (int j = 0; j < maxPartitions; j++) {
                addendVariables[i][j] = solver.makeIntVar(0, orderedAddendCounts[i], StringUtils.join("a", i, "_", j));
            }
        }

        // 定义划分决策变量，每个划分要么存在为 1，要么不存在为 0
        MPVariable[] partitionVariables = new MPVariable[maxPartitions];
        for (int j = 0; j < maxPartitions; j++) {
            partitionVariables[j] = solver.makeIntVar(0, 1, "p" + j);
        }

        // 添加加数的约束条件，即加数使用总数不得超限
        for (int i = 0; i < maxAddendKinds; i++) {
            var constraint = solver.makeConstraint(0, orderedAddendCounts[i], "ac" + i);
            for (int j = 0; j < maxPartitions; j++) {
                constraint.setCoefficient(addendVariables[i][j], 1);
            }
        }

        // 添加划分的约束条件，每个划分要么存在要么不存在，但不管怎样加数之和，划分标记与被划分数值乘积，两者必定相等
        // 换句话两者差值为 0
        for (int j = 0; j < maxPartitions; j++) {
            var constraint = solver.makeConstraint(0, 0, "pc" + j);
            constraint.setCoefficient(partitionVariables[j], -partitioned);
            for (int i = 0; i < maxAddendKinds; i++) {
                constraint.setCoefficient(addendVariables[i][j], orderedAddends[i]);
            }
        }

        // 设定优化目标，即方案总数最大化
        MPObjective objective = solver.objective();
        for (int j = 0; j < maxPartitions; j++) {
            objective.setCoefficient(partitionVariables[j], 1);
        }
        objective.setMaximization();

        // 求解
        final MPSolver.ResultStatus result = solver.solve();
        if (result == MPSolver.ResultStatus.OPTIMAL) {
            ArrayList<PartitionData> partitionData = new ArrayList<>();
            for (int j = 0; j < maxPartitions; j++) {
                if (partitionVariables[j].solutionValue() == 1) {
                    // 说明该划分实际存在，获取划分内容
                    var partition = new HashMap<Long, Long>();
                    for (int i = 0; i < maxAddendKinds; i++) {
                        var addendCnt = (long) addendVariables[i][j].solutionValue();
                        if (addendCnt > 0) {
                            partition.merge(orderedAddends[i], addendCnt, (k, v) -> v + addendCnt);
                        }
                    }
                    partitionData.add(new PartitionData(partition, 1));
                }
            }
            return partitionData;
        }
        return null;
    }
}
