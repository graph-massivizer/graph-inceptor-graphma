package graphma.compute.operator.partitioning;

import magma.data.sequence.operator.Operator;
import magma.data.sequence.pipeline.Composer;
import magma.data.sequence.pipeline.Pipe;
import magma.data.sequence.pipeline.Pipeline;
import org.jgrapht.Graph;
import org.jgrapht.GraphTests;
import org.jgrapht.alg.interfaces.PartitioningAlgorithm.Partitioning;

public enum BipartitePartitioning {
    ;

    public static <V, E,
            G extends Graph<V, E>,
            B extends Partitioning<V>,
            P extends Pipeline<?, ?>>
    Composer<P, Pipeline.Stage<B, P>> partition() {

        final class Partition extends Pipeline.AbstractBase<P> implements Pipeline.Stage<B, P> {
            private Partition(final P tail) {
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
                        if (!GraphTests.isBipartite(next)) {
                            System.out.println("Graph is NOT Bipartite");
                        }
                        final var partition = new org.jgrapht.alg.partition.BipartitePartitioning<>(next);
                        this.yield((B) partition.getPartitioning());  // Yield the connected components
                    }

                    @Override
                    public void close() {
                        super.close();
                    }
                }
                return new LabelStage(out);
            }
        }
        return Partition::new;
    }
}
