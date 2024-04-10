package graphma.compute.operator.cluster;

import magma.data.sequence.operator.Operator;
import magma.data.sequence.pipeline.Composer;
import magma.data.sequence.pipeline.Pipe;
import magma.data.sequence.pipeline.Pipeline;
import org.jgrapht.alg.scoring.Coreness;

public class KCores {
    // No enum instances are defined as this enum is used as a namespace for the utility methods.
    ;

    public static <V, E,
            G extends org.jgrapht.Graph<V, E>,
            B extends Coreness<V, E>,
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
                        final var clustering = new Coreness<V, E>(next);
                        clustering.getDegeneracy();
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

    public static <V, E,
            C extends Coreness<V, E>,
            B extends String,
            P extends Pipeline<?, ?>>
    Composer<P, Pipeline.Stage<B, P>> textify() {

        final class CorenessStage extends Pipeline.AbstractBase<P> implements Pipeline.Stage<B, P> {
            private CorenessStage(final P tail) { super(tail); }

            @Override
            public Pipe<?> apply(final Pipe<B> out) {
                final class CorenessTransform extends Operator.Transform<C, B> {
                    private CorenessTransform(final Pipe<B> out) {
                        super(out);
                    }

                    @Override
                    public void open(long count) { super.open(count); }

                    @Override
                    public void onNext(final long index, final C next) {
                        this.yield((B) String.valueOf(next.getScores()));
                    }

                    @Override
                    public void close() {
                        super.close();
                    }
                }
                return new CorenessTransform(out);
            }
        }
        return CorenessStage::new;
    }
}
