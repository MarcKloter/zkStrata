package zkstrata.compiler.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class OptionBuilder {
    public static Options build() {
        Options options = new Options();

        options.addOption(
                Option.builder()
                        .longOpt("statement")
                        .hasArg()
                        .argName("file")
                        .desc("file containing a zkStrata statement")
                        .required()
                        .build()
        );

        options.addOption(
                Option.builder()
                        .longOpt("witness-data")
                        .hasArgs()
                        .argName("alias=file")
                        .desc("files containing confidential information")
                        .build()
        );

        options.addOption(
                Option.builder()
                        .longOpt("meta-data")
                        .hasArgs()
                        .argName("alias=file")
                        .desc("files containing metadata for confidential information")
                        .build()
        );

        options.addOption(
                Option.builder()
                        .longOpt("instance-data")
                        .hasArgs()
                        .argName("alias=file")
                        .desc("files containing public information")
                        .build()
        );

        options.addOption(
                Option.builder()
                        .longOpt("schemas")
                        .hasArgs()
                        .argName("name=file")
                        .desc("files describing data structures")
                        .build()
        );

        options.addOption(
                Option.builder()
                        .longOpt("verbose")
                        .desc("use verbose output")
                        .build()
        );

        options.addOption(helpFlag());
        options.addOption(versionFlag());

        return options;
    }

    public static Options buildFlagOptions() {
        Options options = new Options();

        options.addOption(helpFlag());
        options.addOption(versionFlag());

        return options;
    }

    private static Option helpFlag() {
        return Option.builder().longOpt("help").desc("display this help and exit").build();
    }

    private static Option versionFlag() {
        return Option.builder().longOpt("version").desc("output version information and exit").build();
    }
}
