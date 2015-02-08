package proxy;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ContextHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Handler listening for shutdown requests.
 * Listens for shutdown requests on {virtual host}/{context path}/shutdown-key e.g., localhost/shutdown/shutdown-key
 */
public class ShutdownContextHandler  extends ContextHandler {

    private String shutdownKey;

    /**
     * @param shutdownKey
     */
    public ShutdownContextHandler(String shutdownKey) {
        this.shutdownKey = shutdownKey.replaceAll("\\s", "");
    }

    public void doHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        super.doHandle(target, baseRequest, request, response);

        if ("GET".equals(request.getMethod())) {
            System.out.println("Shutdown handler GET");
            System.out.println("Shutdown handler" + request.getRequestURI());
            String shutdownUri = request.getContextPath() + "/" + shutdownKey;
            if (shutdownUri.equals(request.getRequestURI())) {
                System.out.println("Shutdown handler - received valid shutdown request");
                System.out.println("Shutting down - bye...");
                writeResponse("Proxy server received shutdown signal", response, 200);
                System.exit(0);
            } else {
                System.out.println("Shutdown handler - invalid shutdown request '" + request.getRequestURI() + "'");
            }

        } else {
            System.out.println("Shutdown handler ignoring " + request.getMethod() + " request");
        }
        writeResponse("Invalid shutdown request", response, 400); // 400: bad request
    }

    protected void doStart() throws Exception {
        System.out.println("Starting shutdown handler");
        super.doStart();
    }

    private void writeResponse(String message, HttpServletResponse response, int statusCode) throws IOException {
        response.setStatus(statusCode);
        PrintWriter res = response.getWriter();
        res.println("<html><body>");
        res.println("<h1>" + message + "</h1>");
        res.println("</body><html>");
        res.flush();
        response.flushBuffer();
    }
}
