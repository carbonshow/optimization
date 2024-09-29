package dev.carbonshow.matchmaking.pool;

import dev.carbonshow.matchmaking.config.MatchMakingCriteria;
import dev.carbonshow.matchmaking.config.TimeVaryingConfig;
import dev.carbonshow.matchmaking.solver.DefaultMatchUnitOperator;
import dev.carbonshow.matchmaking.solver.MatchUnitOperator;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;

import java.util.ArrayList;

public class MatchMakingPoolGraph extends MatchMakingPoolBasic {
    // 建立匹配单元的图结构，如果两个单元可以匹配则存在边
    // 显然，两个单元是否可以进入同一场单局，和时间因素密切相关，因此注意按时更新
    private final Graph<MatchUnit, DefaultEdge> graph;

    // 用于 MatchUnit 的计算
    private final MatchUnitOperator matchUnitOperator;
    TimeVaryingConfig timeVaryingConfig;

    /**
     * 从 match unit 数组中建立图，默认会使用当前 Epoch 时间驱动，匹配单元时变参数的更新
     *
     * @param criteria 匹配参数配置
     * @param name     匹配池的名称
     */
    public MatchMakingPoolGraph(MatchMakingCriteria criteria, String name, TimeVaryingConfig timeVaryingConfig) {
        this(criteria, name, new DefaultMatchUnitOperator(criteria, timeVaryingConfig));
        this.timeVaryingConfig = timeVaryingConfig;
    }

    /**
     * 从 match unit 数组中建立图，并更新时变参数
     *
     * @param criteria 匹配参数配置
     * @param name     匹配池的名称
     * @param operator 匹配单元的计算逻辑
     */
    public MatchMakingPoolGraph(MatchMakingCriteria criteria, String name, MatchUnitOperator operator) {
        super(criteria, name);
        graph = GraphTypeBuilder.<MatchUnit, DefaultEdge>undirected().allowingMultipleEdges(false).allowingSelfLoops(false)
                .edgeClass(DefaultEdge.class).weighted(true).buildGraph();
        matchUnitOperator = operator;
    }

    /**
     * 向匹配池中添加新的匹配单元，如果已经存在则不做任何改变
     * <b>注意：并不添加边，因为边表示可进入同一单局的关系，且随时间变化，按需更新</b>
     *
     * @param matchUnit 待添加的匹配单元
     * @return true 说明池子中该单元并不存在，添加成功；否则失败，说明已经存在
     */
    @Override
    public boolean addMatchUnit(MatchUnit matchUnit) {
        if (graph.addVertex(matchUnit)) {
            return super.addMatchUnit(matchUnit);
        }
        return false;
    }

    /**
     * 根据 matchUnitId 删除对应的匹配单元，如果存在边也会自动删除
     *
     * @param matchUnitId 匹配单元的唯一 ID
     * @return 返回实际删除的数量
     */
    @Override
    public boolean removeMatchUnit(long matchUnitId) {
        var unit = getMatchUnit(matchUnitId);
        if (unit != null) {
            graph.removeVertex(unit);
            return super.removeMatchUnit(matchUnitId);
        }
        return false;
    }

    @Override
    public void update(long currentTimestamp) {
        for (var unit : units.values()) {
            unit.timeVaryingParameters().update(currentTimestamp, this.timeVaryingConfig);
        }
    }

    /**
     * 在获取 units 列表时，需要更新时变参数
     */
    @Override
    public MatchUnit[] matchUnits() {
        return super.matchUnits();
    }

    @Override
    public ArrayList<ArrayList<Integer>> getMutableExclusiveMatchUnits(MatchUnit[] units) {
        ArrayList<ArrayList<Integer>> mutableExclusiveMatchUnits = new ArrayList<>();
        for (int i = 0; i < units.length; i++) {
            ArrayList<Integer> exclusiveUnits = new ArrayList<>();
            for (int j = i + 1; j < units.length; j++) {
                if (!matchUnitOperator.isFitOneTeam(units[i], units[j])) {
                    exclusiveUnits.add(j);
                }
            }
            mutableExclusiveMatchUnits.add(exclusiveUnits);
        }
        return mutableExclusiveMatchUnits;
    }

    public Graph<MatchUnit, DefaultEdge> getGraph() {
        return graph;
    }

    private void test() {
    }
}
