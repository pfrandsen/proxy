package proxy;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ContextHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Class for extending ContextHandler with logging and header handling
 */
public class ContextHandlerInterceptor extends ContextHandler {
    private String domainName = "";
    private String logicalName = "";
    private boolean displayMatch = false;
    private boolean displayRequestHeaders = false;

    public void doHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        super.doHandle(target, baseRequest, request, response);
        if (baseRequest.isHandled()) {
            if (displayRequestHeaders) {
                Enumeration headerNames = request.getHeaderNames();
                while (headerNames.hasMoreElements()) {
                    String key = (String) headerNames.nextElement();
                    System.out.println(key + ": " + request.getHeader(key));
                }
            }
            // TODO: figure out how to set a response header - the normal addHeader does not seem to work
            response.addHeader("X-deception-server", logicalName);
            if (displayMatch) {
                System.out.println("'" + logicalName + "' resolved request '" + target + "', status code " +
                        response.getStatus() + " " + domainName);
            }
        }
    }

    protected void doStart() throws Exception {
        System.out.println("Starting '${logicalName}' handler");
        super.doStart();
    }

    public void setDomainName(String name) {
        domainName = name == null ? "" : name;
    }

    public void setLogicalName(String name) {
        logicalName = name == null ? "" : name;
    }

    public void setDisplayMatch(boolean display) {
        displayMatch = display;
    }

    public void setDisplayRequestHeaders(boolean display) {
        displayRequestHeaders = display;
    }

    public void setVirtualHost(String virtualHost) {
        String[] virtualHosts = new String[1];
        virtualHosts[0] = virtualHost;
        setVirtualHosts(virtualHosts);
    }

}
