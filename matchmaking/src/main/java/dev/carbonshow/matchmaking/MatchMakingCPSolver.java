package dev.carbonshow.matchmaking;

/**
 * 基于 Constraint-Programming Satisfy 的匹配优化器
 */
public class MatchMakingCPSolver implements MatchMakingSolver {

    /**
     * 使用 google OR-tools 中的 cp-sat 模型，来解决匹配优化问题
     * <ul>
     *    <li>决策变量
     *       <ul>
     *           <li>x[i][j][k]，第 i 个匹配单元在第 j 个单局第 k 个队伍中的分配标记，1 表示分配，0 表示未分配</li>
     *           <li>y[j]，第 j 个单局的分配是否有效</li>
     *       </ul>
     *    </li>
     *    <li>约束条件
     *       <ul>
     *           <li>单局内各队伍人数，符合要求</li>
     *           <li>每个匹配单元最多只能出现在一个单局中</li>
     *           <li>满足匹配的其它约束条件，比如段位、最近对手回避等</li>
     *       </ul>
     *    </li>
     *    <li>优化目标
     *       <ul>
     *           <li>匹配质量更高，包含多种因素，比如：网络延迟低、职业覆盖全等等</li>
     *       </ul>
     *    </li>
     * </ul>
     * @param pool 匹配池，包含所有匹配单元的基础数据
     * @return 符合要求的匹配结果
     */
    @Override
    public MatchMakingResults solve(MatchMakingPool pool) {
        return null;
    }
}
