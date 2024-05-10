package graphma.compute.operator.centralities;

import magma.data.sequence.operator.Operator;
import magma.data.sequence.pipeline.Composer;
import magma.data.sequence.pipeline.Pipe;
import magma.data.sequence.pipeline.Pipeline;
import org.jgrapht.Graph;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides utilities for computing the degree centrality for nodes in a graph.
 * Degree centrality is a straightforward metric that measures the number of edges connected to a vertex.
 * This metric can help identify highly connected nodes within a network, which are often considered to be influential.
 *
 * <p>This implementation integrates with a pipeline processing framework, allowing degree centrality
 * calculations to be composed as part of larger data processing operations.</p>
 *
 * <p>Usage example:</p>
 * <pre>
 * {@code
 * ForNext.build(System.out::println)
 *                 .compose(Map.build(r -> r + "\n-------\n"))
 *                 .compose(DegreeCentrality.centrality())
 *                 .compose(MtxToUndirectedGraph.of(DefaultEdge.class))
 *                 .compose(Filter.build((Mtx.MTXFile mtx) -> mtx.lines() < 40 && mtx.lines() > 20))
 *                 .apply(DataSource.of(SSDB.SMALL))
 *                 .evaluate();
 * }
 * </pre>
 */
public enum DegreeCentrality {
    ;

    /**
     * Constructs a pipeline stage that computes the degree centrality for each vertex in a graph.
     * This method returns a composer that can be used to integrate this computation as a stage in a pipeline.
     *
     * @param <V> the vertex type
     * @param <E> the edge type
     * @param <G> the graph type, extending JGraphT's Graph interface
     * @param <B> the output type, expected to be a Map from vertices to their degree centrality scores
     * @param <P> the type of the pipeline
     * @return a composer that constructs a pipeline stage for computing degree centrality
     */
    public static <V, E, G extends Graph<V, E>, B extends Map<V, Double>, P extends Pipeline<?, ?>>
    Composer<P, Pipeline.Stage<B, P>> centrality() {

        final class _DegreeCentrality extends Pipeline.AbstractBase<P> implements Pipeline.Stage<B, P> {
            private _DegreeCentrality(final P tail) {
                super(tail);
            }

            @Override
            public Pipe<?> apply(final Pipe<B> out) {
                final class DegreeStage extends Operator.Transform<G, B> {
                    private DegreeStage(final Pipe<B> out) {
                        super(out);
                    }

                    @Override
                    public void open(long count) {
                        super.open(count);
                    }

                    @Override
                    public void onNext(final long index, final G next) {
                        Map<V, Double> degreeCentrality = new HashMap<>();
                        for (V vertex : next.vertexSet()) {
                            double degree = next.degreeOf(vertex);  // Get the degree of the vertex
                            degreeCentrality.put(vertex, degree);  // Store it in the map
                        }
                        this.yield((B) degreeCentrality);  // Yield the degree centrality scores
                    }

                    @Override
                    public void close() {
                        super.close();
                    }
                }
                return new DegreeStage(out);
            }
        }
        return _DegreeCentrality::new;
    }
}
