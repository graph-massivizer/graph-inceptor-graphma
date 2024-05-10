package formats;

import data.differenformats.FormatsDB;
import formats.Dot.String2StringEdge;
import magma.data.Seq;
import magma.data.sequence.operator.DataSource;
import magma.data.sequence.operator.strict.ForNext;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static data.Config.GRAPH_FORMATS;

/**
 * These are not really tests. More demos with printouts to check if stuff works.
 *
 * Actual unit tests will follow
 *
 */
public class DotTest {

    private static final DataSource<String2StringEdge> DIRECTED_SOURCE =
            DataSource.of(
                    new Dot.DotFile(FormatsDB.DIRECTED_DOT,
                            9999,
                            9999,
                            9999,
                            9999)
            );

    @Test
    public void test_dot_format_ForNext() {
        ForNext.build(System.out::println)
                .apply(DIRECTED_SOURCE)
                .evaluate();
    }

    @Test
    public void test_dot_format_WhileNext() {



        var ds = DataSource.of("A", "B", "C");

        Seq.of(ds).peek(s -> System.out.println(s))
                .anyMatch(e -> e.equals("B"));




        Seq.of(DataSource.of(DIRECTED_SOURCE))
                .anyMatch(edge -> Objects.equals(edge.source(), "A"));
    }

    @Test
    public void test_dot_format_TryNext() {
        var traverser = DIRECTED_SOURCE.traverse();
        while (traverser.tryNext(e -> System.out.println(e.source() + " --> " + e.target())));
    }
}
