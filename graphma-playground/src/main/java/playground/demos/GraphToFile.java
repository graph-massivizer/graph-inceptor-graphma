package playground.demos;

import data.suitesparse.SSDB;
import magma.control.function.Fn;
import magma.control.function.Fn0;
import magma.control.function.Fn1;
import magma.data.sequence.operator.DataSource;
import magma.data.sequence.operator.lazy.Filter;
import magma.data.sequence.operator.lazy.Peek;
import magma.data.sequence.operator.lazy.Take;
import magma.data.sequence.operator.strict.ForNext;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.ExportException;
import org.jgrapht.nio.csv.CSVExporter;
import org.jgrapht.nio.dot.DOTExporter;
import org.jgrapht.nio.gml.GmlExporter;
import org.jgrapht.nio.graphml.GraphMLExporter;
import org.jgrapht.nio.matrix.MatrixExporter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

public enum GraphToFile {
    ;

    public static final Path LOCAL_DATA_REPOSITORY = Path.of("demoDataRepo");

    public static final Path SUITE_SPARSE = LOCAL_DATA_REPOSITORY.resolve("suite_sparse");

    public static final Path TEMP = LOCAL_DATA_REPOSITORY.resolve("temp");


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
    
    public static void main(String[] args) {
        ForNext.build((Graph<Long, DefaultEdge> graph) -> exporter(TEMP.resolve("test.graphml"), "graphml").accept(graph))
                .compose(Peek.build(System.out::println))
                .compose(GraphPipeline.Graph.graph(DefaultEdge.class))
                .compose(Take.build(1))
                .compose(Filter.build((SSDB.MTXFile mtx) -> mtx.lines() < 40 && mtx.lines() > 20))
                .apply(DataSource.of(SSDB.SMALL))
                .evaluate();
    }
}
