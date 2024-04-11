package graphma.compute.operator.transform;

import magma.data.sequence.operator.Operator;
import magma.data.sequence.pipeline.Composer;
import magma.data.sequence.pipeline.Pipe;
import magma.data.sequence.pipeline.Pipeline;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

public enum GraphToSimpleGraph {
    ;

    public static <V, E extends DefaultEdge,
            A extends org.jgrapht.Graph<V, E>,
            G extends SimpleGraph<V, E>,
            P extends Pipeline<?, ?>>

    Composer<P, Pipeline.Stage<G, P>> of(Class<E> edgeClass) {

        final class _GraphToSimpleGraph extends Pipeline.AbstractBase<P> implements Pipeline.Stage<G, P> {

            private _GraphToSimpleGraph(final P tail) { super(tail); }

            @Override
            public Pipe<A> apply(final Pipe<G> out) {

                final class BuildingStage extends Operator.Transform<A, G> {

                    private BuildingStage(final Pipe<G> out) {
                        super(out);
                    }

                    @Override
                    public void open(long count) {
                        super.open(count);
                    }

                    @Override
                    public void onNext(final long index, final A next) {
                        // Create a new SimpleGraph instance for each incoming graph 'next'
                        var simpleGraph = new SimpleGraph<V, E>(edgeClass);

                        // Add all vertices from 'next' to 'simpleGraph'
                        for (V vertex : next.vertexSet()) {
                            simpleGraph.addVertex(vertex);
                        }

                        // Add all edges from 'next' to 'simpleGraph'
                        for (E edge : next.edgeSet()) {
                            V sourceVertex = next.getEdgeSource(edge);
                            V targetVertex = next.getEdgeTarget(edge);
                            simpleGraph.addEdge(sourceVertex, targetVertex, edge);
                        }
                        this.yield((G) simpleGraph);
                    }

                    @Override
                    public void close() {
                        super.close();
                    }
                }
                return new BuildingStage(out);
            }
        }
        return _GraphToSimpleGraph::new;
    }

}
