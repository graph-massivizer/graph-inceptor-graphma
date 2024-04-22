package data.suitesparse;

import data.Config;
import formats.Mtx;
import magma.control.function.Fn;
import magma.control.function.Fn5;
import magma.control.traversal.Traversable;
import magma.control.traversal.Traverser;
import magma.data.sequence.operator.DataSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;

/**
 * Enumeration that provides access to different collections of graphs in the SuiteSparse Matrix Collection
 * formatted as MTX files. This enum implements the {@link Traversable} interface to allow iterating over
 * the collection of MTX formatted graph data.
 *
 * <p>Each enumeration instance corresponds to a specific directory within the SuiteSparse repository,
 * containing multiple MTX files which represent graph data. The files are loaded and parsed to create
 * {@link Mtx.MTXFile} instances which can be traversed using the provided methods.</p>
 *
 * <p>Usage example:</p>
 * <pre>
 * {@code
 * Traverser<Mtx.MTXFile> traverser = SSDB.SMALL.traverse();
 * traverser.forEach(System.out::println);
 * }
 * </pre>
 */
public enum SSDB implements Traversable<Mtx.MTXFile> {
    /**
     * Represents a small collection of graphs.
     */
    SMALL("small_graphs");

    /**
     * Directory containing MTX files.
     */
    final Path DIR;

    /**
     * List of parsed MTX files from the directory.
     */
    final List<Mtx.MTXFile> files;

    /**
     * Constructs an instance of {@link SSDB} which loads MTX files from the specified directory.
     * @param file A sub-directory name under the SuiteSparse directory where the MTX files are stored.
     */
    SSDB(String file) { files = files(DIR = Config.SUITE_SPARSE.resolve(file)); }

    /**
     * Provides a traverser over the loaded MTX files.
     * @return {@link Traverser} for {@link Mtx.MTXFile}.
     */
    public Traverser<Mtx.MTXFile> traverse() {
        return DataSource.of(files).traverse();
    }

    /**
     * Parses the header of an MTX file to construct an {@link Mtx.MTXFile} object.
     * @param path The path to the MTX file.
     * @return A new {@link Mtx.MTXFile} instance representing the parsed file.
     */
    static Mtx.MTXFile parseHeader(Path path) {
        var reader = Fn.checked(() -> Files.newBufferedReader(path)).apply();
        Fn5<Path, Integer, Integer, Integer, Integer, Mtx.MTXFile> factory = Mtx.MTXFile::new;
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

    /**
     * Loads and parses all MTX files from the specified directory.
     * @param root The root directory to search for MTX files.
     * @return A list of {@link Mtx.MTXFile} instances representing each file found.
     */
    static List<Mtx.MTXFile> files(Path root) {
        try (Stream<Path> stream = Files.walk(root)) {
            return stream.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".mtx"))
                    .map(SSDB::parseHeader)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Error loading MTX files from directory", e);
        }
    }
}

