package proxy;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.proxy.ProxyServlet;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

public class TransparentHostProxy extends ProxyServlet {
    public static final String VIA_HEADER = "Transparent host proxy";
    String[] proxyHosts;  // hosts to transparently proxy
    String proxyTo;       // the server to transparently proxy to if host header matches proxyHosts (example http://localhost:9902)
    boolean chatty;       // if true, log rewrites to console
    private String shutdownKey = null;
    private String shutdownContext = "/shutdown"; // default shutdown http://localhost/shutdown/{key}
    private boolean enableShutdown = false;

    public TransparentHostProxy(String[] hosts, String proxy, boolean logRewrites) {
        if (hosts != null) {
            proxyHosts = new String[hosts.length];
            for (int idx = 0; idx < hosts.length; idx++) {
                proxyHosts[idx] = hosts[idx] == null ? "" : hosts[idx].trim();
            }
        } else {
            proxyHosts = new String[0];
        }
        proxyTo = proxy != null ? proxy : "";
        chatty = logRewrites;
    }

    protected URI rewriteURI(HttpServletRequest request) {
        if (!doProxy(request.getServerName())) {
            URI rewritten = super.rewriteURI(request);
            request.getProtocol();
            System.out.println("Dispatching " + request.getRequestURI() + " to " + rewritten);
            // System.out.println("Dispatching request to ${request.getServerName()}")
            //return super.rewriteURI(request);
            return rewritten;
        }
        String query = request.getQueryString() == null ? "" : "?" + request.getQueryString();
        URI rewrittenURI = URI.create(proxyTo + request.getRequestURI() + query).normalize();
        // no whitelist/blacklist check
        if (chatty) {
            System.out.println("Proxy '" + request.getRequestURL() + query +"' to '" + rewrittenURI + "'");
        }
        return rewrittenURI;
    }

    protected void service(final HttpServletRequest request, final HttpServletResponse response) throws
            ServletException, IOException {
        //System.out.println("service ${request.getServerName()} ${request.getRequestURI()} ${request.getQueryString()}")
        /* if (enableShutdown && request.getQueryString() != null && "localhost".equalsIgnoreCase(request.getServerName())
                && ("/shutdown" + "/" + shutdownKey).equals(request.getRequestURI())) {
            String key = "key=" + shutdownKey;
            if (shutdownKey != null && key.equals(request.getQueryString())) {
                System.out.println("Received shutdown signal");
                System.exit(0);
            }
        }*/
        if (enableShutdown && "localhost".equalsIgnoreCase(request.getServerName())
                && (shutdownContext + "/" + shutdownKey).equals(request.getRequestURI())) {
            System.out.println("Received shutdown signal");
            System.exit(0);
        }
        super.service(request, response);
    }

    public void setEnableShutdown(boolean enable) {
        enableShutdown = enable;
    }

    public void setShutdownKey(String key) {
        shutdownKey = key;
    }

    public void setShutdownContext(String context) {
        shutdownContext = context;
    }

    private boolean doProxy(String hostName) {
        for (String host : proxyHosts) {
            if (host.equals(hostName)) {
                return true;
            }
        }
        return false; // do not proxy
    }

    protected HttpClient newHttpClient() {
        System.out.println("Creating http client");
        SslContextFactory sslContextFactory = new SslContextFactory();
        return new HttpClient(sslContextFactory);
    }

    protected Request addViaHeader(Request proxyRequest) {
        System.out.println("addViaHeader");
        // proxyRequest.header(HttpHeader.VIA, VIA_HEADER);
        return proxyRequest;
    }

}
