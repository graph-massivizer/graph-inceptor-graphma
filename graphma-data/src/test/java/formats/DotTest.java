package formats;

import magma.adt.data.Flow;
import magma.data.Seq;
import magma.data.sequence.operator.DataSource;
import magma.data.sequence.operator.strict.ForNext;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static data.Config.GRAPH_FORMATS;

public class DotTest {

    @Test
    public void test_dot_format_ForNext() {
        var pth = GRAPH_FORMATS.resolve("dot").resolve("directed_graph.dot");
        Dot.DotFile dotFile = new Dot.DotFile(pth, 9999, 9999, 9999, 9999);
        final var ds = DataSource.of(dotFile);
        ForNext.build(System.out::println)
                .apply(ds)
                .evaluate();
    }

    @Test
    public void test_dot_format_WhileNext() {
        var pth = GRAPH_FORMATS.resolve("dot").resolve("directed_graph.dot");
        var dotFile = new Dot.DotFile(pth, 9999, 9999, 9999, 9999);
        Seq.of(DataSource.of(dotFile))
                .anyMatch(edge -> Objects.equals(edge.source(), "A"));
    }

    @Test
    public void test_dot_format_TryNext() {
        var pth = GRAPH_FORMATS.resolve("dot").resolve("directed_graph.dot");
        var dotFile = new Dot.DotFile(pth, 9999, 9999, 9999, 9999);
        var traverser = dotFile.traverse();
        while (traverser.tryNext(e -> System.out.println(e.source() + " --> " + e.target())));
    }
}
