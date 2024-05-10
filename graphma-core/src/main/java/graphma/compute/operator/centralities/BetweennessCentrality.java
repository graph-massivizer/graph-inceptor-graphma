package graphma.compute.operator.centralities;

import magma.data.sequence.operator.Operator;
import magma.data.sequence.pipeline.Composer;
import magma.data.sequence.pipeline.Pipe;
import magma.data.sequence.pipeline.Pipeline;
import org.jgrapht.Graph;

import java.util.Map;

/**
 * Provides a utility to compute betweenness centrality for nodes in a graph.
 * Betweenness centrality is a measure of centrality in a graph based on shortest paths.
 * For every pair of vertices in a connected graph, there are at least one shortest path between
 * the vertices such that either the number of edges that the path passes through (for unweighted graphs)
 * or the sum of the weights of the edges (for weighted graphs) is minimized.
 * The betweenness centrality for each vertex is the number of these shortest paths that pass through the vertex.
 *
 * <p>This class leverages JGraphT's BetweennessCentrality algorithm to calculate the centrality scores for each vertex.</p>
 *
 * <p>Usage example:</p>
 * <pre>
 * {@code
 * ForNext.build(System.out::println)
 *                 .compose(Map.build(r -> r + "\n-------\n"))
 *                 .compose(BetweennessCentrality.centrality())
 *                 .compose(MtxToUndirectedGraph.of(DefaultEdge.class))
 *                 .compose(Filter.build((Mtx.MTXFile mtx) -> mtx.lines() < 40 && mtx.lines() > 20))
 *                 .apply(DataSource.of(SSDB.SMALL))
 *                 .evaluate();
 * }
 * </pre>
 */
public enum BetweennessCentrality {
    ;

    /**
     * Constructs a pipeline stage that computes the betweenness centrality for each vertex in a graph.
     * The stage uses the {@link org.jgrapht.alg.scoring.BetweennessCentrality} algorithm provided by JGraphT.
     *
     * @param <V> the vertex type
     * @param <E> the edge type
     * @param <G> the graph type
     * @param <B> the output type, expected to be a Map from vertices to their centrality scores
     * @param <P> the type of the pipeline
     * @return a composer that constructs a pipeline stage for computing betweenness centrality
     */
    public static <V, E, G extends Graph<V, E>, B extends Map<V, Double>, P extends Pipeline<?, ?>>
    Composer<P, Pipeline.Stage<B, P>> centrality() {

        final class _BetweennessCentrality extends Pipeline.AbstractBase<P> implements Pipeline.Stage<B, P> {
            private _BetweennessCentrality(final P tail) {
                super(tail);
            }

            @Override
            public Pipe<?> apply(final Pipe<B> out) {
                final class LabelStage extends Operator.Transform<G, B> {
                    private LabelStage(final Pipe<B> out) {
                        super(out);
                    }

                    @Override
                    public void open(long count) {
                        super.open(count);
                    }

                    @Override
                    public void onNext(final long index, final G next) {
                        final var centrality = new org.jgrapht.alg.scoring.BetweennessCentrality<>(next);
                        this.yield((B) centrality.getScores());  // Yield the centrality scores
                    }

                    @Override
                    public void close() {
                        super.close();
                    }
                }
                return new LabelStage(out);
            }
        }
        return _BetweennessCentrality::new;
    }
}

