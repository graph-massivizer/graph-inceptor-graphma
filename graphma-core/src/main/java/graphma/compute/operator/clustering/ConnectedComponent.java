package graphma.compute.operator.clustering;

import magma.data.sequence.operator.Operator;
import magma.data.sequence.pipeline.Composer;
import magma.data.sequence.pipeline.Pipe;
import magma.data.sequence.pipeline.Pipeline;
import org.jgrapht.Graph;

import java.util.Set;
import java.util.List;

public enum ConnectedComponent {
    ;

    public static <V, E,
            G extends Graph<V, E>,
            B extends List<Set<V>>,
            P extends Pipeline<?, ?>>
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
