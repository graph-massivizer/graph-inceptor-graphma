package scenario_1;

import magma.control.function.Fn2;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static scenario_1.Stage1.traverserFile;
import static scenario_1.Utils.exporter;

public class Stage2 implements Fn2.Consumer<Path, Path> {

    public void accept(Path srcFile, Path tgtFile) {
        System.out.println("RUN CONTAINER II  [MERGE FILES] -- START");

        var graph = new SimpleGraph<Long, DefaultEdge>(DefaultEdge.class);
        var map = new HashMap<Long, Set<Long>>();

        traverserFile(srcFile).forNext(e -> {
            long rId = e._1;
            long pId = e._2;
            graph.addVertex(rId);
            map.computeIfAbsent(pId, k -> new HashSet<>()).add(rId);
        });

        for (var collaborators : map.values()) {
            var rList = new ArrayList<>(collaborators);
            for (int i = 0; i < rList.size(); i++) {
                for (int j = i + 1; j < rList.size(); j++) {
                    graph.addEdge(rList.get(i), rList.get(j));
                }
            }
        }

        exporter(tgtFile, "graphml").accept(graph);
        System.out.println("RUN CONTAINER II  [MERGE FILES] -- DONE");
    }
}
