package graphma.compute.operator.centralities;

import magma.data.sequence.operator.Operator;
import magma.data.sequence.pipeline.Composer;
import magma.data.sequence.pipeline.Pipe;
import magma.data.sequence.pipeline.Pipeline;
import org.jgrapht.Graph;

import java.util.Map;

public enum EigenvectorCentrality {
    ;

    public static <V, E,
            G extends Graph<V, E>,
            B extends Map<V, Double>,  // Using Double for consistency, though degree centrality values will be integers
            P extends Pipeline<?, ?>>
    Composer<P, Pipeline.Stage<B, P>> centrality() {

        final class Centrality extends Pipeline.AbstractBase<P> implements Pipeline.Stage<B, P> {
            private Centrality(final P tail) {
                super(tail);
            }

            @Override
            public Pipe<?> apply(final Pipe<B> out) {
                final class DegreeStage extends Operator.Transform<G, B> {
                    private DegreeStage(final Pipe<B> out) {
                        super(out);
                    }

                    @Override
                    public void open(long count) {
                        super.open(count);
                    }

                    @Override
                    public void onNext(final long index, final G next) {
                        final var centrality = new org.jgrapht.alg.scoring.EigenvectorCentrality<>(next);
                        this.yield((B) centrality.getScores());
                    }

                    @Override
                    public void close() {
                        super.close();
                    }
                }
                return new DegreeStage(out);
            }
        }
        return Centrality::new;
    }
}
