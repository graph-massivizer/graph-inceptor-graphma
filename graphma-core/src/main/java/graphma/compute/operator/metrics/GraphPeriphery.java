package graphma.compute.operator.metrics;

import magma.data.sequence.operator.Operator;
import magma.data.sequence.pipeline.Composer;
import magma.data.sequence.pipeline.Pipe;
import magma.data.sequence.pipeline.Pipeline;
import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.GraphMeasurer;

import java.util.Set;

/**
 * Provides a method to calculate the periphery of graphs within a data processing pipeline. The periphery of a graph
 * consists of all nodes with the maximum eccentricity, representing nodes that are the farthest from others in the graph.
 * This class enables the integration of graph periphery computations into data processing pipelines, offering
 * advanced capabilities for analyzing graph structures efficiently within the Graphma framework.
 *
 * <p>Usage example:</p>
 * <pre>
 * {@code
 * ForNext.build(System.out::println)
 *                 .compose(Map.build(r -> r + "\n--------\n"))
 *                 .compose(GraphPeriphery.periphery())
 *                 .compose(MtxToUndirectedGraph.of(DefaultEdge.class))
 *                 .compose(Filter.build((Mtx.MTXFile mtx) -> mtx.lines() < 40 && mtx.lines() > 20))
 *                 .apply(DataSource.of(SSDB.SMALL))
 *                 .evaluate();
 * }
 * </pre>
 */
public enum GraphPeriphery {
    ;
    // Enum structure used as a namespace for utility methods.

    /**
     * Creates a pipeline stage that computes the periphery of a graph.
     * The graph periphery includes all nodes with the maximum distance to any other node in the graph.
     * This method returns a composer that facilitates seamless integration as a stage in data processing pipelines,
     * supporting modular construction of complex workflows for graph analysis.
     *
     * @param <V> the vertex type
     * @param <E> the edge type
     * @param <G> the graph type, extending JGraphT's Graph interface
     * @param <B> the expected type of the output, typically a set of vertices
     * @param <P> the type of the pipeline context
     * @return a composer that constructs a pipeline stage for computing the graph periphery
     */
    public static <V, E, G extends Graph<V, E>, B extends Set<V>, P extends Pipeline<?, ?>>
    Composer<P, Pipeline.Stage<B, P>> periphery() {

        final class _GraphPeriphery extends Pipeline.AbstractBase<P> implements Pipeline.Stage<B, P> {
            private _GraphPeriphery(final P tail) {
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
                        Set<V> periphery = graphMeasurer.getGraphPeriphery();
                        this.yield((B) periphery);  // Yield the periphery of the graph
                    }

                    @Override
                    public void close() {
                        super.close();
                    }
                }
                return new DegreeStage(out);
            }
        }
        return _GraphPeriphery::new;
    }
}

