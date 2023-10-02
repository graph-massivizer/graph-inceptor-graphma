package playground.storage;

import magma.control.function.Fn;
import magma.control.function.Fn1;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.csv.CSVExporter;
import org.jgrapht.nio.dot.DOTExporter;
import org.jgrapht.nio.gml.GmlExporter;
import org.jgrapht.nio.graphml.GraphMLExporter;
import org.jgrapht.nio.matrix.MatrixExporter;

import java.io.FileWriter;
import java.nio.file.Path;

public enum Persist {
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
}
