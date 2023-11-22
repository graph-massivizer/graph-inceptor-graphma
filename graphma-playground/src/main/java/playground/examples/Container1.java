package playground.examples;

import magma.base.Cast;
import magma.control.function.Fn;
import magma.control.function.Fn2;
import magma.control.traversal.Traverser;
import magma.value.Unit;
import magma.value.tuple.Tuple;
import magma.value.tuple.Tuple5;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.Files.newBufferedReader;
import static playground.examples.Utils.merge;

public class Container1 implements Fn2.Consumer<Path, Path> {

    static Traverser<Tuple5<Integer, Integer, String, String, Integer>> traverserFile(Path pth) {
        return Fn.checked(() -> newBufferedReader(pth))
                .map(BufferedReader::lines)
                .map(lines -> lines.skip(1)
                        .map(line -> line.split(","))
                        .map(ar -> Tuple.of(
                                Integer.valueOf(ar[0]),
                                Integer.valueOf(ar[1]),
                                ar[2],
                                ar[3],
                                Integer.valueOf(ar[4])))
                        .spliterator())
                .map(Traverser::of)
                .apply(Unit.value);
    }

    static Traverser<Tuple5<Integer, Integer, String, String, Integer>> traverserAll(Path root) {
        final Traverser<Tuple5<Integer, Integer, String, String, Integer>>[] array =
                Cast.force(Fn.checked(() -> Files.walk(root)).apply()
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".csv"))
                .map(Container1::traverserFile)
                .toArray(Traverser[]::new));
        return merge(array);
    }

    static void writeToFile(Traverser<Tuple5<Integer, Integer, String, String, Integer>> traverser, Path pth) {
        try {
            if (Files.notExists(pth)) Files.createFile(pth);
            var writer =  new BufferedWriter(new FileWriter(pth.toFile()));
            writer.write("ResearcherID,PaperID,Name,Title,Year\n");
            traverser.forNext(e -> {
                        try { writer.write(e._1 + "," + e._2 + "," + e._3 + "," + e._4 + "," + e._5 + "\n"); }
                            catch (IOException ex) { throw new RuntimeException(ex); }
            });
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void accept(Path srcDir, Path tgtFile) {
        System.out.println("RUN CONTAINER I   [MERGE FILES] -- START");
        var master  = Container1.traverserAll(srcDir);
        writeToFile(master, tgtFile);
        System.out.println("RUN CONTAINER I   [MERGE FILES] -- DONE");
    }
}
