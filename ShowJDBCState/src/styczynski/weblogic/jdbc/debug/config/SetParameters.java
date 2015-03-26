package styczynski.weblogic.jdbc.debug.config;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import styczynski.weblogic.jdbc.monitor.JDBCmonitor;
import styczynski.weblogic.jdbc.debug.show.HTMLhelper;

public class SetParameters extends HttpServlet {
    private static final String CONTENT_TYPE = "text/html; charset=UTF-8";

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // SQL execution threadshold
        long sqlMaxExecutionTime = JDBCmonitor.getSqlMaxExecutionTime();
        // number of alerts to be kept in memory
        int topAlertsToStore = JDBCmonitor.getTopAlertsToStore();
        boolean printHeadersAlways = JDBCmonitor.isPrintHeadersAlways();
        boolean debugNormal = JDBCmonitor.isDebugNormal();
        boolean debugDetailed = JDBCmonitor.isDebugDetailed();
        boolean resetGlobalStatus = false;
        
        
        try {
            if (request.getParameter("sqlMaxExecutionTime") != null) {
                sqlMaxExecutionTime = Integer.parseInt(request.getParameter("sqlMaxExecutionTime"));
                JDBCmonitor.setSqlMaxExecutionTime(sqlMaxExecutionTime);
            }
            
//TODO Not available in array based alert queue
//            if (request.getParameter("topAlertsToStore") != null) {
//                topAlertsToStore = Integer.parseInt(request.getParameter("topAlertsToStore"));
//                ShowJDBCCalls.setTopAlertsToStore(topAlertsToStore);
//            }
            
            if (request.getParameter("printHeadersAlways") != null) {
                printHeadersAlways = Boolean.valueOf(request.getParameter("printHeadersAlways")).booleanValue();
                JDBCmonitor.setPrintHeadersAlways(printHeadersAlways);
            }
            if (request.getParameter("debugNormal") !=null) {
                debugNormal = Boolean.valueOf(request.getParameter("debugNormal")).booleanValue();
                JDBCmonitor.setDebugNormal(debugNormal);
            }
            
            if(request.getParameter("debugDetailed") !=null) {
                debugDetailed = Boolean.valueOf(request.getParameter("debugDetailed")).booleanValue();
                JDBCmonitor.setDebugDetailed(debugDetailed);
            }

            if(request.getParameter("resetGlobalStatus") != null) {
                resetGlobalStatus = true;
            }            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        response.setContentType(CONTENT_TYPE);
        PrintWriter out = response.getWriter();
        out.println("<html>");
        
        HTMLhelper.addHeaders(out, "SetParameters");
  
  
        out.println("<body>");
        out.println("<p>Current JDBC interceptor configuration:</p>");
        
        out.println("SqlMaxExecutionTime:" + JDBCmonitor.getSqlMaxExecutionTime() + "</br>");
        out.println("TopAlertsToStore:" + JDBCmonitor.getTopAlertsToStore() + "</br>");
        out.println("PrintHeadersAlways:" + JDBCmonitor.isPrintHeadersAlways() + "</br>");
        out.println("DebugNormal:" + JDBCmonitor.isDebugNormal() + "</br>");
        out.println("DebugDetailed:" + JDBCmonitor.isDebugDetailed()+ "</br>");
        
        //TODO Experimental
        if (resetGlobalStatus) {
            JDBCmonitor.getJdbcGlobalState().clear();;
            out.println("</br>");
            out.println("Global status cleared."  + "</br>");
        }
        out.println("</body></html>");
        out.close();
    }
}
