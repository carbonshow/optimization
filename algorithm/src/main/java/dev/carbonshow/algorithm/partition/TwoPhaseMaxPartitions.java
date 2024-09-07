package dev.carbonshow.algorithm.partition;

import java.util.ArrayList;
import java.util.Map;

/**
 * 两阶段解决最大化划分数量问题：
 * <ol>
 *     <li>基于加数集合，和被拆分数，确定所有划分方案</li>
 *     <li>基于划分方案，和有限的加数，生成尽可能多的划分实例，一个划分方案可能有多个划分实例</li>
 * </ol>
 */
public interface TwoPhaseMaxPartitions extends MaxPartitions {

    /**
     * 新增接口，在给定划分方案的前提下，对加数集合进行拆分。
     *
     * @param addends 加数集合，key 是加数，value 是加数的数量
     * @param partitioned 被划分数，划分后的每个实例中包含若干加数，这些加数的和必须等于该值
     * @param partitionPlans 划分方案
     * @return 返回划分的最终结果
     */
    ArrayList<PartitionData> solveWithPartitionPlan(Map<Integer, Integer> addends, int partitioned, ArrayList<Map<Integer, Long>> partitionPlans);
}
