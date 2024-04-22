package graphma.compute.operator.centralities;

import magma.data.sequence.operator.Operator;
import magma.data.sequence.pipeline.Composer;
import magma.data.sequence.pipeline.Pipe;
import magma.data.sequence.pipeline.Pipeline;
import org.jgrapht.Graph;

import java.util.Map;

/**
 * Provides a utility for computing the clustering coefficient for nodes in a graph.
 * The clustering coefficient of a node provides a measure of how complete the neighborhood of a node is
 * by calculating how close its neighbors are to being a complete graph (clique).
 *
 * <p>This class uses the {@link org.jgrapht.alg.scoring.ClusteringCoefficient} algorithm from JGraphT to calculate
 * the clustering scores, which represent the likelihood that two neighbors of a node are also neighbors with each other.</p>
 *
 * <p>Usage example:</p>
 * <pre>
 * {@code
 * ForNext.build(System.out::println)
 *                 .compose(Map.build(r -> r + "\n-------\n"))
 *                 .compose(ClusteringCoefficient.centrality())
 *                 .compose(MtxToUndirectedGraph.of(DefaultEdge.class))
 *                 .compose(Filter.build((Mtx.MTXFile mtx) -> mtx.lines() < 40 && mtx.lines() > 20))
 *                 .apply(DataSource.of(SSDB.SMALL))
 *                 .evaluate();
 * }
 * </pre>
 */
public enum ClusteringCoefficient {
    ;

    /**
     * Constructs a pipeline stage that computes the clustering coefficient for each vertex in a graph.
     * This method returns a composer that can be used to integrate this computation as a stage in a pipeline.
     *
     * @param <V> the vertex type
     * @param <E> the edge type
     * @param <G> the graph type, extending JGraphT's Graph interface
     * @param <B> the output type, expected to be a Map from vertices to their clustering coefficient scores
     * @param <P> the type of the pipeline
     * @return a composer that constructs a pipeline stage for computing the clustering coefficient
     */
    public static <V, E, G extends Graph<V, E>, B extends Map<V, Double>, P extends Pipeline<?, ?>>
    Composer<P, Pipeline.Stage<B, P>> centrality() {

        final class _ClusteringCoefficient extends Pipeline.AbstractBase<P> implements Pipeline.Stage<B, P> {
            private _ClusteringCoefficient(final P tail) {
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
                        final var clusteringCoefficient = new org.jgrapht.alg.scoring.ClusteringCoefficient<>(next);
                        this.yield((B) clusteringCoefficient.getScores());  // Yield the clustering coefficient scores
                    }

                    @Override
                    public void close() {
                        super.close();
                    }
                }
                return new DegreeStage(out);
            }
        }
        return _ClusteringCoefficient::new;
    }
}

