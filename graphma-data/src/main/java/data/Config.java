package data;

import magma.control.exception.Exceptions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;

public enum Config {
    ;

    public static final Path LOCAL_DATA_REPOSITORY = findDemoDataRepoDirectoryPath();

    public static final Path SUITE_SPARSE = LOCAL_DATA_REPOSITORY.resolve("suite_sparse");

    public static final Path GRAPH_FORMATS = LOCAL_DATA_REPOSITORY.resolve("different_formats");

    public static final Path TEMP = LOCAL_DATA_REPOSITORY.resolve("temp");

    private static Path findDemoDataRepoDirectoryPath() throws NoSuchElementException {
        Path currentPath = Paths.get("").toAbsolutePath();
        while (currentPath != null) {
            Path potentialPath = currentPath.resolve("demoDataRepo");
            if (Files.exists(potentialPath) && Files.isDirectory(potentialPath)) {
                return potentialPath;
            }
            currentPath = currentPath.getParent();
        }
        throw Exceptions.noSuchElement("demoDataRepo is missing");
    }
}
