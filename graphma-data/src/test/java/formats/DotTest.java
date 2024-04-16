package formats;

import magma.data.sequence.operator.DataSource;
import magma.data.sequence.operator.strict.ForNext;
import org.junit.jupiter.api.Test;

import static data.Config.GRAPH_FORMATS;

public class DotTest {

    @Test
    public void test_dot_format() {
        var pth = GRAPH_FORMATS.resolve("dot").resolve("directed_graph.dot");
        Dot.DotFile dotFile = new Dot.DotFile(pth, 9999, 9999, 9999, 9999);
        final var ds = DataSource.of(dotFile);
        ForNext.build(System.out::println)
                .apply(ds)
                .evaluate();
    }
}
