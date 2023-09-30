package data;

import java.nio.file.Path;

public enum Config {
    ;

    public static final Path LOCAL_DATA_REPOSITORY = Path.of("demoDataRepo");

    public static final Path SUITE_SPARSE = LOCAL_DATA_REPOSITORY.resolve("suite_sparse");
}
