package graphma.compute.operator.metrics;

import magma.data.sequence.operator.Operator;
import magma.data.sequence.pipeline.Composer;
import magma.data.sequence.pipeline.Pipe;
import magma.data.sequence.pipeline.Pipeline;
import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.GraphMeasurer;

/**
 * Provides a method to calculate the diameter of graphs. The diameter is the greatest distance
 * (defined as the number of edges in the shortest path) between any pair of nodes in the graph.
 * This class facilitates the integration of graph diameter computations into data processing
 * pipelines, enabling advanced graph analysis within the Graphma framework.
 *
 * <p>Usage example:</p>
 * <pre>
 * {@code
 * ForNext.build(System.out::println)
 *                 .compose(Map.build(r -> r + "\n--------\n"))
 *                 .compose(Diameter.diameter())
 *                 .compose(MtxToUndirectedGraph.of(DefaultEdge.class))
 *                 .compose(Filter.build((Mtx.MTXFile mtx) -> mtx.lines() < 40 && mtx.lines() > 20))
 *                 .apply(DataSource.of(SSDB.SMALL))
 *                 .evaluate();
 * }
 * </pre>
 */
public enum Diameter {
    ;

    /**
     * Creates a pipeline stage that computes the diameter of a graph.
     * The diameter is defined as the longest shortest path between any pair of vertices within the graph.
     * This method returns a composer that can be seamlessly integrated as a stage in data processing pipelines,
     * thereby supporting the modular construction of complex data processing workflows.
     *
     * @param <V> the vertex type
     * @param <E> the edge type
     * @param <G> the graph type, extending JGraphT's Graph interface
     * @param <P> the type of the pipeline
     * @return a composer that constructs a pipeline stage for computing the graph diameter
     */
    public static <V, E, G extends Graph<V, E>, P extends Pipeline<?, ?>>
    Composer<P, Pipeline.Stage<Double, P>> diameter() {

        final class _Diameter extends Pipeline.AbstractBase<P> implements Pipeline.Stage<Double, P> {
            private _Diameter(final P tail) {
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
                        double diameter = graphMeasurer.getDiameter();
                        this.yield(diameter); // Yield the calculated diameter
                    }

                    @Override
                    public void close() {
                        super.close();
                    }
                }
                return new DegreeStage(out);
            }
        }
        return _Diameter::new;
    }
}
