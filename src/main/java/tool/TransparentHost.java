package tool;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import proxy.TransparentHostProxy;

public class TransparentHost extends CommandLineTool {
    private static int DEFAULT_PORT = 9901;

    private static int MIN_PORT_VALUE = 1024;
    private static int MAX_PORT_VALUE = 65535;
    public static String  OPTIONS_HOSTS = "hosts";
    public static String  OPTIONS_PROXY_TO_URI = "proxyToUri";

    public static String USAGE = "Usage: java -jar <jar-file> " + arg(Runner.OPTION_TRANSPARENT_HOST) + " "
            +  arg(OPTIONS_HOSTS) + " <host> [,<host>]* " + arg(OPTIONS_PROXY_TO_URI) + " <proxy uri>" +
            " [" + arg(Runner.OPTION_PORT) + " <port>] " + arg(Runner.OPTIONS_CHATTY) + "] ["
            + arg(Runner.OPTIONS_SHUTDOWN_KEY) +" <key>]";

    public static void main(String[] args) {
        TransparentHost tool = new TransparentHost();
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
        return "Tool for running transparent host proxy server - transparently proxy list of host to another proxy.";
    }

    @Override
    public Options getCommandlineOptions() {
        Options options = new Options();
        Option help = new Option(Runner.OPTION_HELP, "Show usage information.");

        Option proxyTo = new Option(OPTIONS_PROXY_TO_URI, true,
                "Uri to transparently proxy to, e.g., 'http://localhost:9902'.");
        proxyTo.setRequired(true);

        // port
        Option port = new Option(Runner.OPTION_PORT, true, "Port transparent proxy server will listen on. Valid " +
                "range " + MIN_PORT_VALUE + "-" + MAX_PORT_VALUE + ". Default port is " + DEFAULT_PORT + ". Optional.");
        port.setRequired(false);
        port.setType(Number.class);

        // list of proxy hosts
        Option hosts = new Option(OPTIONS_HOSTS, true, "Comma separate list of host names the server " +
                "transparently proxy.");
        hosts.setRequired(true);
        hosts.setArgs(Option.UNLIMITED_VALUES);
        hosts.setValueSeparator(',');


        // proxyTo (host, port) http://localhost:${proxyToPort}

        // shutdownKey
        Option shutdownKey = new Option(Runner.OPTIONS_SHUTDOWN_KEY, true, "Key used for shutting down proxy server. "
                + "Optional.");
        shutdownKey.setRequired(false);

        // chatty
        Option chatty = new Option(Runner.OPTIONS_CHATTY, false, "Set debug output to high. Optional.");
        chatty.setRequired(false);


        options.addOption(help);
        options.addOption(new Option(Runner.OPTION_TRANSPARENT_HOST, "option to select this tool"));
        options.addOption(proxyTo);
        options.addOption(port);
        options.addOption(hosts);
        options.addOption(shutdownKey);
        options.addOption(chatty);
        return options;
    }

    @Override
    public void run(CommandLine cmd) {
        String shutdownKey = null;
        String shutdownContext = "/shutdown";
        int port = DEFAULT_PORT;
        String proxyTo = cmd.getOptionValue(OPTIONS_PROXY_TO_URI);
        if (cmd.hasOption(Runner.OPTION_PORT)) {
            if (!isInteger(cmd.getOptionValue(Runner.OPTION_PORT), MIN_PORT_VALUE, MAX_PORT_VALUE)) {
                System.err.println("Port (" + cmd.getOptionValue(Runner.OPTION_PORT) + ") must be integer in range " +
                        MIN_PORT_VALUE + "-" + MAX_PORT_VALUE);
                return;
            }
            port = parseInt(cmd.getOptionValue(Runner.OPTION_PORT));
        }
        String[] hosts = cmd.getOptionValues(OPTIONS_HOSTS);
        boolean chatty = cmd.hasOption(Runner.OPTIONS_CHATTY);
        if (cmd.hasOption(Runner.OPTIONS_SHUTDOWN_KEY)) {
            shutdownKey = cmd.getOptionValue(Runner.OPTIONS_SHUTDOWN_KEY);
        }

        TransparentHostProxy proxy = new TransparentHostProxy(hosts, proxyTo, chatty);
        proxy.setEnableShutdown(shutdownKey != null);
        proxy.setShutdownKey(shutdownKey);
        proxy.setShutdownContext(shutdownContext);

        Server server = new Server(port);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS |
                ServletContextHandler.NO_SECURITY);
        context.setContextPath("/");

        ServletHolder proxyServletHolder = new ServletHolder(proxy); // just to set init parameters
        proxyServletHolder.setInitParameter("viaHost", "transparent-host-proxy");
        context.addServlet(proxyServletHolder, "/*");
        server.setHandler(context);

        // now start the proxy server
        try {
            server.start();
            System.out.println("\nTransparent host proxy server running, listening on port " + port);
            System.out.println("Transparently proxy " + hosts + " to: " + proxyTo);
            if (shutdownKey != null) {
                System.out.println("Shutdown url http://localhost:" + port + shutdownContext + "/"
                        + shutdownKey + "\n");
            }
        } catch (Exception e) {
            System.err.println("Server got exception, " + e.getMessage());
        }
        try {
            server.join();
        } catch (InterruptedException ignored) {
        }
    }

}
