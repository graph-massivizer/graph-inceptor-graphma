package playground.examples;

import magma.control.function.Fn2;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static playground.examples.Container1.traverserFile;
import static playground.storage.Persist.exporter;

public class Container2 implements Fn2.Consumer<Path, Path> {

    public void accept(Path srcFile, Path tgtFile) {
        System.out.println("RUN CONTAINER II  [MERGE FILES] -- START");

        var graph = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
        var map = new HashMap<Integer, Set<Integer>>();

        traverserFile(srcFile).forNext(e -> {
            int rId = e._1;
            int pId = e._2;
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

        exporter(tgtFile, "dot");

        System.out.println("RUN CONTAINER II  [MERGE FILES] -- DONE");
    }
}
