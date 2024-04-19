package parser;

import magma.control.exception.Exceptions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;

public enum Utils {
    ;

    static Path findbenchmarkResultPath() throws NoSuchElementException {
        Path currentPath = Paths.get("").toAbsolutePath();
        while (currentPath != null) {
            Path potentialPath = currentPath.resolve("benchmarkResults");
            if (Files.exists(potentialPath) && Files.isDirectory(potentialPath)) {
                return potentialPath;
            }
            currentPath = currentPath.getParent();
        }
        throw Exceptions.noSuchElement("demoDataRepo is missing");
    }
}
