package dev.carbonshow.algorithm.partition;

import java.util.Map;
import java.util.TreeMap;

public class MaxPartitionsUtils {

    /**
     * 将 Map 类型的加数按照加数升序方式排列，并将加数和加数的数量分别保存在两个数组中
     *
     * @param addends             加数集合
     * @param orderedAddends      输出的升序排列的加数
     * @param orderedAddendCounts 输出的加数数量，和 orderedAddends 一一对应
     */
    static void addendsToArray(Map<Integer, Integer> addends, int[] orderedAddends, int[] orderedAddendCounts) {
        // 对加数进行升序排序
        var sortedAddendMap = new TreeMap<>(addends);
        int i = 0;
        for (Integer addend : sortedAddendMap.keySet()) {
            orderedAddends[i] = addend;
            orderedAddendCounts[i] = sortedAddendMap.get(addend);
            i++;
        }
    }
}
