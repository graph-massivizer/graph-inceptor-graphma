package graphma.compute.operator.clustering;

import magma.data.sequence.operator.Operator;
import magma.data.sequence.pipeline.Composer;
import magma.data.sequence.pipeline.Pipe;
import magma.data.sequence.pipeline.Pipeline;
import magma.data.sequence.pipeline.TerminalSink;
import magma.value.Unit;
import org.jgrapht.alg.clustering.LabelPropagationClustering;

/**
 * Provides utility methods for clustering a graph using the Label Propagation algorithm within a pipeline structure.
 * <p>
 * The {@code LabelPropagation} enum includes static methods to create pipeline stages
 * for clustering graphs and printing the clustering results. It leverages the Label Propagation
 * algorithm from the {@link org.jgrapht.alg.clustering.LabelPropagationClustering} class
 * to identify clusters within a graph. These methods are designed to be used within a
 * data processing pipeline, allowing for flexible integration with other pipeline stages.
 * </p>
 * <p>
 * This utility is structured as an enum with no instances, serving as a namespace for
 * the static methods {@code cluster()} and {@code print()}, which provide the core functionality
 * for graph clustering and result output, respectively.
 * </p>
 *
 * Note: The methods defined in this enum are static and are intended to be used
 * through the enum name without instantiating it.
 */
public enum LabelPropagation {
    // No enum instances are defined as this enum is used as a namespace for the utility methods.
    ;

    /**
     * Creates a pipeline stage for clustering a graph using the Label Propagation algorithm.
     * <p>
     * This method returns a {@link Composer} that, when applied, produces a pipeline stage
     * that performs clustering on a graph. The stage uses the Label Propagation Clustering
     * algorithm to identify clusters within the graph.
     * </p>
     *
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @param <G> the graph type, extending {@link org.jgrapht.Graph}
     * @param <B> the clustering type, extending {@link LabelPropagationClustering.Clustering}
     * @param <P> the pipeline type, extending {@link Pipeline}
     * @return a {@link Composer} that, when applied, returns a {@link Pipeline.Stage}
     *         that performs the clustering
     */
    public static <V, E,
            G extends org.jgrapht.Graph<V, E>,
            B extends LabelPropagationClustering.Clustering<V>,
            P extends Pipeline<?, ?>>
    Composer<P, Pipeline.Stage<B, P>> cluster() {

        final class _LabelPropagation extends Pipeline.AbstractBase<P> implements Pipeline.Stage<B, P> {
            private _LabelPropagation(final P tail) {
                super(tail);
            }

            @Override
            public Pipe<?> apply(final Pipe<B> out) {
                //
                final class LabelStage extends Operator.Transform<G, B> {
                    private LabelStage(final Pipe<B> out) {
                        super(out);
                    }

                    @Override
                    public void open(long count) { super.open(count); }

                    @Override
                    public void onNext(final long index, final G next) {
                        var labelPropagationClustering = new LabelPropagationClustering<V, E>(next);
                        LabelPropagationClustering.Clustering<V> clustering = labelPropagationClustering.getClustering();
                        this.yield((B) clustering);
                    }

                    @Override
                    public void close() {
                        super.close();
                    }
                }
                return new LabelStage(out);
            }
        }
        return _LabelPropagation::new;
    }

    /**
     * Creates a pipeline sink that prints out the clusters identified in the graph.
     * <p>
     * This method returns a {@link Composer} that, when applied, produces a pipeline sink
     * stage that prints each identified cluster. The clusters are printed to the standard
     * output, each prefixed with "CLUSTER: ".
     * </p>
     *
     * @param <V> the vertex type of the clusters
     * @param <B> the clustering type, extending {@link LabelPropagationClustering.Clustering}
     * @param <P> the pipeline type, extending {@link Pipeline}
     * @return a {@link Composer} that, when applied, returns a {@link Pipeline.Sink}
     *         that prints the clusters to the standard output
     */
    public static <V,
            B extends LabelPropagationClustering.Clustering<V>,
            P extends Pipeline<?, ?>>
    Composer<P, Pipeline.Sink<B, P, Unit>> print() {

        final class Sink
                extends TerminalSink.CompleteSink<B, P, Unit>
                implements Pipeline.Sink<B, P, Unit> {

            private Sink(final P tail) {
                super(tail);
            }

            @Override
            public void open(long count) {
                super.open(count);
            }

            @Override
            public void onNext(long index, B next) {
                next.forEach(cluster -> System.out.println("CLUSTER: " + cluster));
            }

            @Override
            public void close() {
                super.close();
            }

            @Override
            public Unit result() {
                return Unit.value;
            }
        }
        return Sink::new;
    }


    public static <V, E,
            G extends LabelPropagationClustering.Clustering<V>,
            B extends String,
            P extends Pipeline<?, ?>>
    Composer<P, Pipeline.Stage<B, P>> textify() {

        final class Cluster extends Pipeline.AbstractBase<P> implements Pipeline.Stage<B, P> {
            private Cluster(final P tail) {
                super(tail);
            }

            @Override
            public Pipe<?> apply(final Pipe<B> out) {
                final class LabelStage extends Operator.Transform<G, B> {
                    private LabelStage(final Pipe<B> out) {
                        super(out);
                    }

                    @Override
                    public void open(long count) { super.open(count); }

                    @Override
                    public void onNext(final long index, final G next) {
                        final String[] result = { "" };
                        next.forEach(cluster -> result[0] += "CLUSTER: " + cluster + "\n");
                        this.yield((B) result[0]);
                    }

                    @Override
                    public void close() {
                        super.close();
                    }
                }
                return new LabelStage(out);
            }
        }
        return Cluster::new;
    }
}
