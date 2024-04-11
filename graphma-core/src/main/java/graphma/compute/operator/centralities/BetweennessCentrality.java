package graphma.compute.operator.centralities;

import magma.data.sequence.operator.Operator;
import magma.data.sequence.pipeline.Composer;
import magma.data.sequence.pipeline.Pipe;
import magma.data.sequence.pipeline.Pipeline;
import org.jgrapht.Graph;

import java.util.Map;

public enum BetweennessCentrality {
    ;

    public static <V, E,
            G extends Graph<V, E>,
            B extends Map<V, Double>,
            P extends Pipeline<?, ?>>
    Composer<P, Pipeline.Stage<B, P>> centrality() {

        final class Centrality extends Pipeline.AbstractBase<P> implements Pipeline.Stage<B, P> {
            private Centrality(final P tail) {
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
                        final var centrality = new org.jgrapht.alg.scoring.BetweennessCentrality<>(next);
                        this.yield((B) centrality.getScores());  // Yield the connected components
                    }

                    @Override
                    public void close() {
                        super.close();
                    }
                }
                return new LabelStage(out);
            }
        }
        return Centrality::new;
    }
}
