package scenario_2;

import data.suitesparse.SSDB;
import formats.Mtx;
import graphma.compute.operator.clustering.KCores;
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

import java.util.Comparator;

/**
 * More examples in {@link graphma.compute.operator} tests
 */
public class ExampleTest {

    /**
     * Checks if the graph contains a loop (self-loop).
     *
     * @param graph The graph to check for loops.
     * @param <V> The vertex type.
     * @param <E> The edge type.
     * @return true if there is at least one loop in the graph, false otherwise.
     */
    static <V, E> boolean containsLoop(Graph<V, E> graph) {
        for (E edge : graph.edgeSet()) {
            V source = graph.getEdgeSource(edge);
            V target = graph.getEdgeTarget(edge);
            if (source.equals(target)) {
                return true;
            }
        }
        return false;
    }

    /**
     * We have to assemble the pipeline using the builder
     * contravariant. This is due to the existence of
     * custom operators. When using pure magma operators
     * we can use the seq abstraction (See method below)
     */
    @Test
    public void test_kcore_clustering() {
        ForNext.build(System.out::println)
                .compose(Map.build(r -> r + "\n--------\n"))
                .compose(KCores.textify())
                .compose(KCores.cluster())
                .compose(GraphToSimpleGraph.of(DefaultEdge.class))
                .compose(Filter.build((Graph<?, ?> g) -> !containsLoop(g)))
                .compose(MtxToUndirectedGraph.of(DefaultEdge.class))
                .compose(Filter.build((Mtx.MTXFile mtx) -> mtx.lines() < 40 && mtx.lines() > 20))
                .apply(DataSource.of(SSDB.SMALL))
                .evaluate();
    }

    /**
     * This is an exampe for the sequence api.
     */
    @Test
    public void test_sequence() {
        var ds = DataSource.of("A", "B", "C", "D", "E", "F");
        var count = Seq.of(ds)
                .drop(2)
                .filterNot(e -> e.equals("E"))
                .sort(Comparator.reverseOrder())
                .append("J", "K" , "L")
                .peek(System.out::println)
                .count();
        System.out.println("FOUND " + count + " ELEMENTS");
    }
}
