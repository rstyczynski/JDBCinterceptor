package styczynski.weblogic.jdbc.debug.show;

import java.io.IOException;
import java.io.PrintWriter;

import java.text.SimpleDateFormat;

import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import styczynski.weblogic.jdbc.debug.JDBCcallFSMstate;
import styczynski.weblogic.jdbc.debug.report.ExecutionAlert;
import styczynski.weblogic.jdbc.debug.report.ExecutionHistogram;
import styczynski.weblogic.jdbc.monitor.CFG;
import styczynski.weblogic.jdbc.monitor.JDBCmonitor;

public class LatestHistograms extends HttpServlet {
    private static final String CONTENT_TYPE = "text/html; charset=UTF-8";

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(CONTENT_TYPE);

        PrintWriter out = response.getWriter();
        HTMLhelper.addHeaders(out, "LatestHistograms");

        final Enumeration<String> threadsEnum = Collections.enumeration(JDBCmonitor.getJdbcGlobalState().keySet());

        HashMap<Object, ExecutionHistogram> allHitograms = new HashMap<Object, ExecutionHistogram>();

        while (threadsEnum.hasMoreElements()) {

            String thread = threadsEnum.nextElement();
            //TODO Concurrent modification exception possible on getXX
            JDBCcallFSMstate fsmState = JDBCmonitor.getJdbcGlobalState().get(thread);

            //get alerts from this thread
            if (fsmState.getTopHistograms() != null) {
                
                Iterator histIter = fsmState.getTopHistograms().getReadBuffer().keySet().iterator();
                while (histIter.hasNext()){
                    String key = (String)histIter.next();
                    if (allHitograms.containsKey(key)) {
                        allHitograms.get(key).merge((ExecutionHistogram)fsmState.getTopHistograms().getReadBuffer().get(key));
                    } else {
                        ExecutionHistogram histogram = new ExecutionHistogram((ExecutionHistogram)fsmState.getTopHistograms().getReadBuffer().get(key));
                        allHitograms.put(key, histogram);
                    }
                }    
            }
        }


        out.println("</br>");
        out.println("<div class=\"tabletitle\">" + "Latest " + CFG.getTopHistogramsToStore() + " histograms" + "</div>");

        out.println("<table style=\"width:1600px\" class=\"datatable\" id=\"genericTableFormtable\">");
        out.println("<tbody>");
        out.println("<tr>");
        out.println("<th>" + "statement" + "</th>");
        out.println("<th>" + "cnt" + "</th>");
        out.println("<th>" + "sum [s]" + "</th>");
        out.println("<th>" + "min [ms]" + "</th>");
        out.println("<th>" + "avg [ms]" + "</th>");
        out.println("<th>" + "max [ms]" + "</th>");
        out.println("<th style=\"width:600px\">" + "histogram" + "</th>");
        
        Iterator itr = allHitograms.keySet().iterator();
        int rowNo = 0;
        while (itr.hasNext()) {
            String key = (String)itr.next();    
            ExecutionHistogram histogram = allHitograms.get(key);
            rowNo++;
            if (rowNo % 2 != 0) {
                out.println("<tr class=\"rowEven\">");
            } else {
                out.println("<tr class=\"rowOdd\">");
            }
            out.println("<td>" + key + "</td>");
            out.println("<td>" + histogram.getCnt() + "</td>");
            out.println("<td>" + histogram.getSum()/1000 + "</td>");
            out.println("<td>" + histogram.getMin() + "</td>");
            out.println("<td>" + histogram.getAvg() + "</td>");
            out.println("<td>" + histogram.getMax() + "</td>");

            out.println("<td width=\"600px\"><code><FONT size=\"1\" FACE=\"courier\">");
            out.println(histogram.getASCIIChart(10, 0, "</br>", "X", "x", "'"));
            //out.println("</br>" + histogram);
            out.println("</FONT></code></td>");
            out.println("</tr>");
        }
        out.println("</tbody>");
        out.println("</table>");

        out.println("</body></html>");
        out.close();
    }
}
