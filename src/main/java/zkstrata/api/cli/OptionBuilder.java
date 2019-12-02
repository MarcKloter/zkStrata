package zkstrata.api.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class OptionBuilder {
    private Options options = new Options();

    public Options build() {
        return this.options;
    }

    public OptionBuilder withLongOpts() {
        this.options.addOption(
                Option.builder()
                        .longOpt("statement")
                        .hasArg()
                        .argName("file")
                        .desc("file containing a zkStrata statement")
                        .required()
                        .build()
        );

        this.options.addOption(
                Option.builder()
                        .longOpt("witness-data")
                        .hasArgs()
                        .argName("alias=file")
                        .desc("files containing confidential information")
                        .build()
        );

        this.options.addOption(
                Option.builder()
                        .longOpt("instance-data")
                        .hasArgs()
                        .argName("alias=file")
                        .desc("files containing public information")
                        .build()
        );

        this.options.addOption(
                Option.builder()
                        .longOpt("schemas")
                        .hasArgs()
                        .argName("name=file")
                        .desc("files describing data structures")
                        .build()
        );

        this.options.addOption(
                Option.builder()
                        .longOpt("premises")
                        .hasArgs()
                        .argName("file")
                        .desc("files containing already proven statements")
                        .build()
        );

        this.options.addOption(
                Option.builder()
                        .longOpt("verbose")
                        .desc("use verbose output")
                        .build()
        );

        return this;
    }

    public OptionBuilder withFlags() {
        return this.withHelpFlag().withVersionFlag();
    }

    private OptionBuilder withHelpFlag() {
        this.options.addOption(Option.builder().longOpt("help").desc("display this help and exit").build());
        return this;
    }

    private OptionBuilder withVersionFlag() {
        this.options.addOption(Option.builder().longOpt("version").desc("output version information and exit").build());
        return this;
    }
}
