package workbenchserver;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.Enumeration;

import javax.servlet.*;
import javax.servlet.http.*;

import sun.misc.BASE64Decoder;

public class HeaderDump extends HttpServlet {
    private static final String CONTENT_TYPE = "text/html; charset=UTF-8";

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(CONTENT_TYPE);
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head><title>HeaderDump</title></head>");
        out.println("<body>");

        out.println("<h1>Request header dump</h1>");

        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
                String headerName = (String) headerNames.nextElement();
                out.print("<br/>Header Name: <em>" + headerName);
                String headerValue = request.getHeader(headerName);
                out.print("</em>, Header Value: <em>" + headerValue);
                out.println("</em>");
        }

        out.println("<hr/>");
        String authHeader = request.getHeader("authorization");
        out.println("Authorization Value: <em>" + authHeader);
        out.println("</em>");
        
        //http://stackoverflow.com/questions/16000517/how-to-get-password-from-http-basic-authentication
        //http://stackoverflow.com/questions/5549464/import-sun-misc-base64encoder-results-in-error-compiled-in-eclipse
        if (authHeader != null && authHeader.startsWith("Basic")) {
            String base64Credentials = authHeader.substring("Basic".length()).trim();
            
            sun.misc.BASE64Decoder dec = new sun.misc.BASE64Decoder();
            String credentials = new String(dec.decodeBuffer(base64Credentials));
            out.println("<br/>");
            out.println("Authorization credentials: <em>" + credentials);
            out.println("</em>");
        }
        out.println("</body></html>");
        out.close();
    }
}
