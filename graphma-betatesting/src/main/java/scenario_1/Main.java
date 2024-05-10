package scenario_1;

import magma.control.exception.Exceptions;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import java.nio.file.Files;
import java.nio.file.Path;

public enum Main {
    ;

    public static void main(String[] args) throws org.apache.commons.cli.ParseException {
        var options = new Options()
                .addOption("s1", "stageI", false, "Start Stage I")
                .addOption("s2", "stageII", false, "Start Stage II")
                .addOption("s3", "stageIII", false, "Start Stage III");

        var cmd = new DefaultParser().parse(options, args);
        if (cmd.hasOption("s1")) {
            if (cmd.getArgs().length == 2) {
                var srcFolder = Path.of(cmd.getArgs()[0]);
                var tgtFile = Path.of(cmd.getArgs()[1]);
                if (!Files.isDirectory(srcFolder)) throw Exceptions.illegalArgument("SRC MUST BE A FOLDER");
                if (Files.exists(tgtFile)) throw Exceptions.illegalArgument("FILE ALREADY EXIST");
                new Stage1().accept(srcFolder, tgtFile);
            }
            else {
                throw Exceptions.illegalArgument("WE NEED AN INPUT ");
            }
        }
        else if (cmd.hasOption("s2")) {
            if (cmd.getArgs().length == 2) {
                var srcFile = Path.of(cmd.getArgs()[0]);
                var tgtFile = Path.of(cmd.getArgs()[1]);
                if (Files.notExists(srcFile)) throw Exceptions.illegalArgument("SRC FILE DOES NOT EXIST");
                if (Files.exists(tgtFile)) throw Exceptions.illegalArgument("TGT FILE ALREADY EXIST");
                new Stage2().accept(srcFile, tgtFile);
            }
            else {
                throw Exceptions.illegalArgument("WE NEED AN INPUT ");
            }
        }
        else if (cmd.hasOption("s3")) {
            if (cmd.getArgs().length == 1) {
                var srcFile = Path.of(cmd.getArgs()[0]);
                if (Files.notExists(srcFile)) throw Exceptions.illegalArgument("SRC FILE DOES NOT EXIST");
                new Stage3().accept(srcFile);
            }
            else {
                throw Exceptions.illegalArgument("WE NEED AN INPUT ");
            }
        }
    }
}
