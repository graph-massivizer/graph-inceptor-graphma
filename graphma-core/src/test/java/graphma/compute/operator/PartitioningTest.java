package graphma.compute.operator;

import data.suitesparse.SSDB;
import formats.Mtx;
import graphma.compute.operator.partitioning.BipartitePartitioning;
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
public class PartitioningTest {

    @Test
    public void test_bipartite_partitioning() {
        ForNext.build(System.out::println)
                .compose(Map.build(r -> r + "--------\n"))
                .compose(BipartitePartitioning.partition())
                .compose(MtxToUndirectedGraph.of(DefaultEdge.class))
                .compose(Filter.build((Mtx.MTXFile mtx) -> mtx.lines() < 40 && mtx.lines() > 20))
                .apply(DataSource.of(SSDB.SMALL))
                .evaluate();
    }
}
