package styczynski.weblogic.jdbc.debug.config;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import styczynski.weblogic.jdbc.debug.JDBCcallFSMstate;
import styczynski.weblogic.jdbc.debug.report.ExecutionHistogram;
import styczynski.weblogic.jdbc.monitor.JDBCmonitor;
import styczynski.weblogic.jdbc.debug.show.HTMLhelper;
import styczynski.weblogic.jdbc.monitor.CFG;

public class SetParameters extends HttpServlet {
    private static final String CONTENT_TYPE = "text/html; charset=UTF-8";

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // SQL execution threadshold
        int sqlMaxExecutionTime = JDBCmonitor.getSqlMaxExecutionTime();
        // number of alerts to be kept in memory
        int topAlertsToStore = CFG.getTopAlertsToStore();
        boolean printHeadersAlways = CFG.isPrintHeadersAlways();
        boolean debugNormal = CFG.isDebugNormal();
        boolean debugDetailed = CFG.isDebugDetailed();
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
                CFG.setPrintHeadersAlways(printHeadersAlways);
            }
            if (request.getParameter("debugNormal") !=null) {
                debugNormal = Boolean.valueOf(request.getParameter("debugNormal")).booleanValue();
                CFG.setDebugNormal(debugNormal);
            }
            
            if(request.getParameter("debugDetailed") !=null) {
                debugDetailed = Boolean.valueOf(request.getParameter("debugDetailed")).booleanValue();
                CFG.setDebugDetailed(debugDetailed);
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
        out.println("TopAlertsToStore:" + CFG.getTopAlertsToStore() + "</br>");
        out.println("PrintHeadersAlways:" + CFG.isPrintHeadersAlways() + "</br>");
        out.println("DebugNormal:" + CFG.isDebugNormal() + "</br>");
        out.println("DebugDetailed:" + CFG.isDebugDetailed()+ "</br>");
        
        //TODO Experimental
        if (resetGlobalStatus) {
            //enumerate by fsm objects
            final Enumeration<String> threadsEnum = Collections.enumeration(JDBCmonitor.getJdbcGlobalState().keySet());
            while (threadsEnum.hasMoreElements()) {

                String thread = threadsEnum.nextElement();
                JDBCcallFSMstate fsmState = JDBCmonitor.getJdbcGlobalState().get(thread);
                fsmState.initialize();
                fsmState.getTopHistograms().clear();
                fsmState.getTopHistograms().getReadBuffer().clear();;
            }
            
            out.println("</br>");
            out.println("Global status cleared."  + "</br>");
        }
        out.println("</body></html>");
        out.close();
    }
}
