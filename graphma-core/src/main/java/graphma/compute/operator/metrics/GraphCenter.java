package graphma.compute.operator.metrics;

import magma.data.sequence.operator.Operator;
import magma.data.sequence.pipeline.Composer;
import magma.data.sequence.pipeline.Pipe;
import magma.data.sequence.pipeline.Pipeline;
import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.GraphMeasurer;

import java.util.Set;

public enum GraphCenter {
    ;

    public static <V, E,
            G extends Graph<V, E>,
            B extends Set<V>,
            P extends Pipeline<?, ?>>
    Composer<P, Pipeline.Stage<B, P>> center() {

        final class _GraphCenter extends Pipeline.AbstractBase<P> implements Pipeline.Stage<B, P> {
            private _GraphCenter(final P tail) {
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
                        final var graphMeasurer = new GraphMeasurer<>(next);
                        final var center = graphMeasurer.getGraphCenter();
                        this.yield((B) center);
                    }

                    @Override
                    public void close() {
                        super.close();
                    }
                }
                return new DegreeStage(out);
            }
        }
        return _GraphCenter::new;
    }
}
