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
import styczynski.weblogic.jdbc.debug.security.Authorization;
import styczynski.weblogic.jdbc.debug.show.HTMLhelper;
import styczynski.weblogic.jdbc.monitor.JDBCmonitor;
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
        boolean JDBCmonitoringDisabled = CFG.isJDBCmonitoringDisabled();

        
        try {

            if (request.getParameter("sqlMaxExecutionTime") != null) {
                sqlMaxExecutionTime = Integer.parseInt(request.getParameter("sqlMaxExecutionTime"));
                
                if(Authorization.canUserSetThreshold(request)) {
                    JDBCmonitor.setSqlMaxExecutionTime(sqlMaxExecutionTime);
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
            }
            
//TODO Not available in array based alert queue
//            if (request.getParameter("topAlertsToStore") != null) {
//                topAlertsToStore = Integer.parseInt(request.getParameter("topAlertsToStore"));
//                ShowJDBCCalls.setTopAlertsToStore(topAlertsToStore);
//            }
            
            if (request.getParameter("printHeadersAlways") != null) {
                printHeadersAlways = Boolean.valueOf(request.getParameter("printHeadersAlways")).booleanValue();
                if(Authorization.canUserControlDebug(request)) {
                    CFG.setPrintHeadersAlways(printHeadersAlways);
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
            }
            if (request.getParameter("debugNormal") !=null) {
                debugNormal = Boolean.valueOf(request.getParameter("debugNormal")).booleanValue();
                if(Authorization.canUserControlDebug(request)) {
                    CFG.setDebugNormal(debugNormal);
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
            }
            
            if(request.getParameter("debugDetailed") !=null) {
                debugDetailed = Boolean.valueOf(request.getParameter("debugDetailed")).booleanValue();
                if(Authorization.canUserControlDebug(request)) {
                    CFG.setDebugDetailed(debugDetailed);
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
            }

            if(request.getParameter("JDBCmonitoringDisabled") !=null) {
                JDBCmonitoringDisabled = Boolean.valueOf(request.getParameter("JDBCmonitoringDisabled")).booleanValue();
                if(Authorization.canUserEnableDisable(request)) {
                    CFG.setJDBCmonitoringDisabled(JDBCmonitoringDisabled);
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
            }
            
            if(request.getParameter("resetGlobalStatus") != null) {
                if(Authorization.canUserReset(request)) {
                    resetGlobalStatus = true;
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                }    
            }            
        } catch (Exception e) {
            e.printStackTrace();
        }
        response.setContentType(CONTENT_TYPE);
        PrintWriter out = response.getWriter();
        out.println("<html>");

        HTMLhelper.addHeaders(out, "SetParameters", request);
  
        out.println("<body>");
        
        if (resetGlobalStatus) {
            //enumerate by fsm objects
            final Enumeration<String> threadsEnum = Collections.enumeration(JDBCmonitor.getJdbcGlobalState().keySet());
            while (threadsEnum.hasMoreElements()) {

                String thread = threadsEnum.nextElement();
                JDBCcallFSMstate fsmState = JDBCmonitor.getJdbcGlobalState().get(thread);
                fsmState.initialize();
                
                //clear histograms
                fsmState.getTopHistograms().clear();
                fsmState.getTopHistograms().getReadBuffer().clear();
                
                //clear alerts
                fsmState.getTopAlerts().clear();
            }
            
            out.println("</br>");
            out.println("Global status cleared."  + "</br>");
        }
        
        
        out.println("<p>Current JDBC interceptor configuration:</p>");
        out.println("<code><FONT size=\"1\" FACE=\"courier\">");
        out.println("SqlMaxExecutionTime:" + JDBCmonitor.getSqlMaxExecutionTime() + "</br>");
        out.println("TopAlertsToStore:" + CFG.getTopAlertsToStore() + "</br>");
        out.println("PrintHeadersAlways:" + CFG.isPrintHeadersAlways() + "</br>");
        out.println("DebugNormal:" + CFG.isDebugNormal() + "</br>");
        out.println("DebugDetailed:" + CFG.isDebugDetailed()+ "</br>");
        out.println("JDBCmonitoringDisabled:" + CFG.isJDBCmonitoringDisabled() + "</br>");
        out.println("</code></FONT size=\"1\" FACE=\"courier\">");
        
        out.println("<p>Authorization:</p>");
                //http://stackoverflow.com/questions/16000517/how-to-get-password-from-http-basic-authentication
        //http://stackoverflow.com/questions/5549464/import-sun-misc-base64encoder-results-in-error-compiled-in-eclipse
        out.println("<code><FONT size=\"1\" FACE=\"courier\">");
        String authHeader = request.getHeader("authorization");
        if (authHeader != null && authHeader.startsWith("Basic")) {
            String base64Credentials = authHeader.substring("Basic".length()).trim();
            
            sun.misc.BASE64Decoder dec = new sun.misc.BASE64Decoder();
            String credentials = new String(dec.decodeBuffer(base64Credentials));
            out.println("User:" + credentials.split(":")[0]);
        }
        out.println("</code></FONT size=\"1\" FACE=\"courier\">");
        
        out.println("<p>User permissions:</p>");
        out.println("<code><FONT size=\"1\" FACE=\"courier\">");
        out.println("View...........:" + Authorization.canUserView(request) + "</br>");
        out.println("Reset..........:" + Authorization.canUserReset(request) + "</br>");
        out.println("Set threshold..:" + Authorization.canUserSetThreshold(request) + "</br>");
        out.println("Enable/Disable.:" + Authorization.canUserEnableDisable(request) + "</br>");
        out.println("Execute SQL....:" + Authorization.canUserExecuteSQL(request) + "</br>");
        out.println("Set debug......:" + Authorization.canUserControlDebug(request) + "</br>");
        out.println("Configure......:" + Authorization.canUserConfigure(request) + "</br>");
        out.println("</code></FONT size=\"1\" FACE=\"courier\">");
             
             
        out.println("</body></html>");
        out.close();
    }
}
