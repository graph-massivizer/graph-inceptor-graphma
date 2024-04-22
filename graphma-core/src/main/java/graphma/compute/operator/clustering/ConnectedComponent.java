package graphma.compute.operator.clustering;

import magma.data.sequence.operator.Operator;
import magma.data.sequence.pipeline.Composer;
import magma.data.sequence.pipeline.Pipe;
import magma.data.sequence.pipeline.Pipeline;
import org.jgrapht.Graph;

import java.util.Set;
import java.util.List;

/**
 * Provides utilities for computing connected components within graphs. Connected components are
 * subsets of a graph where each vertex is connected to at least one other vertex in the subset,
 * and no vertex in a given subset is connected to any vertex outside that subset.
 *
 * <p>This class is part of a computational pipeline that enables integration with other data processing
 * operations, making it easier to manage complex workflows in graph analysis.</p>
 *
 * <p>Usage example:</p>
 * <pre>
 * {@code
 * ForNext.build(System.out::println)
 *                 .compose(Map.build(r -> r + "\n--------\n"))
 *                 .compose(ConnectedComponent.cluster())
 *                 .compose(MtxToUndirectedGraph.of(DefaultEdge.class))
 *                 .compose(Filter.build((Mtx.MTXFile mtx) -> mtx.lines() < 40 && mtx.lines() > 20))
 *                 .apply(DataSource.of(SSDB.SMALL))
 *                 .evaluate();
 * }
 * </pre>
 */
public enum ConnectedComponent {
    ;

    /**
     * Constructs a pipeline stage that computes the connected components of a graph.
     * This method returns a composer that facilitates integrating this computation as a stage
     * in a data processing pipeline, offering a modular approach for building complex data processing scenarios.
     *
     * @param <V> the vertex type
     * @param <E> the edge type
     * @param <G> the graph type, extending JGraphT's Graph interface
     * @param <B> the output type, expected to be a List of Sets, each Set containing the vertices of a connected component
     * @param <P> the type of the pipeline
     * @return a composer that constructs a pipeline stage for computing connected components
     */
    public static <V, E, G extends Graph<V, E>, B extends List<Set<V>>, P extends Pipeline<?, ?>>
    Composer<P, Pipeline.Stage<B, P>> cluster() {

        final class _ConnectedComponent extends Pipeline.AbstractBase<P> implements Pipeline.Stage<B, P> {
            private _ConnectedComponent(final P tail) {
                super(tail);
            }

            @Override
            public Pipe<?> apply(final Pipe<B> out) {
                final class LabelStage extends Operator.Transform<G, B> {
                    private LabelStage(final Pipe<B> out) {
                        super(out);
                    }

                    @Override
                    public void open(long count) {
                        super.open(count);
                    }

                    @Override
                    public void onNext(final long index, final G next) {
                        final var cc = new org.jgrapht.alg.connectivity.ConnectivityInspector<>(next);
                        final var components = cc.connectedSets();
                        this.yield((B) components);  // Yield the connected components
                    }

                    @Override
                    public void close() {
                        super.close();
                    }
                }
                return new LabelStage(out);
            }
        }
        return _ConnectedComponent::new;
    }
}

