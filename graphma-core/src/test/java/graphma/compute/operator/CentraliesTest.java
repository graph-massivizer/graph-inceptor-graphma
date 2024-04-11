package graphma.compute.operator;

import data.suitesparse.SSDB;
import graphma.compute.operator.centralities.*;
import graphma.compute.operator.transform.MtxToUndirectedGraph;
import magma.data.sequence.operator.DataSource;
import magma.data.sequence.operator.lazy.Filter;
import magma.data.sequence.operator.lazy.Map;
import magma.data.sequence.operator.strict.ForNext;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

public class CentraliesTest {

    @Test
    public void test_betweeness_centrality() {
        ForNext.build(System.out::println)
                .compose(Map.build(r -> r + "\n-------\n"))
                .compose(BetweennessCentrality.centrality())
                .compose(MtxToUndirectedGraph.of(DefaultEdge.class))
                .compose(Filter.build((SSDB.MTXFile mtx) -> mtx.lines() < 40 && mtx.lines() > 20))
                .apply(DataSource.of(SSDB.SMALL))
                .evaluate();
    }

    @Test
    public void test_degree_centrality() {
        ForNext.build(System.out::println)
                .compose(Map.build(r -> r + "\n-------\n"))
                .compose(DegreeCentrality.centrality())
                .compose(MtxToUndirectedGraph.of(DefaultEdge.class))
                .compose(Filter.build((SSDB.MTXFile mtx) -> mtx.lines() < 40 && mtx.lines() > 20))
                .apply(DataSource.of(SSDB.SMALL))
                .evaluate();
    }

    @Test
    public void test_closeness_centrality() {
        ForNext.build(System.out::println)
                .compose(Map.build(r -> r + "-\n-------\n"))
                .compose(ClosenessCentrality.centrality())
                .compose(MtxToUndirectedGraph.of(DefaultEdge.class))
                .compose(Filter.build((SSDB.MTXFile mtx) -> mtx.lines() < 40 && mtx.lines() > 20))
                .apply(DataSource.of(SSDB.SMALL))
                .evaluate();
    }

    @Test
    public void test_clustering_coefficient() {
        ForNext.build(System.out::println)
                .compose(Map.build(r -> r + "\n-------\n"))
                .compose(ClusteringCoefficient.centrality())
                .compose(MtxToUndirectedGraph.of(DefaultEdge.class))
                .compose(Filter.build((SSDB.MTXFile mtx) -> mtx.lines() < 40 && mtx.lines() > 20))
                .apply(DataSource.of(SSDB.SMALL))
                .evaluate();
    }

    @Test
    public void test_eigenvector_centrality() {
        ForNext.build(System.out::println)
                .compose(Map.build(r -> r + "\n-------\n"))
                .compose(EigenvectorCentrality.centrality())
                .compose(MtxToUndirectedGraph.of(DefaultEdge.class))
                .compose(Filter.build((SSDB.MTXFile mtx) -> mtx.lines() < 40 && mtx.lines() > 20))
                .apply(DataSource.of(SSDB.SMALL))
                .evaluate();
    }

    @Test
    public void test_katz_centrality() {
        ForNext.build(System.out::println)
                .compose(Map.build(r -> r + "\n-------\n"))
                .compose(KatzCentrality.centrality())
                .compose(MtxToUndirectedGraph.of(DefaultEdge.class))
                .compose(Filter.build((SSDB.MTXFile mtx) -> mtx.lines() < 40 && mtx.lines() > 20))
                .apply(DataSource.of(SSDB.SMALL))
                .evaluate();
    }

    @Test
    public void test_page_rank() {
        ForNext.build(System.out::println)
                .compose(Map.build(r -> r + "\n-------\n"))
                .compose(PageRank.centrality())
                .compose(MtxToUndirectedGraph.of(DefaultEdge.class))
                .compose(Filter.build((SSDB.MTXFile mtx) -> mtx.lines() < 40 && mtx.lines() > 20))
                .apply(DataSource.of(SSDB.SMALL))
                .evaluate();
    }
}
