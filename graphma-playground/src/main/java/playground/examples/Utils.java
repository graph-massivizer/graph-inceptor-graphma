package playground.examples;

import magma.adt.control.traversal.Traversal;
import magma.base.Assert;
import magma.base.Cast;
import magma.control.function.Fn1;
import magma.control.traversal.Traverser;

public enum Utils {
    ;

    static <A> Traverser<A> merge(final Traverser<? extends A>... ts) {
        Assert.noNulls(ts);
        final class MergeTraverser extends Traversal.Control.Context implements Traverser<A> {
            private int index = 0;
            private Traverser<? extends A> current = ts[index];

            @Override
            public boolean tryNext(final Fn1.Consumer<? super A> action) {
                final var result = current.tryNext(action);
                if (!result && index < ts.length) {
                    this.current = ts[++index];
                    return tryNext(action);
                }
                return result;
            }

            @Override
            public void forNext(Fn1.Consumer<? super A> action) {
                current.forNext(action);
                if (index < ts.length) {
                    current = ts[index++];
                    forNext(action);
                }
            }

            @Override
            public Traversal.Status whileNext(Fn1<Traversal.Control, Fn1.Consumer<? super A>> context) {
                while (index < ts.length) {
                    Traversal.Status status = current.whileNext(Cast.force(context));
                    if (status == Traversal.Status.EXIT) {
                        return status;
                    }
                    if (++index < ts.length) {
                        current = ts[index];
                    }
                }
                return Traversal.Status.DONE;
            }
        }
        return new MergeTraverser();
    }
}
