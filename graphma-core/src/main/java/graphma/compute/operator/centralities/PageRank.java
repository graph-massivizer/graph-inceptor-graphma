package graphma.compute.operator.centralities;

import magma.data.sequence.operator.Operator;
import magma.data.sequence.pipeline.Composer;
import magma.data.sequence.pipeline.Pipe;
import magma.data.sequence.pipeline.Pipeline;
import org.jgrapht.Graph;

import java.util.Map;

/**
 * Provides utilities for computing PageRank centrality for nodes in a graph. PageRank measures
 * the importance of each node within the graph based on the link structure and is particularly
 * well-known for its use in ranking web pages in search engine results.
 *
 * <p>This class is part of a computational pipeline that allows for integration with other data processing
 * operations, facilitating complex workflows in graph analysis.</p>
 *
 * <p>Usage example:</p>
 * <pre>
 * {@code
 * ForNext.build(System.out::println)
 *                 .compose(Map.build(r -> r + "\n-------\n"))
 *                 .compose(PageRank.centrality())
 *                 .compose(MtxToUndirectedGraph.of(DefaultEdge.class))
 *                 .compose(Filter.build((Mtx.MTXFile mtx) -> mtx.lines() < 40 && mtx.lines() > 20))
 *                 .apply(DataSource.of(SSDB.SMALL))
 *                 .evaluate();
 * }
 * </pre>
 */
public enum PageRank {
    ;

    /**
     * Constructs a pipeline stage that computes the PageRank centrality for each vertex in a graph.
     * This method returns a composer that can be used to seamlessly integrate this computation as a stage
     * in a pipeline, providing a modular approach for assembling complex data processing pipelines.
     *
     * @param <V> the vertex type
     * @param <E> the edge type
     * @param <G> the graph type, extending JGraphT's Graph interface
     * @param <B> the output type, expected to be a Map from vertices to their PageRank centrality scores
     * @param <P> the type of the pipeline
     * @return a composer that constructs a pipeline stage for computing PageRank centrality
     */
    public static <V, E, G extends Graph<V, E>, B extends Map<V, Double>, P extends Pipeline<?, ?>>
    Composer<P, Pipeline.Stage<B, P>> centrality() {

        final class _PageRank extends Pipeline.AbstractBase<P> implements Pipeline.Stage<B, P> {
            private _PageRank(final P tail) {
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
                        final var centrality = new org.jgrapht.alg.scoring.PageRank<>(next);
                        this.yield((B) centrality.getScores());  // Yield the PageRank scores
                    }

                    @Override
                    public void close() {
                        super.close();
                    }
                }
                return new DegreeStage(out);
            }
        }
        return _PageRank::new;
    }
}

