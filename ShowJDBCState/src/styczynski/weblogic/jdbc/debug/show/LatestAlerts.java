package styczynski.weblogic.jdbc.debug.show;

import java.io.IOException;
import java.io.PrintWriter;

import java.text.SimpleDateFormat;

import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.TimeZone;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import styczynski.weblogic.jdbc.debug.JDBCcallFSMstate;
import styczynski.weblogic.jdbc.debug.report.ExecutionAlert;
import styczynski.weblogic.jdbc.monitor.CFG;
import styczynski.weblogic.jdbc.monitor.JDBCmonitor;

public class LatestAlerts extends HttpServlet {
    private static final String CONTENT_TYPE = "text/html; charset=UTF-8";

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(CONTENT_TYPE);

        PrintWriter out = response.getWriter();
        HTMLhelper.addHeaders(out, "LatestAlerts", request);

        final Enumeration<String> threadsEnum = Collections.enumeration(JDBCmonitor.getJdbcGlobalState().keySet());

        LinkedList<ExecutionAlert> allAlerts = new LinkedList<ExecutionAlert>();

        while (threadsEnum.hasMoreElements()) {

            String thread = threadsEnum.nextElement();
            //TODO Concurrent modification exception possible on getXX
            JDBCcallFSMstate fsmState = JDBCmonitor.getJdbcGlobalState().get(thread);

            //get alerts from this thread
            if (fsmState.getTopAlerts() != null) {
                ExecutionAlert[] alerts=fsmState.getTopAlerts().getAlerts();
                for(int i=0; i<alerts.length;i++){
                    if (alerts[i]!=null)
                        allAlerts.add(alerts[i]);
                }
                
            }
            
        }

        //get alerts from threads
        LinkedList<ExecutionAlert> alerts = new LinkedList<ExecutionAlert>();

        //sort by execution time
        Collections.sort(allAlerts, new Comparator<ExecutionAlert>() {
            @Override
            public int compare(ExecutionAlert o1, ExecutionAlert o2) {
                Long myTime = o1.getTimestamp();
                Long otherTime = o2.getTimestamp();
                return otherTime.compareTo(myTime);

            }
        });

        boolean showAll = false;
        if(!showAll) {
            for (int i = 0; i < CFG.getTopAlertsToStore(); i++) {
                if (allAlerts.size() >= 1) {
                    ExecutionAlert alert = allAlerts.remove();
    
                    alerts.add(alert);
                }
            }
        } else {
            alerts = allAlerts;
        }
        
        out.println("</br>");
        out.println("<div class=\"tabletitle\">" + "Latest " + CFG.getTopAlertsToStore() + " alerts" + "</div>");

        out.println("<table class=\"datatable\" id=\"genericTableFormtable\">");
        out.println("<tbody>");
        out.println("<tr>");
        out.println("<th>" + "lasted" + "</th>");
        out.println("<th>" + "statement" + "</th>");
        out.println("<th>" + "time" + "</th>");
        out.println("<th>" + "modifiers" + "</th>");
        out.println("<th>" + "state timing" + "</th>");
        
        //        //sort by execution time
        //        Collections.sort(alerts, new Comparator<ExecutionAlert>() {
        //             @Override
        //             public int compare(ExecutionAlert o1, ExecutionAlert o2) {
        //                Long myTime = o1.getLapsed();
        //                Long otherTime = o2.getLapsed();
        //                return otherTime.compareTo(myTime);
        //
        //             }
        //           }
        //        );

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy 'at' hh:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("CET"));
        
        ListIterator itr = alerts.listIterator();
        int rowNo = 0;
        while (itr.hasNext()) {
            ExecutionAlert alert = (ExecutionAlert) itr.next();
            rowNo++;
            if (rowNo % 2 != 0) {
                out.println("<tr class=\"rowEven\">");
            } else {
                out.println("<tr class=\"rowOdd\">");
            }
            out.println("<td>" + alert.getLasted() + "</td>");
            out.println("<td width=\"600px\">" + alert.getStatement() + "</td>");

            out.println("<td>" + sdf.format(alert.getTimestamp()) + "</td>");
            out.println("<td>" + alert.getModifiers() + "</td>");
            out.println("<td>" + alert.getStatesTiming() + "</td>");
            out.println("</tr>");
        }
        out.println("</tbody>");
        out.println("</table>");

        out.println("</body></html>");
        out.close();
    }
}
