package graphma.compute.operator.centralities;

import magma.data.sequence.operator.Operator;
import magma.data.sequence.pipeline.Composer;
import magma.data.sequence.pipeline.Pipe;
import magma.data.sequence.pipeline.Pipeline;
import org.jgrapht.Graph;

import java.util.HashMap;
import java.util.Map;

public enum DegreeCentrality {
    ;

    public static <V, E,
            G extends Graph<V, E>,
            B extends Map<V, Double>,  // Using Double for consistency, though degree centrality values will be integers
            P extends Pipeline<?, ?>>
    Composer<P, Pipeline.Stage<B, P>> centrality() {

        final class _DegreeCentrality extends Pipeline.AbstractBase<P> implements Pipeline.Stage<B, P> {
            private _DegreeCentrality(final P tail) {
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
                        Map<V, Double> degreeCentrality = new HashMap<>();
                        for (V vertex : next.vertexSet()) {
                            double degree = next.degreeOf(vertex);  // Get the degree of the vertex
                            degreeCentrality.put(vertex, degree);  // Store it in the map
                        }
                        this.yield((B) degreeCentrality);  // Yield the degree centrality scores
                    }

                    @Override
                    public void close() {
                        super.close();
                    }
                }
                return new DegreeStage(out);
            }
        }
        return _DegreeCentrality::new;
    }
}
