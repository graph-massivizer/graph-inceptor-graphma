package graphma.compute.operator.clustering;

import magma.data.sequence.operator.Operator;
import magma.data.sequence.pipeline.Composer;
import magma.data.sequence.pipeline.Pipe;
import magma.data.sequence.pipeline.Pipeline;
import org.jgrapht.alg.scoring.Coreness;

/**
 * Provides utilities for calculating the k-core decomposition of graphs. A k-core is a maximal
 * subgraph that contains vertices of degree k or more. This class is used in computational pipelines
 * to integrate k-core computations seamlessly with other data operations, enabling sophisticated
 * analyses of graph structures.
 *
 * <p>Usage example:</p>
 * <pre>
 * {@code
 * ForNext.build(System.out::println)
 *                 .compose(Map.build(r -> r + "\n--------\n"))
 *                 .compose(KCores.textify())
 *                 .compose(KCores.cluster())
 *                 .compose(GraphToSimpleGraph.of(DefaultEdge.class))
 *                 .compose(Filter.build((Graph<?, ?> g) -> !Utils.containsLoop(g)))
 *                 .compose(MtxToUndirectedGraph.of(DefaultEdge.class))
 *                 .compose(Filter.build((Mtx.MTXFile mtx) -> mtx.lines() < 40 && mtx.lines() > 20))
 *                 .apply(DataSource.of(SSDB.SMALL))
 *                 .evaluate();
 * }
 * </pre>
 */
public enum KCores {
    ;

    /**
     * Creates a pipeline stage that computes the k-core decomposition of a graph.
     * This method returns a composer that can be integrated as a stage in data processing pipelines,
     * facilitating the modular construction of data processing workflows.
     *
     * @param <V> the vertex type
     * @param <E> the edge type
     * @param <G> the graph type, extending JGraphT's Graph interface
     * @param <B> the output type, typically Coreness which encapsulates the k-core decomposition
     * @param <P> the type of the pipeline
     * @return a composer that constructs a pipeline stage for computing the k-core decomposition
     */
    public static <V, E, G extends org.jgrapht.Graph<V, E>, B extends Coreness<V, E>, P extends Pipeline<?, ?>>
    Composer<P, Pipeline.Stage<B, P>> cluster() {

        final class _KCores extends Pipeline.AbstractBase<P> implements Pipeline.Stage<B, P> {
            private _KCores(final P tail) {
                super(tail);
            }

            @Override
            public Pipe<?> apply(final Pipe<B> out) {
                final class LabelStage extends Operator.Transform<G, B> {
                    private LabelStage(final Pipe<B> out) {
                        super(out);
                    }

                    @Override
                    public void open(long count) { super.open(count); }

                    @Override
                    public void onNext(final long index, final G next) {
                        final var clustering = new Coreness<V, E>(next);
                        clustering.getDegeneracy();
                        this.yield((B) clustering);
                    }

                    @Override
                    public void close() {
                        super.close();
                    }
                }
                return new LabelStage(out);
            }
        }
        return _KCores::new;
    }

    /**
     * Converts the numerical coreness scores to a text format for easier visualization or further processing.
     * This pipeline stage takes a Coreness object and transforms it into a textual representation.
     *
     * @param <V> the vertex type
     * @param <E> the edge type
     * @param <C> the Coreness type containing the coreness scores
     * @param <B> the output type, set as a String
     * @param <P> the type of the pipeline
     * @return a composer that constructs a pipeline stage for transforming coreness data into textual form
     */
    public static <V, E, C extends Coreness<V, E>, B extends String, P extends Pipeline<?, ?>>
    Composer<P, Pipeline.Stage<B, P>> textify() {

        final class CorenessStage extends Pipeline.AbstractBase<P> implements Pipeline.Stage<B, P> {
            private CorenessStage(final P tail) { super(tail); }

            @Override
            public Pipe<?> apply(final Pipe<B> out) {
                final class CorenessTransform extends Operator.Transform<C, B> {
                    private CorenessTransform(final Pipe<B> out) {
                        super(out);
                    }

                    @Override
                    public void open(long count) { super.open(count); }

                    @Override
                    public void onNext(final long index, final C next) {
                        this.yield((B) String.valueOf(next.getScores()));
                    }

                    @Override
                    public void close() {
                        super.close();
                    }
                }
                return new CorenessTransform(out);
            }
        }
        return CorenessStage::new;
    }
}

