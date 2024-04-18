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


    public static void main(String[] args) {
        var ds = DataSource.of("A", "B", "C", "D", "E");
        Seq.of(ds).forEach(System.out::println);
    }

    @Test
    public void test_dot_format_WhileNext() {
        var ds = DataSource.of("A", "B", "C", "D", "E");
        Seq.of(ds).forEach(System.out::println);
//        Seq.of(ds)
//                .peek(s -> System.out.println(s))
//                .anyMatch(e -> Objects.equals(e, "D"));
//        var pth = GRAPH_FORMATS.resolve("dot").resolve("directed_graph.dot");
//        Dot.DotFile dotFile = new Dot.DotFile(pth, 9999, 9999, 9999, 9999);
//        boolean r = Seq.of(DataSource.of(dotFile))
////                .forNext(e -> System.out.println(e));
////                .peek(System.out::println)
//                .anyMatch(edge -> Objects.equals(edge._1(), "A"));

//        System.out.println(res);
    }
}
