package tool;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Runner {
    // shared options for tools
    public static String OPTION_PORT = "port";
    public static String OPTIONS_CHATTY = "chatty";
    public static String OPTIONS_SHUTDOWN_KEY = "shutdownKey";
    public static String OPTION_HELP = "help";
    // options for selecting tool
    public static String OPTION_FILESYSTEM_PROXY = "filesystemProxy";
    public static String OPTION_TRANSPARENT_HOST = "transparentHostProxy";
    static private String USAGE = "to get help for tool use: java -jar <jar-file> -" + OPTION_HELP + " -<tool>";

    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption(new Option(OPTION_HELP, "Print this message"));
        options.addOption(new Option(OPTION_FILESYSTEM_PROXY, "Get help on file system proxy tool"));
        options.addOption(new Option(OPTION_TRANSPARENT_HOST, "Get help on transparent host proxy tool"));
        formatter.printHelp(USAGE, options);
    }

    private static void addDummyOption(Options options, String opt, boolean hasArg) {
        Option op = new Option(opt, hasArg, "");
        if (hasArg) {
            op.setArgName("arg");
        }
        options.addOption(op);
    }

    private static Options getCommandlineOptions() {
        Options options = new Options();
        options.addOption(OptionBuilder.create(OPTION_HELP));
        options.addOption(OptionBuilder.create(OPTION_FILESYSTEM_PROXY));
        options.addOption(OptionBuilder.create(OPTION_TRANSPARENT_HOST));

        // Add options for all the "sub" tools to avoid command line parsing error
        // shared option(s)
        addDummyOption(options, OPTION_PORT, true);
        addDummyOption(options, OPTIONS_CHATTY, false);
        addDummyOption(options, OPTIONS_SHUTDOWN_KEY, true);

        // options used by filesystem proxy tool
        addDummyOption(options, FileSystemProxy.OPTION_ROOT, true);
        addDummyOption(options, FileSystemProxy.OPTIONS_VIRTUAL_HOSTS, true);
        addDummyOption(options, FileSystemProxy.OPTIONS_SUBDIRECTORIES, true);
        addDummyOption(options, FileSystemProxy.OPTIONS_CONTEXT_PATHS, true);
        addDummyOption(options, FileSystemProxy.OPTIONS_LOGICAL_NAMES, true);
        addDummyOption(options, FileSystemProxy.OPTIONS_SHUTDOWN_CONTEXT, true);

        // options used by transparent host proxy tool
        addDummyOption(options, TransparentHost.OPTIONS_PROXY_TO_URI, true);
        addDummyOption(options, TransparentHost.OPTIONS_HOSTS, true);


        return options;
    }

    static CommandLineTool getTool(CommandLine cmd) {
        if (cmd.hasOption(OPTION_FILESYSTEM_PROXY)) {
            return new FileSystemProxy();
        }
        if (cmd.hasOption(OPTION_TRANSPARENT_HOST)) {
            return new TransparentHost();
        }
        return null;
    }

    public static void main(String[] args) {
        CommandLineParser parser = new GnuParser(); // replace with BasicParser when Apache commons-cli is released
        CommandLine cmd;
        try {
            Options options = getCommandlineOptions();
            // parse the command line arguments
            cmd = parser.parse(options, args);
        } catch (ParseException exp) {
            // oops, something went wrong
            System.out.println("\nParse error, " + exp.getMessage() + "\n");
            printHelp();
            return;
        }

        CommandLineTool tool = getTool(cmd);
        if (null == tool) {
            System.out.println("Required option -" + OPTION_FILESYSTEM_PROXY + " or -" + OPTION_TRANSPARENT_HOST +
                    " not provided");
            printHelp();
            return;
        }
        if (cmd.hasOption(OPTION_HELP)) {
            tool.printHelp();
            return;
        }

        try {
            cmd = tool.parseCommandLine(args);
        } catch (ParseException e) {
            tool.printHelp();
            return;
        }
        tool.run(cmd);
    }

}
