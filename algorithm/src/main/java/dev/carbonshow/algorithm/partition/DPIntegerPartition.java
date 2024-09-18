package dev.carbonshow.algorithm.partition;

import java.util.ArrayList;

/**
 * 使用动态规划方法解决整数划分问题
 */
public class DPIntegerPartition implements IntegerPartition {
  /**
   * 动态规划的实现方式，包括两种自底向上和自顶向下。但均基于状态转移方程
   */
  public enum DPImplementMethod {
    // bottom-up 自底向上
    TABLE_FILL,

    // 自顶向下
    RECURSION
  }

  // 模式实现方式为自顶向下
  final private DPImplementMethod SOLVE_METHOD;

  /**
   * 默认是递归实现
   */
  DPIntegerPartition() {
    this(DPImplementMethod.RECURSION);
  }

  /**
   * 可以指定实现方法，默认是递归
   *
   * @param implementMethod 动态规划的实现方法
   */
  DPIntegerPartition(DPImplementMethod implementMethod) {
    SOLVE_METHOD = implementMethod;
  }

  /**
   * 获取划分方案最大值。动态规划状态转移方程如下所示：
   * <p>
   * 令 dp[i][j] 表示正整数 j 被大小为 i 加数集合拆分时，总的划分数。
   * 状态转移方程是：
   * {{
   * // a(i) 表示加数集合中第 i 个元素
   * dp[i][j] = dp[i][j-a(i)] + dp[i-1][j]
   * }}
   * 动态规划，可以自底向上，或自顶向下实现，依据不同情况分别实现
   *
   * @param addendSet   加数集合，内部不能有重复的元素，且升序排列，必须均为正整数。不同数量的不同加数之和应该等于 `partitionedValue`
   * @param partitioned 等待被拆分的正整数
   * @return 返回总的划分数量
   */
  @Override
  public long solve(long[] addendSet, long partitioned) {
    return SOLVE_METHOD == DPImplementMethod.RECURSION ? recursiveSolve(addendSet, addendSet.length, partitioned) : tableFillSolve(addendSet, partitioned);
  }

  /**
   * 递归解决方案，包含了可用的具体划分方案，以递归路径的形式表达
   *
   * @param addendSet   加数集合，内部不能有重复的元素，且升序排列，必须均为正整数。不同数量的不同加数之和应该等于 `partitionedValue`
   * @param partitioned 等待被拆分的正整数
   * @return 返回可用的所有划分方案
   */
  @Override
  public ArrayList<ArrayList<Long>> solveWithPartitions(long[] addendSet, long partitioned) {
    var paths = new ArrayList<ArrayList<Long>>();
    var path = new ArrayList<Long>();

    recursiveSolveWithPartitionsImpl(addendSet, addendSet.length, partitioned, paths, path);

    return paths;
  }

  /**
   * 自底向上，先计算靠前状态的结果，再通过状态转移方程，计算下一个状态结果
   *
   * @param addendSet   加数集合，内部不能有重复的元素，且升序排列，必须均为正整数。不同数量的不同加数之和应该等于 `partitionedValue`
   * @param partitioned 等待被拆分的正整数
   * @return 返回总的划分数量
   */
  private long tableFillSolve(long[] addendSet, long partitioned) {
    // 定义 dp 状态空间，默认全部为 0
    long[][] dp = new long[addendSet.length + 1][(int)partitioned + 1];

    // 初始化将 partitionedValue 为 0 时的 dp 值设置为 1
    for (int i = 0; i <= addendSet.length; i++) {
      dp[i][0] = 1;
    }

    // 从初始值开始遍历，逐步构造获得最终结果，注意从 1 开始计数，1 表示addendSet中的第一个元素
    for (int i = 1; i <= addendSet.length; i++) {
      for (int j = 1; j < partitioned + 1; j++) {
        var partitionedForInclude = (int)(j - addendSet[i - 1]);
        var dpForInclude = (partitionedForInclude < 0 ? 0 : dp[i][partitionedForInclude]);
        dp[i][j] = dpForInclude + dp[i - 1][j];
      }
    }

    return dp[addendSet.length][(int)partitioned];
  }

  /**
   * 自顶向下递归实现
   *
   * @param addendSet        加数集合，内部不能有重复的元素，且升序排列，必须均为正整数。不同数量的不同加数之和应该等于 `partitionedValue`
   * @param addendSize       加数集合只考虑最小的指定数量的加数
   * @param partitionedValue 等待被拆分的正整数
   * @return 返回总的划分数量
   * @see DPIntegerPartition
   * <p>
   * 采取和动态规划求解器相同的思路，只不过将问题转化为递归方式解决。
   */
  private long recursiveSolve(long[] addendSet, int addendSize, long partitionedValue) {
    if (partitionedValue < 0 || addendSize <= 0) {
      return 0L;
    } else if (partitionedValue == 0) {
      return 1L;
    } else {
      return recursiveSolve(addendSet, addendSize, partitionedValue - addendSet[addendSize - 1]) +
        recursiveSolve(addendSet, addendSize - 1, partitionedValue);
    }
  }

  /**
   * 递归解决方案，包含了可用的具体划分方案，这是一个深度优先遍历的过程，因此可以使用一个链表记录单条可用路径，然后将所有可用解返回即可。
   *
   * @param addendSet        加数集合，内部不能有重复的元素，且升序排列，必须均为正整数。不同数量的不同加数之和应该等于 `partitionedValue`
   * @param addendSize       加数集合只考虑最小的指定数量的加数
   * @param partitionedValue 等待被拆分的正整数
   * @param paths            可用的划分集合，以树遍历路径的形式表示
   * @param curPath          单条划分，不一定可用
   */
  private void recursiveSolveWithPartitionsImpl(long[] addendSet, int addendSize, long partitionedValue, ArrayList<ArrayList<Long>> paths, ArrayList<Long> curPath) {
    if (partitionedValue < 0 || addendSize <= 0) {
      // 说明路径不可用
      curPath.clear();
    } else if (partitionedValue == 0) {
      // 可用则将当前路径添加至最终结果
      paths.add(curPath);
    } else {
      var newPath = new ArrayList<>(curPath);

      // 包含分支
      var maxAddend = addendSet[addendSize - 1];
      curPath.add(maxAddend);
      recursiveSolveWithPartitionsImpl(addendSet, addendSize, partitionedValue - maxAddend, paths, curPath);

      // 不包含的分支
      recursiveSolveWithPartitionsImpl(addendSet, addendSize - 1, partitionedValue, paths, newPath);
    }
  }
}
