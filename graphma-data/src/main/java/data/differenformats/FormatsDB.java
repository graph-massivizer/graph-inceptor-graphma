package data.differenformats;

import java.nio.file.Path;

import static data.Config.GRAPH_FORMATS;

public enum FormatsDB {
    ;

    public static final Path DIRECTED_DOT = GRAPH_FORMATS.resolve("dot").resolve("directed_graph.dot");

    public static final Path DIRECTED_GML = GRAPH_FORMATS.resolve("gml").resolve("directed_graph.gml");

    public static final Path DIRECTED_MTX = GRAPH_FORMATS.resolve("mtx").resolve("directed_graph.mtx");

    public static final Path DIRECTED_GRAPHML = GRAPH_FORMATS.resolve("graphml").resolve("directed_graph.graphml");
}
