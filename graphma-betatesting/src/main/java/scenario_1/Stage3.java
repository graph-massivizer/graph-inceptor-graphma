package scenario_1;

import magma.control.function.Fn;
import magma.control.function.Fn1;
import magma.control.traversal.Traversable;
import org.jgrapht.Graph;
import org.jgrapht.alg.clustering.LabelPropagationClustering;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm.Clustering;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.jgrapht.nio.graphml.GraphMLImporter;

import java.io.FileReader;
import java.nio.file.Path;
import java.util.Set;

public class Stage3 implements Fn1.Consumer<Path> {

    private static Graph<Long, DefaultEdge> readGraphMl(Path pth) {
        final var graph = new DefaultUndirectedGraph<Long, DefaultEdge>(DefaultEdge.class);
        final var importer = new GraphMLImporter<Long, DefaultEdge>();
        importer.setVertexFactory(Long::parseLong);
        var reader = Fn.checked(() -> new FileReader(pth.toFile())).apply();
        importer.importGraph(graph, reader);
        return graph;
    }

    private static Clustering<Long> cluster(Graph<Long, DefaultEdge> graph) {
        var labelPropagationClustering = new LabelPropagationClustering<>(graph);
        return labelPropagationClustering.getClustering();
    }

    @Override
    public void accept(Path path) {
        Traversable.of(Set.of(path))
                        .forEach(p -> {
                            var g = readGraphMl(p);
                            var c = cluster(g);
                            System.out.println(c);
                        });
    }
}
