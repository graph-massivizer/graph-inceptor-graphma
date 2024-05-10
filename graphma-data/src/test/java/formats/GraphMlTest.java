package formats;

import data.differenformats.FormatsDB;
import magma.data.Seq;
import magma.data.sequence.operator.DataSource;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static data.Config.GRAPH_FORMATS;

/**
 * These are not really tests. More demos with printouts to check if stuff works.
 *
 * Actual unit tests will follow
 *
 */
public class GraphMlTest {

    private static final DataSource<GraphML.String2StringEdge> DIRECTED_SOURCE =
            DataSource.of(
                    new GraphML.GraphMLFile(
                            FormatsDB.DIRECTED_GRAPHML,
                            9999,
                            9999,
                            9999,
                            9999)
            );

    @Test
    public void test_dot_format_ForNext() {
        Seq.of(DataSource.of(DIRECTED_SOURCE))
                .forEach(e -> System.out.println(e.source() + " --> " + e.target()));
    }

    @Test
    public void test_dot_format_WhileNext() {
        Seq.of(DataSource.of(DIRECTED_SOURCE))
                .peek(e -> System.out.println(e.source() + " --> " + e.target()))
                .anyMatch(edge -> Objects.equals(edge.source(), "Z"));
    }

    @Test
    public void test_dot_format_TryNext() {
        var traverser = DIRECTED_SOURCE.traverse();
        while (traverser.tryNext(e -> System.out.println(e.source() + " --> " + e.target())));
    }
}
