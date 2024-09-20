package dev.carbonshow.matchmaking;

import java.security.InvalidParameterException;

/**
 * 匹配单元操作接口
 */
public interface MatchUnitOperator {
    /**
     * 判断两个匹配单元是否互斥，互斥意味着不能进入同一场单局
     * @param unit1 匹配单元 1
     * @param unit2 匹配单元 2
     * @return true 表示互斥，不能进入同一单局；false 表示可以进入同一单局
     */
    boolean isMutualExclusive(MatchUnit unit1, MatchUnit unit2);

    /**
     * 将两个匹配单元进行合并
     *
     * @param unit1 匹配单元 1
     * @param unit2 匹配单元 2
     * @return 返回合并后的新匹配单元，如果合并出错抛出异常
     */
    MatchUnit merge(MatchUnit unit1, MatchUnit unit2) throws InvalidParameterException;

    /**
     * 计算两个匹配单元的预期胜率
     * @param unit1 匹配单元 1
     * @param unit2 匹配单元 2
     * @return 返回匹配单元 1 相对于匹配单元 2 的获胜概率
     */
    double getWinProbability(MatchUnit unit1, MatchUnit unit2);
}
