package graphma.compute.operator;

import data.suitesparse.SSDB;
import formats.Mtx;
import graphma.compute.operator.transform.GraphToSimpleGraph;
import graphma.compute.operator.transform.MtxToUndirectedGraph;
import magma.data.sequence.operator.DataSource;
import magma.data.sequence.operator.lazy.Append;
import magma.data.sequence.operator.lazy.Filter;
import magma.data.sequence.operator.lazy.Map;
import magma.data.sequence.operator.strict.ForNext;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

public class TransformTest {

    @Test
    public void test_mtx_to_graph() {
        ForNext.build(System.out::println)
                .compose(Map.build(r -> r.toString() + "\n" + "--------\n"))
                .compose(MtxToUndirectedGraph.of(DefaultEdge.class))
                .compose(Filter.build((Mtx.MTXFile mtx) -> mtx.lines() < 40 && mtx.lines() > 20))
                .apply(DataSource.of(SSDB.SMALL))
                .evaluate();
    }

    @Test
    public void test_graph_to_simplegraph() {
        ForNext.build(System.out::println)
                .compose(Map.build(r -> r.toString() + "\n" + "--------\n"))
                .compose(GraphToSimpleGraph.of(DefaultEdge.class))
                .compose(Filter.build((Graph<?, ?> g) -> !Utils.containsLoop(g)))
                .compose(MtxToUndirectedGraph.of(DefaultEdge.class))
                .compose(Filter.build((Mtx.MTXFile mtx) -> mtx.lines() < 40 && mtx.lines() > 20))
                .apply(DataSource.of(SSDB.SMALL))
                .evaluate();
    }
}
