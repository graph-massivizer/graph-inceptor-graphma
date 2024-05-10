package scenario_1;

import magma.adt.control.traversal.Traversal;
import magma.base.Assert;
import magma.base.Cast;
import magma.control.function.Fn;
import magma.control.function.Fn1;
import magma.control.traversal.Traverser;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.csv.CSVExporter;
import org.jgrapht.nio.dot.DOTExporter;
import org.jgrapht.nio.gml.GmlExporter;
import org.jgrapht.nio.graphml.GraphMLExporter;
import org.jgrapht.nio.matrix.MatrixExporter;

import java.io.FileWriter;
import java.nio.file.Path;

public enum Utils {
    ;

    public static <V extends Long, E extends DefaultEdge> Fn1.Consumer<Graph<V, E>> exporter(Path filePath, String format) {
        FileWriter writer = Fn.checked(() -> new FileWriter(filePath.toFile())).apply();
        return switch (format) {
            case "dot" -> graph -> new DOTExporter<V, E>().exportGraph(graph, writer);
            case "graphml" -> graph -> { new GraphMLExporter<V, E>().exportGraph(graph, writer); };
            case "gml" -> graph -> new GmlExporter<V, E>().exportGraph(graph, writer);
            case "csv" -> graph -> new CSVExporter<V, E>().exportGraph(graph, writer);
            case "matrix" -> graph -> new MatrixExporter<V, E>().exportGraph(graph, writer);
            default -> throw new IllegalArgumentException("Unsupported file extension: " + format);
        };
    }

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
