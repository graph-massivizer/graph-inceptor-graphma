package playground.examples;

import magma.control.exception.Exceptions;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws ParseException {
        var options = new Options()
                .addOption("c1", "containerI", false, "Start Container I")
                .addOption("c2", "containerII", false, "Start Container II")
                .addOption("c3", "containerIII", false, "Start Container III");

        var cmd = new DefaultParser().parse(options, args);
        if (cmd.hasOption("c1")) {
            if (cmd.getArgs().length == 2) {
                var srcFolder = Path.of(cmd.getArgs()[0]);
                var tgtFile = Path.of(cmd.getArgs()[1]);
                if (!Files.isDirectory(srcFolder)) throw Exceptions.illegalArgument("SRC MUST BE A FOLDER");
                if (Files.exists(tgtFile)) throw Exceptions.illegalArgument("FILE ALREADY EXIST");
                new Container1().accept(srcFolder, tgtFile);
            }
            else {
                throw Exceptions.illegalArgument("WE NEED AN INPUT ");
            }
        }
        else if (cmd.hasOption("c2")) {
            if (cmd.getArgs().length == 2) {
                var srcFile = Path.of(cmd.getArgs()[0]);
                var tgtFile = Path.of(cmd.getArgs()[1]);
                if (Files.notExists(srcFile)) throw Exceptions.illegalArgument("SRC FILE DOES NOT EXIST");
                if (Files.exists(tgtFile)) throw Exceptions.illegalArgument("TGT FILE ALREADY EXIST");
                new Container2().accept(srcFile, tgtFile);
            }
            else {
                throw Exceptions.illegalArgument("WE NEED AN INPUT ");
            }
        }
        else if (cmd.hasOption("c3")) {
            new Container3().run();
        }
    }
}
