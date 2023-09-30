package data.suitesparse;

import data.Config;
import magma.base.Cast;
import magma.control.Option;
import magma.control.function.Fn;
import magma.control.function.Fn1;
import magma.control.function.Fn5;
import magma.data.sequence.operator.DataSource;
import magma.data.sequence.operator.lazy.Filter;
import magma.data.sequence.operator.lazy.Map;
import magma.data.sequence.operator.lazy.Peek;
import magma.data.sequence.operator.strict.Collect;
import magma.data.sequence.operator.strict.First;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static data.suitesparse.SuiteSparse.*;

public enum SuiteSparse implements Fn1<Fn1.Predicate<MTXMeta>, List<MTXMeta>> {
    SMALL("small_graphs");

    final Path DIR;

    SuiteSparse(String file) { DIR = Config.SUITE_SPARSE.resolve(file); }

    public Option<MTXMeta> findFirst(Predicate<MTXMeta> query) {
        var ds = DataSource.of(apply(query));
        return Cast.narrow(First.build().apply(ds).evaluate());
    }

    @Override
    public List<MTXMeta> apply(Predicate<MTXMeta> query) {
        var filter1 = Filter.build((Path p) -> Files.isRegularFile(p));
        var filter2 = Filter.build((Path p) -> p.getFileName().toString().endsWith(".mtx"));
        var filter3 = Filter.build(query);
        var map = Map.build(MTXMeta::parseHeader);
        var source = DataSource.of(Fn.checked(() -> Files.walk(DIR)).apply().toList());
        return Cast.narrow(Collect.build(ArrayList::new, ArrayList::add)
                .compose(filter3)
                .compose(map)
                .compose(filter2)
                .compose(filter1)
                .apply(source)
                .evaluate());
    }

    public record MTXMeta(int rows, int cols, int entries, int dataStartLine, int entriesPerLine, Path pth) {
        boolean isSquare() { return rows == cols; }
        boolean isWeighted() { return entriesPerLine == 3; }
        public static MTXMeta parseHeader(Path path) {
            MTXMeta header = null;
            try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {
                String line;
                int lineCount = 0;
                // Skip over comment lines
                while ((line = reader.readLine()) != null) {
                    lineCount++;
                    if (!line.startsWith("%")) {
                        break;
                    }
                }
                // At this point, 'line' should contain the matrix dimensions and metadata
                if (line != null) {
                    var tokenizer = new StringTokenizer(line);
                    int rows = Integer.parseInt(tokenizer.nextToken());
                    int cols = Integer.parseInt(tokenizer.nextToken());
                    int entries = 0;
                    if (tokenizer.hasMoreTokens()) {
                        entries = Integer.parseInt(tokenizer.nextToken());
                    }
                    int dataStartLine = lineCount + 1;
                    line = reader.readLine();
                    int entriesPerLine = 0;
                    if (line != null) {
                        entriesPerLine = line.split("\\s+").length;
                    }

                    header = new MTXMeta(
                            rows,
                            cols,
                            entries,
                            dataStartLine,
                            entriesPerLine,
                            path);
                }
                return header;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {
        Predicate<MTXMeta> pred = mtxMeta -> mtxMeta.isSquare()
                && !mtxMeta.isWeighted()
                && mtxMeta.entries > 20;
        var r = SuiteSparse.SMALL.findFirst(pred).value();
        System.out.println(r);
    }
}
