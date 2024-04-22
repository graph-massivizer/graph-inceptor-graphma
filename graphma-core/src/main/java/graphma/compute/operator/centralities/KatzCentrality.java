package graphma.compute.operator.centralities;

import magma.data.sequence.operator.Operator;
import magma.data.sequence.pipeline.Composer;
import magma.data.sequence.pipeline.Pipe;
import magma.data.sequence.pipeline.Pipeline;
import org.jgrapht.Graph;

import java.util.Map;

/**
 * Provides utilities for computing Katz centrality for nodes in a graph. Katz centrality measures
 * the centrality of a node within a graph based on the total number of walks between pairs of nodes,
 * exponentially attenuated by the length of the walk.
 *
 * <p>This class is designed to integrate Katz centrality calculation within a data processing pipeline,
 * allowing it to be used seamlessly with other data operations.</p>
 *
 * <p>Usage example:</p>
 * <pre>
 * {@code
 *   ForNext.build(System.out::println)
 *                 .compose(Map.build(r -> r + "\n-------\n"))
 *                 .compose(KatzCentrality.centrality())
 *                 .compose(MtxToUndirectedGraph.of(DefaultEdge.class))
 *                 .compose(Filter.build((Mtx.MTXFile mtx) -> mtx.lines() < 40 && mtx.lines() > 20))
 *                 .apply(DataSource.of(SSDB.SMALL))
 *                 .evaluate();
 * }
 * </pre>
 */
public enum KatzCentrality {
    ;

    /**
     * Constructs a pipeline stage that computes the Katz centrality for each vertex in a graph.
     * This method returns a composer that can be used to integrate this computation as a stage in a pipeline,
     * providing a flexible setup for combining with other stages or operations.
     *
     * @param <V> the vertex type
     * @param <E> the edge type
     * @param <G> the graph type, extending JGraphT's Graph interface
     * @param <B> the output type, expected to be a Map from vertices to their Katz centrality scores
     * @param <P> the type of the pipeline
     * @return a composer that constructs a pipeline stage for computing Katz centrality
     */
    public static <V, E, G extends Graph<V, E>, B extends Map<V, Double>, P extends Pipeline<?, ?>>
    Composer<P, Pipeline.Stage<B, P>> centrality() {

        final class _KatzCentrality extends Pipeline.AbstractBase<P> implements Pipeline.Stage<B, P> {
            private _KatzCentrality(final P tail) {
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
                        final var centrality = new org.jgrapht.alg.scoring.KatzCentrality<>(next);
                        this.yield((B) centrality.getScores());  // Yield the centrality scores
                    }

                    @Override
                    public void close() {
                        super.close();
                    }
                }
                return new DegreeStage(out);
            }
        }
        return _KatzCentrality::new;
    }
}

