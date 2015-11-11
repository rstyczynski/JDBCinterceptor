package styczynski.weblogic.jdbc.debug.show;

import java.io.IOException;
import java.io.PrintWriter;

import java.text.SimpleDateFormat;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;

import java.util.LinkedList;

import java.util.ListIterator;

import java.util.TimeZone;

import javax.naming.Context;
import javax.naming.InitialContext;

import javax.naming.LinkException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import styczynski.weblogic.jdbc.debug.JDBCcallFSMstate;
import styczynski.weblogic.jdbc.monitor.JDBCmonitor;
import styczynski.weblogic.jdbc.debug.report.ExecutionAlert;
import styczynski.weblogic.jdbc.debug.report.TopAlertsArray;
import styczynski.weblogic.jdbc.debug.security.Authorization;

public class LatestAlertsDebug extends HttpServlet {
    private static final String CONTENT_TYPE = "text/html; charset=UTF-8";

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(CONTENT_TYPE);

        if(! Authorization.canUserView(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        
        PrintWriter out = response.getWriter();
        addHeaders(out, "LatestAlertsDebug");

        final Enumeration<String> threadsEnum = Collections.enumeration(JDBCmonitor.getJdbcGlobalState().keySet());

        LinkedList<ExecutionAlert> allAlerts = new LinkedList<ExecutionAlert>();


        out.println("<div class=\"tabletitle\">" + "Latest " + allAlerts.size() + " alerts" + "</div>");

        out.println("<table class=\"datatable\" id=\"genericTableFormtable\">");
        out.println("<tbody>");

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy 'at' hh:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("CET"));

        int rowNo = 0;
        int threadNo = 0;
        while (threadsEnum.hasMoreElements()) {
            rowNo++;
            threadNo++;
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


            LinkedList<ExecutionAlert> threadAlerts = new LinkedList<ExecutionAlert>();
            if (fsmState.getTopAlerts() != null) {
                ExecutionAlert[] alerts=fsmState.getTopAlerts().getAlerts();
                for(int i=0; i<alerts.length;i++){
                    if (alerts[i]!=null)
                        threadAlerts.add(alerts[i]);
                }
                
            }


            ListIterator itr = threadAlerts.listIterator();
            while (itr.hasNext()) {
                ExecutionAlert alert = (ExecutionAlert) itr.next();
                rowNo++;
                if (rowNo % 2 != 0) {
                    out.println("<tr class=\"rowEven\">");
                } else {
                    out.println("<tr class=\"rowOdd\">");
                }
                out.println("<td>" + alert.getLasted() + "</td>");
                out.println("<td width=\"100px\">" + alert.getStatement() + "</td>");
                out.println("<td>" + sdf.format(alert.getTimestamp()) + "</td>");
                out.println("<td>" + alert.getModifiers() + "</td>");
                out.println("<td>" + thread + "</td>");
                out.println("</tr>");
            }
        }
        out.println("</tbody>");
        out.println("</table>");
        out.println("Rows:" + rowNo + "</br>");
        out.println("Threads:" + threadNo + "</br>");

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

        //        for (int i = 0; i < ShowJDBCCalls.getTopAlertsToStore(); i++) {
        //            if (allAlerts.size() >= 1) {
        //                ExecutionAlert alert = allAlerts.removeFirst();
        //
        //                alerts.add(alert);
        //            }
        //        }
        alerts = allAlerts;
        out.println("</br>");
        out.println("<div class=\"tabletitle\">" + "Latest " + allAlerts.size() + " alerts" + "</div>");

        out.println("<table class=\"datatable\" id=\"genericTableFormtable\">");
        out.println("<tbody>");
        out.println("<tr>");
        out.println("<th class=\"tg-031e\">" + "lasted" + "</th>");
        out.println("<th class=\"tg-031e\">" + "statement" + "</th>");
        out.println("<th class=\"tg-031e\">" + "time" + "</th>");
        out.println("<th class=\"tg-031e\">" + "modifiers" + "</th>");

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


        ListIterator itr = alerts.listIterator();
        rowNo = 0;
        while (itr.hasNext()) {
            ExecutionAlert alert = (ExecutionAlert) itr.next();
            rowNo++;
            if (rowNo % 2 != 0) {
                out.println("<tr class=\"rowEven\">");
            } else {
                out.println("<tr class=\"rowOdd\">");
            }
            out.println("<td>" + alert.getLasted() + "</td>");
            out.println("<td width=\"100px\">" + alert.getStatement() + "</td>");
            out.println("<td>" + sdf.format(alert.getTimestamp()) + "</td>");
            out.println("<td>" + alert.getModifiers() + "</td>");
            out.println("</tr>");
        }
        out.println("</tbody>");
        out.println("</table>");

        out.println("</body></html>");
        out.close();
    }

    public static void addHeaders(PrintWriter out, String title) {
        out.println("<html>");
        out.println("<head>");
        out.println("<title>" + title + "</title>");
        out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"resources/css/general.css\">");
        out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"resources/css/format.css\">");
        out.println("</head>");

        out.println("<body>");

        out.println("<div class=\"toolbar\">");
        //            out.println("<div class=\"toolbar-menu\">");
        out.println("<div class=\"tbframe\">");
        out.println("<div id=\"topMenu\" class=\"tbframeContent\">");
        out.println("<ul>");

        out.println("<li>");
        out.println("<a href=\"currentSQL\">Current SQL</a>");
        out.println("</li>");

        out.println("<li>");
        out.println("<a href=\"latestAlerts\">Latest Alerts</a>");
        out.println("</li>");

        out.println("<li>");
        out.println("<a href=\"setparameters?sqlMaxExecutionTime=1\">Execution threadshold: 1ms</a>");
        out.println("</li>");

        out.println("<li>");
        out.println("<a href=\"setparameters?sqlMaxExecutionTime=10\">10ms</a>");
        out.println("</li>");

        out.println("<li>");
        out.println("<a href=\"setparameters?sqlMaxExecutionTime=100\">100ms</a>");
        out.println("</li>");

        out.println("<li>");
        out.println("<a href=\"setparameters?sqlMaxExecutionTime=1000\">1s</a>");
        out.println("</li>");

        out.println("<li>");
        out.println("<a href=\"setparameters?sqlMaxExecutionTime=10000\">10s</a>");
        out.println("</li>");

        out.println("<li>");
        out.println("<a href=\"setparameters?sqlMaxExecutionTime=100000&\">100s</a>");
        out.println("</li>");

        out.println("<li>");
        out.println("<a href=\"setparameters?topAlertsToStore=10\">Show alerts: 10</a>");
        out.println("</li>");

        out.println("<li>");
        out.println("<a href=\"setparameters?topAlertsToStore=50\">50</a>");
        out.println("</li>");

        out.println("<li>");
        out.println("<a href=\"setparameters?printHeadersAlways=true&debugNormal=true&\">Debug: normal</a>");
        out.println("</li>");

        out.println("<li>");
        out.println("<a href=\"setparameters?printHeadersAlways=true&debugNormal=true&debugDetailed=true\">detailed</a>");
        out.println("</li>");

        out.println("<li>");
        out.println("<a href=\"setparameters?printHeadersAlways=false&debugNormal=false&debugDetailed=false\">off</a>");
        out.println("</li>");

        out.println("</ul>");
        out.println("</div>");
        out.println("</div>");
        //            out.println("</div>");
        out.println("</div>");
    }
}


