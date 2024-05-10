package graphma.compute.operator;

import data.suitesparse.SSDB;
import formats.Mtx;
import graphma.compute.operator.metrics.Diameter;
import graphma.compute.operator.metrics.GraphCenter;
import graphma.compute.operator.metrics.GraphPeriphery;
import graphma.compute.operator.metrics.Radius;
import graphma.compute.operator.transform.MtxToUndirectedGraph;
import magma.data.sequence.operator.DataSource;
import magma.data.sequence.operator.lazy.Filter;
import magma.data.sequence.operator.lazy.Map;
import magma.data.sequence.operator.strict.ForNext;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

/**
 * These are not really tests. More demos with printouts to check if stuff works.
 *
 * Actual unit tests will follow
 *
 */
public class MetricsTest {

    @Test
    public void test_diameter() {
        ForNext.build(System.out::println)
                .compose(Map.build(r -> r + "\n--------\n"))
                .compose(Diameter.diameter())
                .compose(MtxToUndirectedGraph.of(DefaultEdge.class))
                .compose(Filter.build((Mtx.MTXFile mtx) -> mtx.lines() < 40 && mtx.lines() > 20))
                .apply(DataSource.of(SSDB.SMALL))
                .evaluate();
    }

    @Test
    public void test_graph_center() {
        ForNext.build(System.out::println)
                .compose(Map.build(r -> r + "\n--------\n"))
                .compose(GraphCenter.center())
                .compose(MtxToUndirectedGraph.of(DefaultEdge.class))
                .compose(Filter.build((Mtx.MTXFile mtx) -> mtx.lines() < 40 && mtx.lines() > 20))
                .apply(DataSource.of(SSDB.SMALL))
                .evaluate();
    }

    @Test
    public void test_graph_periphery() {
        ForNext.build(System.out::println)
                .compose(Map.build(r -> r + "\n--------\n"))
                .compose(GraphPeriphery.periphery())
                .compose(MtxToUndirectedGraph.of(DefaultEdge.class))
                .compose(Filter.build((Mtx.MTXFile mtx) -> mtx.lines() < 40 && mtx.lines() > 20))
                .apply(DataSource.of(SSDB.SMALL))
                .evaluate();
    }

    @Test
    public void test_radius() {
        ForNext.build(System.out::println)
                .compose(Map.build(r -> r + "\n--------\n"))
                .compose(Radius.radius())
                .compose(MtxToUndirectedGraph.of(DefaultEdge.class))
                .compose(Filter.build((Mtx.MTXFile mtx) -> mtx.lines() < 40 && mtx.lines() > 20))
                .apply(DataSource.of(SSDB.SMALL))
                .evaluate();
    }
}
