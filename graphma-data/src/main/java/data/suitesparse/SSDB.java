package data.suitesparse;

import data.Config;
import formats.Mtx;
import magma.adt.value.product.Product2;
import magma.control.function.Fn;
import magma.control.function.Fn5;
import magma.control.traversal.Traversable;
import magma.control.traversal.Traverser;
import magma.data.sequence.operator.DataSource;
import magma.value.index.Range;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.lang.Integer.parseInt;

public enum SSDB implements Traversable<SSDB.MTXFile> {
    SMALL("small_graphs");

    final Path DIR;
    final List<MTXFile> files;

    SSDB(String file) { files = files(DIR = Config.SUITE_SPARSE.resolve(file)); }

    public Traverser<MTXFile> traverse() {
        return DataSource.of(files).traverse();
    }

    public record MTXFile(Path pth, int rows, int cols, int lines, int numCol) implements Traversable<Mtx.Long2LongEdge> {
        public Traverser<Mtx.Long2LongEdge> traverse() {
            return Mtx.traverse(pth, Range.of(0, lines), 0);
        }
    }

    static MTXFile parseHeader(Path path) {
        var reader = Fn.checked(() -> Files.newBufferedReader(path)).apply();
        Fn5<Path, Integer, Integer, Integer, Integer, MTXFile> factory = MTXFile::new;
        String line;
        do {
            line = Fn.checked(reader::readLine).apply();
        } while (line != null && line.startsWith("%"));
        var tokenizer = line.split("\\s+");
        var mtx = factory.apply(path)
                .apply(tokenizer.length > 0 ? parseInt(tokenizer[0]) : 0)
                .apply(tokenizer.length > 1 ? parseInt(tokenizer[1]) : 0)
                .apply(tokenizer.length > 2 ? parseInt(tokenizer[2]) : 0)
                .apply(Fn.checked(reader::readLine).apply().split("\\s+").length);
        Fn.checked(() -> { reader.close(); return true; }).apply();
        return mtx;
    }

    static List<MTXFile> files(Path root) {
        return Fn.checked(() -> Files.walk(root)).apply()
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".mtx"))
                .map(SSDB::parseHeader)
                .toList();
    }
}
