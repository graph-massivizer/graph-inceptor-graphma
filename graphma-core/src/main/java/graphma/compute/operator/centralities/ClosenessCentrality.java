package graphma.compute.operator.centralities;

import magma.data.sequence.operator.Operator;
import magma.data.sequence.pipeline.Composer;
import magma.data.sequence.pipeline.Pipe;
import magma.data.sequence.pipeline.Pipeline;
import org.jgrapht.Graph;

import java.util.Map;

/**
 * Provides a utility for computing closeness centrality for nodes in a graph.
 * Closeness centrality is a way of detecting nodes that are able to spread information efficiently through a graph.
 * The closeness centrality of a node measures its average distance to all other nodes in the graph.
 * Nodes with a high closeness score have the shortest distances to all other nodes, and thus lower overall distance scores.
 *
 * <p>This class uses the {@link org.jgrapht.alg.scoring.ClosenessCentrality} algorithm from JGraphT to calculate the centrality scores.</p>
 *
 * <p>Usage example:</p>
 * <pre>
 * {@code
 * ForNext.build(System.out::println)
 *                 .compose(Map.build(r -> r + "-\n-------\n"))
 *                 .compose(ClosenessCentrality.centrality())
 *                 .compose(MtxToUndirectedGraph.of(DefaultEdge.class))
 *                 .compose(Filter.build((Mtx.MTXFile mtx) -> mtx.lines() < 40 && mtx.lines() > 20))
 *                 .apply(DataSource.of(SSDB.SMALL))
 *                 .evaluate();
 * }
 * </pre>
 */
public enum ClosenessCentrality {
    ;

    /**
     * Constructs a pipeline stage that computes the closeness centrality for each vertex in a graph.
     * This method returns a composer that can be used to integrate this computation as a stage in a pipeline.
     *
     * @param <V> the vertex type
     * @param <E> the edge type
     * @param <G> the graph type, extending JGraphT's Graph interface
     * @param <B> the output type, expected to be a Map from vertices to their centrality scores
     * @param <P> the type of the pipeline
     * @return a composer that constructs a pipeline stage for computing closeness centrality
     */
    public static <V, E, G extends Graph<V, E>, B extends Map<V, Double>, P extends Pipeline<?, ?>>
    Composer<P, Pipeline.Stage<B, P>> centrality() {

        final class Centrality extends Pipeline.AbstractBase<P> implements Pipeline.Stage<B, P> {
            private Centrality(final P tail) {
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
                        final var centrality = new org.jgrapht.alg.scoring.ClosenessCentrality<>(next);
                        this.yield((B) centrality.getScores());  // Yield the closeness centrality scores
                    }

                    @Override
                    public void close() {
                        super.close();
                    }
                }
                return new DegreeStage(out);
            }
        }
        return Centrality::new;
    }
}

