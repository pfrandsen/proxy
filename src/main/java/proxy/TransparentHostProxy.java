package proxy;

import org.eclipse.jetty.proxy.ProxyServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

public class TransparentHostProxy extends ProxyServlet {
    String[] proxyHosts;  // hosts to transparently proxy
    String proxyTo;       // the server to transparently proxy to if host header matches proxyHosts (example http://localhost:9902)
    boolean chatty;       // if true, log rewrites to console
    private String shutdownKey = null;
    private boolean enableShutdown = false;

    public TransparentHostProxy(String[] hosts, String proxy, boolean logRewrites) {
        proxyHosts = hosts != null ? hosts : new String[0];
        proxyTo = proxy != null ? proxy : "";
        chatty = logRewrites;
    }

    protected URI rewriteURI(HttpServletRequest request) {
        if (!doProxy(request.getServerName())) {
            // System.out.println("Dispatching request to ${request.getServerName()}")
            return super.rewriteURI(request);
        }
        String query = request.getQueryString() == null ? "" : "?" + request.getQueryString();
        URI rewrittenURI = URI.create(proxyTo + request.getRequestURI() + query).normalize();
        // no whitelist/blacklist check
        if (chatty) {
            System.out.println("Proxy '" + request.getRequestURL() + query +"' to '" + rewrittenURI + "'");
        }
        return rewrittenURI;
    }

    protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        //System.out.println("service ${request.getServerName()} ${request.getRequestURI()} ${request.getQueryString()}")
        if (enableShutdown && request.getQueryString() != null && "localhost".equalsIgnoreCase(request.getServerName())
                && "/shutdown".equals(request.getRequestURI())) {
            String key = "key=" + shutdownKey;
            if (shutdownKey != null && key.equals(request.getQueryString())) {
                System.out.println("Received shutdown signal");
                System.exit(0);
            }
        }
        super.service(request, response);
    }

    public void setEnableShutdown(boolean enable) {
        enableShutdown = enable;
    }

    public void setShutdownKey(String key) {
        shutdownKey = key;
    }

/*  protected void onResponseHeaders(HttpServletRequest request, HttpServletResponse response, Response proxyResponse) {
    System.out.println("onResponseHeaders")
    for (HttpField field : proxyResponse.getHeaders()) {
      System.out.println("field.getName() + " " + field.getValue());
    }
    super.onResponseHeaders(request, response, proxyResponse);
  }
*/

    private boolean doProxy(String hostName) {
        for (String host : proxyHosts) {
            if (host.equals(hostName)) {
                return true;
            }
        }
        return false; // do not proxy
    }
}
