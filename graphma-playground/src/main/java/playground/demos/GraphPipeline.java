package playground.demos;

import data.suitesparse.SSDB;
import formats.Mtx;
import magma.adt.value.product.Product2;
import magma.control.traversal.Traversable;
import magma.data.sequence.operator.DataSource;
import magma.data.sequence.operator.Operator;
import magma.data.sequence.operator.lazy.Filter;
import magma.data.sequence.pipeline.Composer;
import magma.data.sequence.pipeline.Pipe;
import magma.data.sequence.pipeline.Pipeline;
import magma.data.sequence.pipeline.TerminalSink;
import magma.value.Unit;
import org.jgrapht.alg.clustering.LabelPropagationClustering;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;

public enum GraphPipeline {
    ;

    public interface Graph {

        static <V, E extends DefaultEdge,
                A extends Traversable<Product2<Long, Long>>,
                G extends org.jgrapht.Graph<V, E>,
                P extends Pipeline<?, ?>>

        Composer<P, Pipeline.Stage<G, P>> graph(Class<E> edgeClass) {

            final class ToGraph extends Pipeline.AbstractBase<P> implements Pipeline.Stage<G, P> {

                private ToGraph(final P tail) { super(tail); }

                @Override
                public Pipe<A> apply(final Pipe<G> out) {
                    //
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
                            System.out.println("FILE: " + next);
                            next.forEach(edge -> {
                                System.out.println("EDGE: " + edge);
                                graph.addVertex(edge._1());
                                graph.addVertex(edge._2());
                                graph.addEdge(edge._1(), edge._2());
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
            return ToGraph::new;
        }


        static <V, E,
                G extends org.jgrapht.Graph<V, E>,
                B extends LabelPropagationClustering.Clustering<V>,
                P extends Pipeline<?, ?>>
        Composer<P, Pipeline.Stage<B, P>> cluster() {

            final class Cluster extends Pipeline.AbstractBase<P> implements Pipeline.Stage<B, P> {

                private Cluster(final P tail) {
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
            return Cluster::new;
        }


        static <V,
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
    }

    public static void main(String[] args) {
        Graph.print()
                .compose(Graph.cluster())
                .compose(Graph.graph(DefaultEdge.class))
                .compose(Filter.build((Mtx.MTXFile mtx) -> mtx.lines() < 40 && mtx.lines() > 20))
                .apply(DataSource.of(SSDB.SMALL))
                .evaluate();

//        var description = Graph.print()
//                .compose(Graph.cluster())
//                .compose(Graph.graph(DefaultEdge.class)) // READER
//                .compose(Filter.build((SSDB.MTXFile mtx) -> mtx.lines() < 40 && mtx.lines() > 20))
//                .apply(DataSource.of(SSDB.SMALL));

    }
}
