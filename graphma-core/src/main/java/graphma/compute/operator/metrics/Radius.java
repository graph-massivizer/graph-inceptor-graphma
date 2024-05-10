package graphma.compute.operator.metrics;

import magma.data.sequence.operator.Operator;
import magma.data.sequence.pipeline.Composer;
import magma.data.sequence.pipeline.Pipe;
import magma.data.sequence.pipeline.Pipeline;
import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.GraphMeasurer;

/**
 * Provides a method to calculate the radius of graphs within a data processing pipeline.
 * The radius of a graph is the minimum eccentricity of any vertex, representing the smallest
 * maximum distance between any vertex and all other vertices in the graph. This class
 * enables the integration of graph radius computations into data processing pipelines,
 * offering advanced capabilities for analyzing graph structures efficiently within the Graphma framework.
 *
 * <p>Usage example:</p>
 * <pre>
 * {@code
 * ForNext.build(System.out::println)
 *                 .compose(Map.build(r -> r + "\n--------\n"))
 *                 .compose(Radius.radius())
 *                 .compose(MtxToUndirectedGraph.of(DefaultEdge.class))
 *                 .compose(Filter.build((Mtx.MTXFile mtx) -> mtx.lines() < 40 && mtx.lines() > 20))
 *                 .apply(DataSource.of(SSDB.SMALL))
 *                 .evaluate();
 * }
 * </pre>
 */
public enum Radius {
    ;
    // Enum structure used as a namespace for utility methods.

    /**
     * Creates a pipeline stage that computes the radius of a graph.
     * The radius is the minimum distance necessary to ensure that all nodes can be reached from any node
     * in the graph via the shortest path. This method returns a composer that facilitates seamless
     * integration as a stage in data processing pipelines, supporting modular construction of
     * complex workflows for graph analysis.
     *
     * @param <V> the vertex type
     * @param <E> the edge type
     * @param <G> the graph type, extending JGraphT's Graph interface
     * @param <P> the type of the pipeline context
     * @return a composer that constructs a pipeline stage for computing the graph radius
     */
    public static <V, E, G extends Graph<V, E>, P extends Pipeline<?, ?>>
    Composer<P, Pipeline.Stage<Double, P>> radius() {

        final class _Radius extends Pipeline.AbstractBase<P> implements Pipeline.Stage<Double, P> {
            private _Radius(final P tail) {
                super(tail);
            }

            @Override
            public Pipe<?> apply(final Pipe<Double> out) {
                final class DegreeStage extends Operator.Transform<G, Double> {
                    private DegreeStage(final Pipe<Double> out) {
                        super(out);
                    }

                    @Override
                    public void open(long count) {
                        super.open(count);
                    }

                    @Override
                    public void onNext(final long index, final G next) {
                        GraphMeasurer<V, E> graphMeasurer = new GraphMeasurer<>(next);
                        double radius = graphMeasurer.getRadius();
                        this.yield(radius);  // Yield the radius of the graph
                    }

                    @Override
                    public void close() {
                        super.close();
                    }
                }
                return new DegreeStage(out);
            }
        }
        return _Radius::new;
    }
}

