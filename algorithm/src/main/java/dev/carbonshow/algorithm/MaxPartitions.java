package dev.carbonshow.algorithm;

import java.util.ArrayList;
import java.util.Map;

/**
 * 整数划分问题的变种：
 * <ul>
 *   <li>给定加数集合 A(M) ，共有 M 个加数，对于第 i 个加数 a(i)，其数量为 c(i) </li>
 *   <li>将上述有限的加数进行划分，确保每个划分的加数之和为 N ，两个划分的方案可以相同，假设符合该要求的划分总数为 Z </li>
 *   <li>目标：使符合要求的划分总数 Z 最大化 </li>
 * </ul>
 * 举个实例：
 * <ul>
 *   <li>有 1 元硬币 20 枚</li>
 *   <li>有 2 元硬币 10 枚</li>
 *   <li>有 5 元硬币 4 枚</li>
 *   <li>需要凑出尽可能多的 10 元</li>
 * </ul>
 * 那么有一种可能的划分是：
 * <ol>
 *   <li> 1元 10 个</li>
 *   <li> 1元 10 个</li>
 *   <li> 2元 5 个</li>
 *   <li> 2元 5 个</li>
 *   <li> 5元 2 个</li>
 *   <li> 5元 2 个</li>
 * </ol>
 * 可以发现，共有三种划分方案，每种方案实施两次，共凑出 6 个 10 元。
 */
interface MaxPartitions {

  /**
   * 获得最大划分的详细数据，包括：
   * <ul>
   *   <li>加数集合，即有哪些加数，每个加数可以使用的总数</li>
   *   <li>每个划分中，各加数之和</li>
   * </ul>
   *
   * @param finiteAddends 加数集合，这是一个二维数组，一维表示不同的加数，二维应该具有两个元素，分别为加数的值，以及当前加数的数量
   * @param partitioned   对上面的加数集合进行划分，每个划分所含加数之和应该等于该值。
   * @return 返回最终划分结果的详细数据，将所有划分方案的组成以及实施数量罗列出来
   */
  ArrayList<PartitionData> solveWithPartitions(Map<Integer, Integer> finiteAddends, int partitioned);
}
