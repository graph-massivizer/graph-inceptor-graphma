package graphma.compute.operator.metrics;

import magma.data.sequence.operator.Operator;
import magma.data.sequence.pipeline.Composer;
import magma.data.sequence.pipeline.Pipe;
import magma.data.sequence.pipeline.Pipeline;
import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.GraphMeasurer;

import java.util.Set;

/**
 * Provides a method to calculate the center of graphs within a data processing pipeline. The center of a graph
 * is the set of nodes with the minimum eccentricity, which is the maximum distance to any other node in the graph.
 * This class allows the integration of graph center computations into data processing pipelines, enhancing
 * the ability to analyze graph structures dynamically within the Graphma framework.
 *
 * <p>Usage example:</p>
 * <pre>
 * {@code
 * ForNext.build(System.out::println)
 *                 .compose(Map.build(r -> r + "\n--------\n"))
 *                 .compose(GraphCenter.center())
 *                 .compose(MtxToUndirectedGraph.of(DefaultEdge.class))
 *                 .compose(Filter.build((Mtx.MTXFile mtx) -> mtx.lines() < 40 && mtx.lines() > 20))
 *                 .apply(DataSource.of(SSDB.SMALL))
 *                 .evaluate();
 * }
 * </pre>
 */
public enum GraphCenter {
    ;
    // Enum structure used as a namespace to hold utility methods.

    /**
     * Creates a pipeline stage that computes the center of a graph.
     * The graph center includes all nodes with the minimum distance to the farthest node in the graph.
     * This method returns a composer that facilitates seamless integration as a stage in data processing pipelines,
     * supporting modular construction of complex workflows for graph analysis.
     *
     * @param <V> the vertex type
     * @param <E> the edge type
     * @param <G> the graph type, extending JGraphT's Graph interface
     * @param <B> the expected type of the output, typically a set of vertices
     * @param <P> the type of the pipeline context
     * @return a composer that constructs a pipeline stage for computing the graph center
     */
    public static <V, E, G extends Graph<V, E>, B extends Set<V>, P extends Pipeline<?, ?>>
    Composer<P, Pipeline.Stage<B, P>> center() {

        final class _GraphCenter extends Pipeline.AbstractBase<P> implements Pipeline.Stage<B, P> {
            private _GraphCenter(final P tail) {
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
                        GraphMeasurer<V, E> graphMeasurer = new GraphMeasurer<>(next);
                        Set<V> center = graphMeasurer.getGraphCenter();
                        this.yield((B) center);  // Yield the center of the graph
                    }

                    @Override
                    public void close() {
                        super.close();
                    }
                }
                return new DegreeStage(out);
            }
        }
        return _GraphCenter::new;
    }
}

