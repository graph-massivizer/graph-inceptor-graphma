package graphma.compute.operator.metrics;

import magma.data.sequence.operator.Operator;
import magma.data.sequence.pipeline.Composer;
import magma.data.sequence.pipeline.Pipe;
import magma.data.sequence.pipeline.Pipeline;
import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.GraphMeasurer;

public enum Diameter {
    ;

    public static <V, E,
            G extends Graph<V, E>,
            P extends Pipeline<?, ?>>
    Composer<P, Pipeline.Stage<Double, P>> diameter() {

        final class _Diameter extends Pipeline.AbstractBase<P> implements Pipeline.Stage<Double, P> {
            private _Diameter(final P tail) {
                super(tail);
            }

            @Override
            public Pipe<?> apply(final Pipe<Double> out) {
                final class DegreeStage extends Operator.Transform<G, Double> {
                    private DegreeStage(final Pipe<Double> out) {
                        super(out);
                    }

                    @Override
                    public void open(long count) {
                        super.open(count);
                    }

                    @Override
                    public void onNext(final long index, final G next) {
                        GraphMeasurer<V, E> graphMeasurer = new GraphMeasurer<>(next);
                        double diameter = graphMeasurer.getDiameter();
                        this.yield(diameter);
                    }

                    @Override
                    public void close() {
                        super.close();
                    }
                }
                return new DegreeStage(out);
            }
        }
        return _Diameter::new;
    }
}
