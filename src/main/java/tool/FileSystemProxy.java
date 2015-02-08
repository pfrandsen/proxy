package tool;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import proxy.LocalFileProxy;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FileSystemProxy extends CommandLineTool {
    private static int DEFAULT_PORT = 9902;
    private static int MIN_PORT_VALUE = 1024;
    private static int MAX_PORT_VALUE = 65535;
    public static String DEFAULT_SHUTDOWN_KEY = "12345";
    public static String SHUTDOWN_VIRTUAL_HOST = "localhost";
    public static String OPTION_ROOT = "root";
    public static String OPTIONS_VIRTUAL_HOSTS = "virtualHosts";
    public static String OPTIONS_SUBDIRECTORIES = "subdirectories";
    public static String OPTIONS_CONTEXT_PATHS = "contextPaths";
    public static String OPTIONS_LOGICAL_NAMES = "logicalNames";
    public static String OPTIONS_SHUTDOWN_CONTEXT = "shutdownContext";


    // context path, / -> no context path

    public static String USAGE = "Usage: java -jar <jar-file> " + arg(Runner.OPTION_FILESYSTEM_PROXY) + " " +
            arg(OPTION_ROOT) + " <directory> [" + arg(Runner.OPTION_PORT) + " <port>] " + arg(OPTIONS_VIRTUAL_HOSTS) +
            " <host> [,<host>]* " + arg(OPTIONS_SUBDIRECTORIES) + " <subdirectory> [,<subdirectory>]* " +
            arg(OPTIONS_CONTEXT_PATHS) + " <context> [,<context>]* " + arg(OPTIONS_LOGICAL_NAMES) +
            " <name> [,<name>]* [" + arg(Runner.OPTIONS_CHATTY) + "] ["  + arg(OPTIONS_SHUTDOWN_CONTEXT)
            + "<context> [" + arg(Runner.OPTIONS_SHUTDOWN_KEY) +" <key>]]";

    public static void main(String[] args) {
        FileSystemProxy tool = new FileSystemProxy();
        CommandLine cmd;
        try {
            cmd = tool.parseCommandLine(args);
        } catch (ParseException e) {
            tool.printHelp();
            return;
        }
        tool.run(cmd);
    }

    @Override
    public String getUsageString() {
        return USAGE;
    }

    @Override
    public String getToolDescription() {
        return "Tool for running file system proxy server - proxy virtual hosts to local directories.";
    }

    @Override
    public Options getCommandlineOptions() {
        Options options = new Options();
        Option help = new Option(Runner.OPTION_HELP, "Show usage information.");
        Option root = new Option(OPTION_ROOT, true,
                "Root directory. All virtual host to subdirectory mappings are relative to this.");
        root.setRequired(true);
        root.setArgName("directory");
        Option port = new Option(Runner.OPTION_PORT, true, "Port proxy server will listen on. Valid range " +
                MIN_PORT_VALUE + "-" + MAX_PORT_VALUE + ". Default port is " + DEFAULT_PORT + ". Optional.");
        port.setRequired(false);
        port.setType(Number.class);

        Option virtualHosts = new Option(OPTIONS_VIRTUAL_HOSTS, true, "Comma separate list of host names. List " +
                "entries are matched with list of subdirectories.");
        virtualHosts.setRequired(true);
        virtualHosts.setArgs(Option.UNLIMITED_VALUES);
        virtualHosts.setValueSeparator(',');

        Option subdirectories = new Option(OPTIONS_SUBDIRECTORIES, true, "Comma separate list of subdirectories. List "
                + "entries are matched with list of virtual hosts.");
        subdirectories.setRequired(true);
        subdirectories.setArgs(Option.UNLIMITED_VALUES);
        subdirectories.setValueSeparator(',');

        Option contextPaths = new Option(OPTIONS_CONTEXT_PATHS, true, "Comma separate list of context paths. List "
                + "entries are matched with list of virtual hosts. Use '/' for 'no' context path. Optional.");
        contextPaths.setRequired(false);
        contextPaths.setArgs(Option.UNLIMITED_VALUES);
        contextPaths.setValueSeparator(',');

        Option logicalNames = new Option(OPTIONS_LOGICAL_NAMES, true, "Comma separate list of logical names, one "
                + "name for each virtual host. Names are used in debug messages. Optional.");
        logicalNames.setRequired(false);
        logicalNames.setArgs(Option.UNLIMITED_VALUES);
        logicalNames.setValueSeparator(',');

        Option shutdownContext = new Option(OPTIONS_SHUTDOWN_CONTEXT, true, "If provided proxy will listen to "
                + "shutdown requests on " + SHUTDOWN_VIRTUAL_HOST + "/<context>/{" + Runner.OPTIONS_SHUTDOWN_KEY
                        + "}. Optional.");
        shutdownContext.setRequired(false);

        Option shutdownKey = new Option(Runner.OPTIONS_SHUTDOWN_KEY, true, "Key used for shutting down proxy server. "
                + "Default value is '" + DEFAULT_SHUTDOWN_KEY + "'. See description of " + arg(OPTIONS_SHUTDOWN_CONTEXT)
                + " for format of shutdown request. Key is ignored if " + arg(OPTIONS_SHUTDOWN_CONTEXT)
                + " is not provided. Optional.");
        shutdownKey.setRequired(false);

        Option chatty = new Option(Runner.OPTIONS_CHATTY, false, "Set debug output to high. Optional.");
        chatty.setRequired(false);

        options.addOption(help);
        options.addOption(new Option(Runner.OPTION_FILESYSTEM_PROXY, "option to select this tool"));
        options.addOption(root);
        options.addOption(port);
        options.addOption(virtualHosts);
        options.addOption(subdirectories);
        options.addOption(contextPaths);
        options.addOption(logicalNames);
        options.addOption(shutdownContext);
        options.addOption(shutdownKey);
        options.addOption(chatty);
        return options;
    }

    @Override
    public void run(CommandLine cmd) {
        LocalFileProxy proxy = new LocalFileProxy();
        int port = DEFAULT_PORT; // proxy.getDefaultPort();
        Path root = Paths.get(cmd.getOptionValue(OPTION_ROOT));
        if (cmd.hasOption(Runner.OPTION_PORT)) {
            if (!isInteger(cmd.getOptionValue(Runner.OPTION_PORT), MIN_PORT_VALUE, MAX_PORT_VALUE)) {
                System.err.println("Port (" + cmd.getOptionValue(Runner.OPTION_PORT) + ") must be integer in range " +
                MIN_PORT_VALUE + "-" + MAX_PORT_VALUE);
                return;
            }
            port = parseInt(cmd.getOptionValue(Runner.OPTION_PORT));
        }
        String[] virtualHosts = cmd.getOptionValues(OPTIONS_VIRTUAL_HOSTS);
        String[] subdirectories = cmd.getOptionValues(OPTIONS_SUBDIRECTORIES);
        if (subdirectories.length != virtualHosts.length) {
            System.err.println("Number of virtual hosts (" + virtualHosts.length +
                    ") must match number of subdirectories (" + subdirectories.length + ")");
            return;
        }
        String[] contextPaths = null;
        if (cmd.hasOption(OPTIONS_CONTEXT_PATHS)) {
            contextPaths = cmd.getOptionValues(OPTIONS_CONTEXT_PATHS);
            if (contextPaths.length != virtualHosts.length) {
                System.err.println("Number of context paths (" + contextPaths.length +
                        ") must match number of virtual hosts (" + virtualHosts.length + ")");
                return;
            }
        }
        String[] logicalNames = null;
        if (cmd.hasOption(OPTIONS_LOGICAL_NAMES)) {
            logicalNames = cmd.getOptionValues(OPTIONS_LOGICAL_NAMES);
            if (logicalNames.length != virtualHosts.length) {
                System.err.println("Number of logical names (" + logicalNames.length +
                        ") must match number of virtual hosts (" + virtualHosts.length + ")");
                return;
            }
        }
        boolean chatty = cmd.hasOption(Runner.OPTIONS_CHATTY);

        // create the context handlers
        for (int idx = 0; idx < virtualHosts.length; idx++) {
            String virtualHost = virtualHosts[idx].trim();
            String name = virtualHost.split("\\.")[0];
            String contextPath = "";
            if (contextPaths != null) {
                contextPath = contextPaths[idx].trim();
            }
            if (logicalNames != null) {
                name = logicalNames[idx].trim();
            }
            String uri = root.resolve(subdirectories[idx].trim()).toString();
            if (!proxy.appendContextHandler(name, chatty, contextPath, virtualHost, uri)) {
                System.err.println("Could not create context handler for " + virtualHost);
                return;
            }
        }
        // add optional shutdown handler
        if (cmd.hasOption(OPTIONS_SHUTDOWN_CONTEXT)) {
            String context = cmd.getOptionValue(OPTIONS_SHUTDOWN_CONTEXT);
            String key = DEFAULT_SHUTDOWN_KEY;
            if (cmd.hasOption(Runner.OPTIONS_SHUTDOWN_KEY)) {
                key = cmd.getOptionValue(Runner.OPTIONS_SHUTDOWN_KEY);
            }
            proxy.appendShutdownHandler(context, key);
        }
        // now start the server
        try {
            proxy.startServer(port);
        } catch (Exception e) {
            System.err.println("Server got exception, " + e.getMessage());
        }
    }

}
