package graphma.compute.operator.transform;

import magma.data.sequence.operator.Operator;
import magma.data.sequence.pipeline.Composer;
import magma.data.sequence.pipeline.Pipe;
import magma.data.sequence.pipeline.Pipeline;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

/**
 * Provides a method to transform general graphs into a {@link SimpleGraph} within a data processing pipeline.
 * This transformation is particularly useful for standardizing graph data into a simple, undirected graph format
 * with no loops or multiple edges between any pair of vertices. The class supports conversion within the Graphma
 * framework's pipeline processing model, enabling seamless graph transformations and ensuring compatibility with
 * subsequent processing stages that require a {@link SimpleGraph} structure.
 *
 * <p>Usage example:</p>
 * <pre>
 * {@code
 * Graph<String, DefaultEdge> originalGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
 * originalGraph.addVertex("A");
 * originalGraph.addVertex("B");
 * originalGraph.addEdge("A", "B");
 * SimpleGraph<String, DefaultEdge> simpleGraph = Pipeline.of(originalGraph)
 *                                                       .apply(GraphToSimpleGraph.of(DefaultEdge.class))
 *                                                       .collectSingle();
 * System.out.println("Vertices: " + simpleGraph.vertexSet());
 * System.out.println("Edges: " + simpleGraph.edgeSet());
 * }
 * </pre>
 */
public enum GraphToSimpleGraph {
    ;
    // Enum structure used as a namespace for utility methods.

    /**
     * Creates a pipeline stage that transforms a graph into a {@link SimpleGraph}.
     * This method allows for the conversion of graphs with various edge definitions into a simple graph format
     * that disallows multiple edges and loops, simplifying the structure for analyses that do not support these
     * features. The method returns a composer that can be used to insert this transformation into a sequence
     * of pipeline stages, facilitating complex data transformations in a modular fashion.
     *
     * @param <V> the vertex type
     * @param <E> the edge type, extending {@link DefaultEdge}
     * @param <A> the original graph type, extending JGraphT's Graph interface
     * @param <G> the target simple graph type, extending {@link SimpleGraph}
     * @param <P> the type of the pipeline context
     * @param edgeClass the class object of the edge type used in the simple graph; this helps in instantiating edges
     * @return a composer that constructs a pipeline stage for converting graphs into {@link SimpleGraph}
     */
    public static <V, E extends DefaultEdge,
            A extends org.jgrapht.Graph<V, E>,
            G extends SimpleGraph<V, E>,
            P extends Pipeline<?, ?>>
    Composer<P, Pipeline.Stage<G, P>> of(Class<E> edgeClass) {

        final class _GraphToSimpleGraph extends Pipeline.AbstractBase<P> implements Pipeline.Stage<G, P> {
            private _GraphToSimpleGraph(final P tail) {
                super(tail);
            }

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

