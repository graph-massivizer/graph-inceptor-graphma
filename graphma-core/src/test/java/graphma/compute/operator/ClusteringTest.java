package graphma.compute.operator;

import data.suitesparse.SSDB;
import formats.Mtx;
import graphma.compute.operator.clustering.ConnectedComponent;
import graphma.compute.operator.clustering.KCores;
import graphma.compute.operator.clustering.LabelPropagation;
import graphma.compute.operator.transform.GraphToSimpleGraph;
import graphma.compute.operator.transform.MtxToUndirectedGraph;
import magma.data.Seq;
import magma.data.sequence.operator.DataSource;
import magma.data.sequence.operator.lazy.Filter;
import magma.data.sequence.operator.lazy.Map;
import magma.data.sequence.operator.strict.ForNext;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

/**
 * These are not really tests. More demos with printouts to check if stuff works.
 *
 * Actual unit tests will follow
 *
 */
final class ClusteringTest {

    @Test
    public void test_label_propagation() {
        ForNext.build(System.out::println)
                .compose(Map.build(r -> r + "--------\n"))
                .compose(LabelPropagation.textify())
                .compose(LabelPropagation.cluster())
                .compose(MtxToUndirectedGraph.of(DefaultEdge.class))
                .compose(Filter.build((Mtx.MTXFile mtx) -> mtx.lines() < 40 && mtx.lines() > 20))
                .apply(DataSource.of(SSDB.SMALL))
                .evaluate();
    }

    @Test
    public void test_kcore_clustering() {
        ForNext.build(System.out::println)
                .compose(Map.build(r -> r + "\n--------\n"))
                .compose(KCores.textify())
                .compose(KCores.cluster())
                .compose(GraphToSimpleGraph.of(DefaultEdge.class))
                .compose(Filter.build((Graph<?, ?> g) -> !Utils.containsLoop(g)))
                .compose(MtxToUndirectedGraph.of(DefaultEdge.class))
                .compose(Filter.build((Mtx.MTXFile mtx) -> mtx.lines() < 40 && mtx.lines() > 20))
                .apply(DataSource.of(SSDB.SMALL))
                .evaluate();
    }

    @Test
    public void test_connected_components() {
        ForNext.build(System.out::println)
                .compose(Map.build(r -> r + "\n--------\n"))
                .compose(ConnectedComponent.cluster())
                .compose(MtxToUndirectedGraph.of(DefaultEdge.class))
                .compose(Filter.build((Mtx.MTXFile mtx) -> mtx.lines() < 40 && mtx.lines() > 20))
                .apply(DataSource.of(SSDB.SMALL))
                .evaluate();
    }
}
