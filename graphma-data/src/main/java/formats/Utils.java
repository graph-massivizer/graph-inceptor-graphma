package formats;

import magma.control.function.Fn1;
import magma.control.function.Fn2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Path;

enum Utils {
    ;

    static final Fn2.Checked<BufferedReader, char[], Integer> readBuffer = Reader::read;

    static final Fn1.Checked<Path, BufferedReader> newReader = pth -> new BufferedReader(new FileReader(pth.toFile()));

    static final Fn1.Checked<BufferedReader, Boolean> closeReader = rea -> {
        rea.close();
        return true;
    };
}
