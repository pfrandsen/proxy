package proxy;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Proxy for a list of local directories. Each directory is exposed via a virtual host.
 */
public class LocalFileProxy {
    private static int DEFAULT_PORT = 9902;

    private ArrayList<ContextHandlerInterceptor> handlers;

    public LocalFileProxy() {
        handlers = new ArrayList<>();
    }

    public boolean appendHandler(String name, boolean chatty, String contextPath, String virtualHost, String path) {
        String uri = checkPathAndGetUri(path);
        if (uri.length() == 0) {
            System.err.println("Error: Could not create handler '" + name + "' for '" + path + "'.");
            return false;
        }
        try {
            return handlers.add(createContextHandler(name, chatty, contextPath, virtualHost, uri));
        } catch (MalformedURLException e) {
            System.err.println("Error: Could not add handler '" + name + "' for '" + path + "'. Malformed url.");
            return false;
        }
    }

    public int getDefaultPort() {
        return DEFAULT_PORT;
    }

    public void startServer() throws Exception {
        startServer(getDefaultPort());
    }

    public void startServer(int port) throws Exception {
        System.out.println("\nStarting file proxy server, mappings:");
        for (ContextHandlerInterceptor context : handlers) {
            for (String vh : context.getVirtualHosts()) {
                System.out.println("Virtual host: " + vh);
            }
        }
        System.out.println();
        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        Connector[] connectors = new ServerConnector[1];
        connectors[0] = connector;
        server.setConnectors(connectors);

        //setupHandlers(config)
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(handlers.toArray(new Handler[handlers.size()]));
        //contexts.setHandlers((Handler[])handlers)
        server.setHandler(contexts);
        server.start();
        System.out.println("\nFile proxy server is running, listening on port " + port + "\n");
        server.join();
    }

    private String checkPathAndGetUri(String path) {
        try {
            Path canonical = Paths.get(path).toRealPath();
            if (canonical.toFile().isDirectory()) {
                return canonical.toUri().toString();
            } else {
                System.err.println("'" + path + "' is not a directory.");
            }
        } catch (Exception e) {
            System.err.println("Caught exception while checking '" + path + "'.");
            System.err.println(e.toString());
        }
        return "";
    }


    private ContextHandlerInterceptor createContextHandler(String logicalName, boolean chatty, String contextPath,
                                                       String virtualHost, String uri) throws MalformedURLException {
        //String[] virtualHosts = {virtualHost};
        ContextHandlerInterceptor context = new ContextHandlerInterceptor();
        context.setDomainName(virtualHost);
        context.setLogicalName(logicalName);
        context.setDisplayMatch(chatty);
        if (contextPath != null && contextPath.length() > 0) {
            context.setContextPath(contextPath);
        }
        context.setVirtualHost(virtualHost);
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setBaseResource(Resource.newResource(uri));
        context.setHandler(resourceHandler);
        return context;
    }

    /*public static void main(String[] args) {
        ConfigParameters config = new ConfigParameters()
        CliBuilder cli = commandlineParser(config)
        def options = cli.parse(args)
        if (!options) {
            return
        }
        cli.usage()
        System.out.println("Starting deception schema server")
        Server server = new Server()
        ServerConnector connector = new ServerConnector(server)
        connector.setPort(config.port)
        server.setConnectors((Connector[])[connector])
        setupHandlers(config)
        ContextHandlerCollection contexts = new ContextHandlerCollection()
        contexts.setHandlers((Handler[])handlers)
        server.setHandler(contexts)
        server.start()
        System.out.println("Deception schema server running")
        server.join()
    }
*/
}
