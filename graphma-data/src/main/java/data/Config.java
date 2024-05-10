package data;

import magma.control.exception.Exceptions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;

/**
 * A utility class to manage configuration paths for data used throughout the application.
 * This class specifically handles the identification and storage of directory paths for various data categories,
 * such as demo data repositories and specific graph format files.
 */
public enum Config {
    ;

    /**
     * The root path to the local data repository containing demo data.
     * This path is dynamically identified at runtime based on the application's current working directory.
     */
    public static final Path LOCAL_DATA_REPOSITORY = findDemoDataRepoDirectoryPath();

    /**
     * Path to the directory containing SuiteSparse graph files within the local data repository.
     */
    public static final Path SUITE_SPARSE = LOCAL_DATA_REPOSITORY.resolve("suite_sparse");

    /**
     * Path to the directory containing graphs in different formats within the local data repository.
     */
    public static final Path GRAPH_FORMATS = LOCAL_DATA_REPOSITORY.resolve("different_formats");

    /**
     * Path to a temporary directory within the local data repository, used for temporary storage during processing.
     */
    public static final Path TEMP = LOCAL_DATA_REPOSITORY.resolve("temp");

    /**
     * Finds the directory path for the demo data repository by ascending from the current working directory
     * until it finds a directory named "demoDataRepo". Throws an exception if the directory is not found.
     *
     * @return the absolute path to the demo data repository
     * @throws NoSuchElementException if the demo data repository directory is not found in any parent directory
     */
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

