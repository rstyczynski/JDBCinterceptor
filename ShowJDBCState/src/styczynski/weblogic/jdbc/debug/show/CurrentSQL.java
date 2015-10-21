package styczynski.weblogic.jdbc.debug.show;

import java.io.IOException;
import java.io.PrintWriter;

import java.text.SimpleDateFormat;

import java.util.Collections;
import java.util.Enumeration;

import java.util.TimeZone;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.LogFactory;

import styczynski.weblogic.jdbc.debug.JDBCcallFSMstate;
import styczynski.weblogic.jdbc.monitor.JDBCmonitor;

//TODO currentSQL must show query start time, last activity time, and execution time.

public class CurrentSQL extends HttpServlet {
    private static final String CONTENT_TYPE = "text/html; charset=UTF-8";

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(CONTENT_TYPE);
    
        LogFactory.getLog("TEST").trace("trace");
        LogFactory.getLog("TEST").debug("DEBUG");
        LogFactory.getLog("TEST").info("INFO");
        LogFactory.getLog("TEST").warn("WARN");
        LogFactory.getLog("TEST").error("ERROR");
        LogFactory.getLog("TEST").fatal("FATAL");
        
        PrintWriter out = response.getWriter();
        out.println("<html>");

        HTMLhelper.addHeaders(out, "Current SQL");
                
        out.println("<body>");

        out.println("<div class=\"tabletitle\">" + "Current statements" + "</div>");
        out.println("<table class=\"datatable\" id=\"genericTableFormtable\">");
        out.println("<tbody>");
        out.println("<tr>");
        out.println("<th>" + "uptime [ms]" + "</th>");
        out.println("<th>" + "lasted [ms]" + "</th>");
        out.println("<th>" + "statement" + "</th>");
        out.println("<th>" + "state" + "</th>");
        out.println("<th>" + "modifiers" + "</th>");
        out.println("<th>" + "updated" + "</th>");
        out.println("<th>" + "state timing" + "</th>");
        out.println("<th>" + "interceptor" + "</th>");
        out.println("<th>" + "fsmState" + "</th>");

        out.println("</tr>");
        
        final Enumeration<String> jdbcEnum = Collections.enumeration(JDBCmonitor.getJdbcGlobalState().keySet());
      
//TODO Show sorted by execution time
// Problem: after converting to linked list, thread is lost.
        
//        LinkedList <JDBCcallFSMstate>globalState = new LinkedList<JDBCcallFSMstate>();
//            
//        while(threadsEnum.hasMoreElements()) {
//            String thread = threadsEnum.nextElement();            
//            //TODO Concurrent modification exception possible on getXX
//            JDBCcallFSMstate fsmState = ShowJDBCCalls.getJdbcGlobalState().get(thread);
//            //fsmState.setThread(thread);
//            globalState.add(fsmState);   
//        }
//             
//        //sort by execution time
//        Collections.sort(globalState, new Comparator<JDBCcallFSMstate>() {
//             @Override
//             public int compare(JDBCcallFSMstate o1, JDBCcallFSMstate o2) {
//                Long myTime = o1.getLasted();
//                Long otherTime = o2.getLasted();
//                return otherTime.compareTo(myTime);
//                
//             }
//           }
//        );
//        ListIterator <JDBCcallFSMstate>itr = globalState.listIterator();

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy 'at' hh:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("CET"));
        
        int rowNo=0;
        while(jdbcEnum.hasMoreElements()) {
            rowNo++;
            if (rowNo % 2 != 0) {
                out.println("<tr class=\"rowEven\">");
            } else {
                out.println("<tr class=\"rowOdd\">");
            } 
            
            String jdbcCall = jdbcEnum.nextElement();
            
            JDBCcallFSMstate fsmState = JDBCmonitor.getJdbcGlobalState().get(jdbcCall);

            out.println("<td>" + fsmState.getCurrentExecutionTime() + "</td>");
            out.println("<td>" + fsmState.getLasted() + "</td>");
            out.println("<td>" + fsmState.getStatement() + "</td>");
            out.println("<td>" + fsmState.getCurrentState() + "</td>");
            out.println("<td>" + fsmState.getModifiers() + "</td>");  
            out.println("<td>" + sdf.format(fsmState.getTimeUpdated()) + "</td>");
            out.println("<td>" + fsmState.getStatesTiming() + "</td>");
            out.println("<td>" + jdbcCall.replace("styczynski.weblogic.jdbc.monitor.", "") + "</td>");
            out.println("<td>" + Integer.toHexString(fsmState.hashCode()) + "</td>");  
            
            out.println("<tr>");
        }
        out.println("</tbody>");
        out.println("</table>");
        

        
        out.println("</body></html>");
        out.close();
    }
}
