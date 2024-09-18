package dev.carbonshow.algorithm.partition;

import java.util.ArrayList;

/**
 * 整数划分，给定一个整数 N，给定一个加数集合 A(M)，加数集合中包含 M 个互不相同的元素。
 * 将 N 划分为 A(M) 中任意种类任意数量个加数之和，获取划分方案最大数量，以及划分方案细节。
 */
public interface IntegerPartition {

  /**
   * 获取最大划分方案数量
   *
   * @param addendSet   加数集合，内部不能有重复的元素，必须均为正整数，同时应该按升序排列。不同数量的不同加数之和应该等于 `partitioned`
   * @param partitioned 被划分的正整数
   * @return 返回划分方案的最大数量
   */
  long solve(long[] addendSet, long partitioned);

  /**
   * 获取最大划分方案的细节，即具体划分方法
   *
   * @param addendSet   加数集合，内部不能有重复的元素，必须均为正整数。不同数量的不同加数之和应该等于 `partitioned`
   * @param partitioned 被划分的正整数
   * @return 返回所有的划分方案，一维区分不同划分方案，二维表示一个划分方案中的加数选择。该方案集合一定对应最大划分数。
   */
  ArrayList<ArrayList<Long>> solveWithPartitions(long[] addendSet, long partitioned);
}
