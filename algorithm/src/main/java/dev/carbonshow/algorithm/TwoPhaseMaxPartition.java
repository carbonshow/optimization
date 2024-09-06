package dev.carbonshow.algorithm;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

/**
 * 基于整数规划解决最大划分问题，具体包含两个过程：
 * <ol>
 *   <li>通过Integer Partition 确定整数划分方案</li>
 *   <li>通过整数线性规划确定划分数最多的方式</li>
 * </ol>
 */
public class TwoPhaseMaxPartition implements MaxPartitions {

  // 建立基于动态规划，并通过递归方式实现的整数划分(Integer Partition, IP)优化器
  final private DPIntegerPartition ipSolver = new DPIntegerPartition();

  /**
   * 构造入口，提前加载整数线性规划的 native library，通过 JNI 接口调用 c++ 实现
   */
  TwoPhaseMaxPartition() {
    Loader.loadNativeLibraries();
  }

  /**
   * 获取最大划分的细节数据
   * @param finiteAddends 加数集合，这是一个二维数组，一维表示不同的加数，二维应该具有两个元素，分别为加数的值，以及当前加数的数量
   * @param partitioned   对上面的加数集合进行划分，每个划分所含加数之和应该等于该值。
   * @return 返回最大化分的细节数据，包含每个划分的组成，以及该划分实例化之后的数据
   */
  @Override
  public ArrayList<PartitionData> solveWithPartitions(Map<Integer, Integer> finiteAddends, int partitioned) {
    return solveImpl(finiteAddends, partitioned);
  }

  /**
   * 求解过程的具体实现，包含以下几个主要步骤：
   * <ol>
   *   <li>加数排序</li>
   *   <li>基于加数集合，在不考虑加数限制的情况下，生成划分方案</li>
   *   <li>通过线性规划，考虑加数总数限制前提下，所有可能划分方案实例化总数的最大化</li>
   * </ol>
   * @param finiteAddends 加数集合，key 是加数，value 是加数的总数
   * @param partitioned 等待被划分的数，即划分得到的每个小集合内所有加数之和
   * @return 返回划分详细数据列表，可能返回 null
   */
  private ArrayList<PartitionData> solveImpl(Map<Integer, Integer> finiteAddends, int partitioned) {
    // 升序排列的加数列表
    int[] orderedAddends = new int[finiteAddends.size()];

    // 每个加数存在的数量上限
    int[] orderedAddendCounts = new int[finiteAddends.size()];

    // 对加数进行升序排序
    var sortedAddendMap = new TreeMap<>(finiteAddends);
    int i = 0;
    for (Integer addend : sortedAddendMap.keySet()) {
      orderedAddends[i] = addend;
      orderedAddendCounts[i] = sortedAddendMap.get(addend);
      i++;
    }

    // 得到划分方案
    var partitionPlans = solveIntegerPartition(orderedAddends, partitioned);

    // 进行线性规划，计算划分方案实例化之后总数最大化的情况
    var planCounts = solveIntegerProgramming(orderedAddends, orderedAddendCounts, partitionPlans);
    if (planCounts == null) {
      return null;
    }

    var result = new ArrayList<PartitionData>();
    for (int j = 0; j < partitionPlans.size(); j++) {
      result.add(new PartitionData(partitionPlans.get(j), planCounts.get(j)));
    }
    return result;
  }

  /**
   * 针对给定的加数集合，生成划分方案集合
   *
   * @param orderedAddends 升序排列的加数列表
   * @param partitioned    目标凑数值，每种划分方案中加数组合之和
   * @return 划分方案列表，每个方案是一个 Map，key 表示加数，value 表示加数数量
   */
  private ArrayList<Map<Integer, Long>> solveIntegerPartition(int[] orderedAddends, int partitioned) {

    // 获取排列方案列表
    return ipSolver.solveWithPartitions(orderedAddends, partitioned)
      .stream().map(partition ->
        partition.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting())))
      .collect(Collectors.toCollection(ArrayList::new));
  }

  /**
   * 使用整数规划计算划分总数的最大值。对于划分方案 P(i)，假设该方案可以重复 d(i)次。
   * 约束条件是：每种加数被使用的次数不能超过自己的最大数量
   * 优化目标是：d(i)的总数最大化
   * @param addends 加数列表
   * @param addendCounts 加数数量列表，与 addends 一一对应
   * @param partitionPlans 划分方案列表
   * @return 返回各个方案的出现次数，如果无解则返回 null
   */
  private ArrayList<Integer> solveIntegerProgramming(int[] addends, int[] addendCounts, ArrayList<Map<Integer, Long>> partitionPlans) {
    // 基于 SCIP 实现整数规划
    MPSolver solver = MPSolver.createSolver("SCIP");
    if (solver == null) {
      return null;
    }

    // 决策变量，每种划分方案出现的次数，取值范围是 [0, max(c(i))]，c(i)表示第 i 个加数的总数
    int addendMaxCount = Arrays.stream(addendCounts).max().getAsInt();
    ArrayList<MPVariable> partitionPlanCountVariables = new ArrayList<>(partitionPlans.size());
    for (int i = 0; i < partitionPlans.size(); i++) {
      partitionPlanCountVariables.add(solver.makeIntVar(0.0, addendMaxCount, "p"+i));
    }

    // 约束条件，每个加数在所有方案中出现的总次数不得超过其总数，所以约束条件数量和加数相同
    for (int i =0; i < addends.length; i++) {
      int addend = addends[i];
      int addendCountTotal = addendCounts[i];

      // 加数的约束条件，使用的总次数一定处于 [0, addendCountTotal] 之内
      var constraint = solver.makeConstraint(0.0, addendCountTotal, "c"+i);

      // 决策变量即每个划分方案的实施次数。对于当前加数而言：
      // 加数使用总次数 = 方案1实施次数*方案1内加数使用次数 + 方案2实施次数*方案2内加数使用次数 + ...
      // 加数使用总次数 ≤ 加数总数
      // 向该约束条件中，第 i 个加数的系数，即每个方案出现次数和该方案内该加数出现次数的乘积，之和
      for (int planIndex = 0; planIndex < partitionPlans.size(); planIndex++) {
        long addendCountUsedByCurPlan = partitionPlans.get(planIndex).getOrDefault(addend, 0L);
        constraint.setCoefficient(partitionPlanCountVariables.get(planIndex), addendCountUsedByCurPlan);
      }
    }

    // 设置优化目标，即各个方案出现次数之和的最大值，即所有决策变量的系数均为 1
    MPObjective objective = solver.objective();
    partitionPlanCountVariables.forEach(variable -> objective.setCoefficient(variable, 1));
    objective.setMaximization();

    // 求解
    final MPSolver.ResultStatus resultStatus = solver.solve();
    if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
      // 获得最优解
      return partitionPlanCountVariables.stream().map(variable -> (int)variable.solutionValue()).collect(Collectors.toCollection(ArrayList::new));
    } else {
      // 无解
      return null;
    }
  }
}
