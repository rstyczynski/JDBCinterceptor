package styczynski.weblogic.jdbc.debug.show;

import java.io.IOException;
import java.io.PrintWriter;

import java.net.URI;

import java.net.URISyntaxException;
import java.net.URL;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.servlet.http.HttpServletRequestWrapper;

import styczynski.weblogic.jdbc.debug.JDBCcallFSMstate;
import styczynski.weblogic.jdbc.debug.report.ExecutionAlert;
import styczynski.weblogic.jdbc.debug.report.ExecutionHistogram;
import styczynski.weblogic.jdbc.debug.security.Authorization;
import styczynski.weblogic.jdbc.monitor.CFG;
import styczynski.weblogic.jdbc.monitor.JDBCmonitor;

public class LatestHistograms extends HttpServlet {
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
        HashMap props = new HashMap();
        props.put("filter", true);
        
        HTMLhelper.addHeaders(out, "LatestHistograms", props, request);

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
        
        
        SortInfo sortInfo = HTMLhelper.switchSort(request);
        
        out.println("<div class=\"tabletitle\">" + "Latest histograms" + "</div>");
        out.println("<table style=\"width:1400px\" class=\"datatable\" id=\"genericTableFormtable\">");
        out.println("<tbody>");
        out.println("<tr>");
        out.println("<th>" + "statement" + "</th>");
        
        String href = sortInfo.first ? sortInfo.myURL + "?sortBy=cnt&sortAsc=false" : sortInfo.myURL + 
                      (sortInfo.sortExists ? "&sortBy=cnt" : "&sortBy=cnt&sortAsc=" + sortInfo.nextSortAsc);
        out.println("<th>" + "<a href=" + href + ">cnt</a>" + "</th>");
        
        href = sortInfo.first ? sortInfo.myURL + "?sortBy=sum&sortAsc=false" : sortInfo.myURL + 
                      (sortInfo.sortExists ? "&sortBy=sum" : "&sortBy=sum&sortAsc=" + sortInfo.nextSortAsc);        
        out.println("<th>" + "<a href=" + href + ">sum [s]</a>" + "</th>");
        
        href = sortInfo.first ? sortInfo.myURL + "?sortBy=min&sortAsc=false" : sortInfo.myURL +
                      (sortInfo.sortExists ? "&sortBy=min" : "&sortBy=min&sortAsc=" + sortInfo.nextSortAsc);
        out.println("<th>" + "<a href=" + href + ">min [ms]</a>" + "</th>");
        
        href = sortInfo.first ? sortInfo.myURL + "?sortBy=avg&sortAsc=false" : sortInfo.myURL + 
                      (sortInfo.sortExists ? "&sortBy=avg" : "&sortBy=avg&sortAsc=" + sortInfo.nextSortAsc);
        out.println("<th>" + "<a href=" + href + ">avg [ms]</a>" + "</th>");

        href = sortInfo.first ? sortInfo.myURL + "?sortBy=max&sortAsc=false" : sortInfo.myURL +
                      (sortInfo.sortExists ? "&sortBy=max" : "&sortBy=max&sortAsc=" + sortInfo.nextSortAsc);
        out.println("<th>" + "<a href=" + href + ">max [ms]</a>" + "</th>");
        
        out.println("<th style=\"width:400px\">" + "histogram" + "</th>");
        
        
        //FILTERING
        List<ExecutionHistogram> sortedHistograms = new ArrayList<ExecutionHistogram>(allHitograms.values());
        
        String filter = null;
        boolean regExp = false;
        boolean include = true;
        if (request.getParameter("filter") != null) filter = request.getParameter("filter");
        if (request.getParameter("regexp") != null) regExp = Boolean.valueOf(request.getParameter("regexp"));   
        if (request.getParameter("include") != null) include = Boolean.valueOf(request.getParameter("include"));   

        if (filter != null) {
            List<ExecutionHistogram> removeThem = new ArrayList<ExecutionHistogram>();
            for(ExecutionHistogram histogram : sortedHistograms)
                if (include) {
                    if (regExp) {
                        if ( ! histogram.getName().matches(filter)) 
                            removeThem.add(histogram);
                    } else {
                        if ( histogram.getName().indexOf(filter) < 0) 
                            removeThem.add(histogram);
                    }
                } else {
                    if (regExp) {
                        if ( histogram.getName().matches(filter)) 
                            removeThem.add(histogram);
                    } else {
                        if ( histogram.getName().indexOf(filter) > 0) 
                            removeThem.add(histogram);
                    }                    
                }
            sortedHistograms.removeAll(removeThem);
        }
        
        // SORTING START
        {
        String sortBy = "(none)";
        boolean sortAsc = false;
        if (request.getParameter("sortBy") != null) sortBy=request.getParameter("sortBy");
        if (request.getParameter("sortAsc") != null) sortAsc= Boolean.valueOf(request.getParameter("sortAsc"));      
        
        if ( "cnt".equals(sortBy) )
            if (sortAsc)
                Collections.sort(sortedHistograms, new Comparator<ExecutionHistogram>() {
                    public int compare(ExecutionHistogram o1, ExecutionHistogram o2) {
                        Long oneVal = o1.getCnt();
                        Long otherVal = o2.getCnt();
                        return oneVal.compareTo(otherVal);
                    }
                });
            else
                Collections.sort(sortedHistograms, new Comparator<ExecutionHistogram>() {
                    public int compare(ExecutionHistogram o1, ExecutionHistogram o2) {
                        Long oneVal = o1.getCnt();
                        Long otherVal = o2.getCnt();
                        return otherVal.compareTo(oneVal);
                    }
                });
        
        if ( "sum".equals(sortBy) )
            if (sortAsc)
                Collections.sort(sortedHistograms, new Comparator<ExecutionHistogram>() {
                    @Override
                    public int compare(ExecutionHistogram o1, ExecutionHistogram o2) {
                        Double oneVal = o1.getSum();
                        Double otherVal = o2.getSum();
                        return oneVal.compareTo(otherVal);
                    }
                });
            else
            Collections.sort(sortedHistograms, new Comparator<ExecutionHistogram>() {
                @Override
                public int compare(ExecutionHistogram o1, ExecutionHistogram o2) {
                    Double oneVal = o1.getSum();
                    Double otherVal = o2.getSum();
                    return otherVal.compareTo(oneVal);
                }
            });          
        
            if ( "min".equals(sortBy) )
                if (sortAsc)
                    Collections.sort(sortedHistograms, new Comparator<ExecutionHistogram>() {
                        public int compare(ExecutionHistogram o1, ExecutionHistogram o2) {
                            Long oneVal = o1.getMin();
                            Long otherVal = o2.getMin();
                            return oneVal.compareTo(otherVal);
                        }
                    });
                else
                    Collections.sort(sortedHistograms, new Comparator<ExecutionHistogram>() {
                        public int compare(ExecutionHistogram o1, ExecutionHistogram o2) {
                            Long oneVal = o1.getMin();
                            Long otherVal = o2.getMin();
                            return otherVal.compareTo(oneVal);
                        }
                    });
 
            if ( "avg".equals(sortBy) )
                if (sortAsc)
                    Collections.sort(sortedHistograms, new Comparator<ExecutionHistogram>() {
                        public int compare(ExecutionHistogram o1, ExecutionHistogram o2) {
                            Long oneVal = o1.getAvg();
                            Long otherVal = o2.getAvg();
                            return oneVal.compareTo(otherVal);
                        }
                    });
                else
                    Collections.sort(sortedHistograms, new Comparator<ExecutionHistogram>() {
                        public int compare(ExecutionHistogram o1, ExecutionHistogram o2) {
                            Long oneVal = o1.getAvg();
                            Long otherVal = o2.getAvg();
                            return otherVal.compareTo(oneVal);
                        }
                    });
     
            if ( "max".equals(sortBy) )
                if (sortAsc)
                    Collections.sort(sortedHistograms, new Comparator<ExecutionHistogram>() {
                        public int compare(ExecutionHistogram o1, ExecutionHistogram o2) {
                            Long oneVal = o1.getMax();
                            Long otherVal = o2.getMax();
                            return oneVal.compareTo(otherVal);
                        }
                    });
                else
                    Collections.sort(sortedHistograms, new Comparator<ExecutionHistogram>() {
                        public int compare(ExecutionHistogram o1, ExecutionHistogram o2) {
                            Long oneVal = o1.getMax();
                            Long otherVal = o2.getMax();
                            return otherVal.compareTo(oneVal);
                        }
                    });
            
        } //SORTING STOP


        int rowNo = 0;        
        for(ExecutionHistogram histogram : sortedHistograms) {
            rowNo++;
            if (rowNo % 2 != 0) {
                out.println("<tr class=\"rowEven\">");
            } else {
                out.println("<tr class=\"rowOdd\">");
            }
            out.println("<td>" + histogram.getName() + "</td>");
            out.println("<td>" + histogram.getCnt() + "</td>");
            out.println("<td>" + histogram.getSum()/1000 + "</td>");
            out.println("<td>" + histogram.getMin() + "</td>");
            out.println("<td>" + histogram.getAvg() + "</td>");
            out.println("<td>" + histogram.getMax() + "</td>");

            out.println("<td width=\"400px\"><code><FONT size=\"1\" FACE=\"courier\">");
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