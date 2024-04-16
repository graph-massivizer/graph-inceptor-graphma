package graphma.compute.operator.transform;

import formats.Mtx;
import magma.adt.value.product.Product2;
import magma.control.traversal.Traversable;
import magma.data.sequence.operator.Operator;
import magma.data.sequence.pipeline.Composer;
import magma.data.sequence.pipeline.Pipe;
import magma.data.sequence.pipeline.Pipeline;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;

public enum MtxToUndirectedGraph {
    // No enum instances are defined as this enum is used as a namespace for the utility methods.
    ;

    /**
     * Converts a traversable collection of edge representations into a JGraphT graph.
     * <p>
     * This method provides a way to transform a series of edge data, represented as {@link Product2} of {@link Long},
     * into a {@link org.jgrapht.Graph} instance. It's designed to facilitate the conversion of matrix-based
     * graph representations into JGraphT's graph format, which is more conducive to various graph algorithms
     * and operations. The method is particularly tailored for processing test instances from a test database,
     * aiding in the setup of test environments or the preprocessing of graph data for testing purposes.
     * </p>
     *
     * @param <V> The vertex type of the resulting graph.
     * @param <E> The edge class extending {@link DefaultEdge}, representing the edges in the resulting graph.
     * @param <A> The traversable collection type containing edge representations as {@link Product2} of {@link Long}.
     * @param <G> The graph type extending {@link org.jgrapht.Graph}.
     * @param <P> The pipeline type extending {@link Pipeline}.
     * @param edgeClass The class object for the edge type, which is used to instantiate edges in the graph.
     * @return A {@link Composer} that, when applied, returns a {@link Pipeline.Stage} for converting edge
     *         data into a graph.
     */
    public static <V, E extends DefaultEdge,
            A extends Traversable<Mtx.Long2LongEdge>,
            G extends org.jgrapht.Graph<V, E>,
            P extends Pipeline<?, ?>>

    Composer<P, Pipeline.Stage<G, P>> of(Class<E> edgeClass) {

        final class _MtxToUndirectedGraph extends Pipeline.AbstractBase<P> implements Pipeline.Stage<G, P> {

            private _MtxToUndirectedGraph(final P tail) { super(tail); }

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
                        var graph = new DefaultUndirectedGraph<Long, E>(edgeClass);
//                        System.out.println("FILE: " + next);
                        next.forEach(edge -> {
//                            System.out.println("EDGE: " + edge);
                            graph.addVertex(edge.source());
                            graph.addVertex(edge.target());
                            graph.addEdge(edge.source(), edge.target());
                        });
                        this.yield((G) graph);
                    }

                    @Override
                    public void close() {
                        super.close();
                    }
                }
                return new BuildingStage(out);
            }
        }
        return _MtxToUndirectedGraph::new;
    }
}
