package scenario_3;

import data.suitesparse.SSDB;
import formats.Mtx;
import graphma.compute.operator.centralities.ClusteringCoefficient;
import graphma.compute.operator.transform.MtxToUndirectedGraph;
import magma.data.sequence.operator.DataSource;
import magma.data.sequence.operator.Operator;
import magma.data.sequence.operator.lazy.Filter;
import magma.data.sequence.operator.lazy.Map;
import magma.data.sequence.operator.strict.ForNext;
import magma.data.sequence.pipeline.Composer;
import magma.data.sequence.pipeline.Pipe;
import magma.data.sequence.pipeline.Pipeline;
import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.GraphMeasurer;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

public class ExampleTest {

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
    private static <
            V,
            E,
            G extends Graph<V, E>,
            P extends Pipeline<?, ?>>
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

                    /**
                     * This method is called after assembling the pipeline
                     * for each graph. When designing the custom operator, you
                     * have to replace the method body and yield the result.
                     *
                     * @param index Position or sequence number of the value.
                     * @param nextGraph  The value to be processed.
                     */
                    @Override
                    public void onNext(final long index, final G nextGraph) {
                        GraphMeasurer<V, E> graphMeasurer = new GraphMeasurer<>(nextGraph);
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

    @Test
    public void test_radius() {
        ForNext.build(System.out::println)
                .compose(Map.build(r -> r + "\n--------\n"))
                .compose(ExampleTest.radius())
                .compose(MtxToUndirectedGraph.of(DefaultEdge.class))
                .compose(Filter.build((Mtx.MTXFile mtx) -> mtx.lines() < 40 && mtx.lines() > 20))
                .apply(DataSource.of(SSDB.SMALL))
                .evaluate();
    }
}
