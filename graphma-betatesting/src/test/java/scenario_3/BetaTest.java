package scenario_3;

import magma.data.sequence.operator.Operator;
import magma.data.sequence.pipeline.Composer;
import magma.data.sequence.pipeline.Pipe;
import magma.data.sequence.pipeline.Pipeline;
import org.jgrapht.Graph;
import org.junit.jupiter.api.Test;

public class BetaTest {

    // TODO YOUR CUSTOM OPERATOR GOES HERE
    private static <
            V,
            E,
            G extends Graph<V, E>,
            P extends Pipeline<?, ?>>
    Composer<P, Pipeline.Stage<
            Double, // TODO replace with return type
            P
            >> xxx() // TODO rename
    {

        final class _XXX // TODO rename
                extends Pipeline.AbstractBase<P> implements Pipeline.Stage<
                Double, // TODO replace with return type
                P> {
            private _XXX // TODO rename
                    (final P tail) {
                super(tail);
            }

            @Override
            public Pipe<?> apply(final Pipe<
                    Double // TODO replace with return type
                    > out) {
                final class DegreeStage extends Operator.Transform<
                        G,
                        Double // TODO replace with return type
                        > {
                    private DegreeStage(final Pipe<
                            Double // TODO replace with return type
                            > out) {
                        super(out);
                    }

                    @Override
                    public void open(long count) {
                        super.open(count);
                    }

                    /**
                     * This method is called after assembling the pipeline
                     * for each graph. When designing the custom operator, you
                     * have to replace the method body and yield the result.
                     *
                     * @param index Position or sequence number of the value.
                     * @param nextGraph  The value to be processed.
                     */
                    @Override
                    public void onNext(final long index, final G nextGraph) {

                        // TODO your implementation goes here.

                        this.yield(
                                (Double) // TODO replace with return type
                                        null);  // Yield the radius of the graph
                    }

                    @Override
                    public void close() {
                        super.close();
                    }
                }
                return new DegreeStage(out);
            }
        }
        return _XXX::new;
    }

    @Test
    public void test_scenario_3() {
        // TODO YOUR CODE GOES HERE
    }
}
