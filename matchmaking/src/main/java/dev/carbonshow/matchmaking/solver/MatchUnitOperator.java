package dev.carbonshow.matchmaking.solver;

import dev.carbonshow.matchmaking.pool.MatchUnit;

import java.security.InvalidParameterException;
import java.util.BitSet;

/**
 * 匹配单元操作接口
 */
public interface MatchUnitOperator {
    /**
     * 判断两个匹配单元是否能进入同一场单局的同一个队伍中
     * @param unit1 匹配单元 1
     * @param unit2 匹配单元 2
     * @return true 表示能进入同一单局；false 表示不可进入同一单局
     */
    boolean isFitOneTeam(MatchUnit unit1, MatchUnit unit2);

    /**
     * 判断给定的几个 team 是否可以进入同一单局内
     * @param team1 队伍 1，自身一定是可用的
     * @param team2 队伍 2，自身一定是可用的
     * @return true 表示这些队伍可以进入同一单局；否则表示不能进入同一单局
     */
    boolean isFitOneGame(FeasibleTeam team1, FeasibleTeam team2);

    /**
     * 将撮合到同一队的多个匹配单元转化为队伍可行解，更新时变参数
     *
     * @param units       匹配单元数组
     * @param unitMembers 队伍中所含各匹配单元在数组中的索引集合，用 BitSet 的形式表示
     * @param currentTimestamp 当前时间戳，单位业务自定义，保持一致即可
     * @return 返回合并后的可用队伍，如果合并出错抛出异常
     */
    FeasibleTeam mergeUnitsToTeam(MatchUnit[] units, BitSet unitMembers, long currentTimestamp) throws InvalidParameterException;

    /**
     * 计算两个匹配单元的预期胜率
     * @param unit1 匹配单元 1
     * @param unit2 匹配单元 2
     * @return 返回匹配单元 1 相对于匹配单元 2 的获胜概率
     */
    double getWinProbability(MatchUnit unit1, MatchUnit unit2);


}
