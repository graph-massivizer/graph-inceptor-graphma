package playground.sdexample;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Main {

    public static void main(String[] args) throws ParseException {
        var options = new Options()
                .addOption("c1", "containerI", false, "Start Container I")
                .addOption("c2", "containerII", false, "Start Container II")
                .addOption("c3", "containerIII", false, "Start Container III");

        var cmd = new DefaultParser().parse(options, args);

        if (cmd.hasOption("c1")) {

        }
        else if (cmd.hasOption("c2")) {

        }
        else if (cmd.hasOption("c3")) {

        }
    }
}
