package styczynski.weblogic.jdbc.debug.show;

import java.io.IOException;
import java.io.PrintWriter;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.TimeZone;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import styczynski.weblogic.jdbc.debug.JDBCcallFSMstate;
import styczynski.weblogic.jdbc.debug.report.ExecutionAlert;
import styczynski.weblogic.jdbc.debug.report.ExecutionHistogram;
import styczynski.weblogic.jdbc.debug.security.Authorization;
import styczynski.weblogic.jdbc.monitor.CFG;
import styczynski.weblogic.jdbc.monitor.JDBCmonitor;

public class LatestAlerts extends HttpServlet {
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
        HTMLhelper.addHeaders(out, "LatestAlerts", props, request);


        //COLLECT DATA FROM THREADS
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


        //sort by execution time
        LinkedList<ExecutionAlert> alerts = new LinkedList<ExecutionAlert>();
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
        
        out.println("<div class=\"tabletitle\">" + "Latest alerts" + "</div>");

        SortInfo sortInfo = HTMLhelper.switchSort(request);
        String sortBy="(none)";
        String href = "(none)";
        
        out.println("<table class=\"datatable\" id=\"genericTableFormtable\">");
        out.println("<tbody>");
        out.println("<tr>");
        
        sortBy="lasted";
        href = sortInfo.first ? sortInfo.myURL + "?sortBy=" + sortBy + "&sortAsc=false" : sortInfo.myURL + 
                      (sortInfo.sortExists ? "&sortBy=" + sortBy : "&sortBy=" + sortBy + "&sortAsc=" + sortInfo.nextSortAsc);
        out.println("<th>" + "<a href=" + href + ">" + sortBy + "</a>" + "</th>");
        
        sortBy="statement";
        href = sortInfo.first ? sortInfo.myURL + "?sortBy=" + sortBy + "&sortAsc=false" : sortInfo.myURL + 
                      (sortInfo.sortExists ? "&sortBy=" + sortBy : "&sortBy=" + sortBy + "&sortAsc=" + sortInfo.nextSortAsc);
        out.println("<th>" + "<a href=" + href + ">" + sortBy + "</a>" + "</th>");

        sortBy="time";
        href = sortInfo.first ? sortInfo.myURL + "?sortBy=" + sortBy + "&sortAsc=false" : sortInfo.myURL + 
                      (sortInfo.sortExists ? "&sortBy=" + sortBy : "&sortBy=" + sortBy + "&sortAsc=" + sortInfo.nextSortAsc);
        out.println("<th>" + "<a href=" + href + ">" + sortBy + "</a>" + "</th>");

        
        out.println("<th>" + "modifiers" + "</th>");
        out.println("<th>" + "state timing" + "</th>");
        
        
        //FILTERING

        String filter = null;
        boolean regExp = false;
        boolean include = true;
        if (request.getParameter("filter") != null) filter = request.getParameter("filter");
        if (request.getParameter("regexp") != null) regExp = Boolean.valueOf(request.getParameter("regexp"));   
        if (request.getParameter("include") != null) include = Boolean.valueOf(request.getParameter("include"));   

        if (filter != null) {
            List<ExecutionAlert> removeThem = new ArrayList<ExecutionAlert>();
            for(ExecutionAlert alert : alerts)
                if (include) {
                    if (regExp) {
                        if ( ! alert.getStatement().matches(filter)) 
                            removeThem.add(alert);
                    } else {
                        if ( alert.getStatement().indexOf(filter) < 0) 
                            removeThem.add(alert);
                    }
                } else {
                    if (regExp) {
                        if ( alert.getStatement().matches(filter)) 
                            removeThem.add(alert);
                    } else {
                        if ( alert.getStatement().indexOf(filter) > 0) 
                            removeThem.add(alert);
                    }                    
                }
            alerts.removeAll(removeThem);
        }
        
        // SORTING START
        {
        sortBy = "(none)";
        boolean sortAsc = false;
        if (request.getParameter("sortBy") != null) sortBy = request.getParameter("sortBy");
        if (request.getParameter("sortAsc") != null) sortAsc = Boolean.valueOf(request.getParameter("sortAsc"));      
        
        if ( "lasted".equals(sortBy) )
            if (sortAsc)
                Collections.sort(alerts, new Comparator<ExecutionAlert>() {
                     @Override
                     public int compare(ExecutionAlert o1, ExecutionAlert o2) {
                        Long myTime = o2.getLasted();
                        Long otherTime = o1.getLasted();
                        return otherTime.compareTo(myTime);
        
                     }
                   }
                );
            else
                Collections.sort(alerts, new Comparator<ExecutionAlert>() {
                     @Override
                     public int compare(ExecutionAlert o1, ExecutionAlert o2) {
                        Long myTime = o1.getLasted();
                        Long otherTime = o2.getLasted();
                        return otherTime.compareTo(myTime);
                
                     }
                   }
                );        
            
        if ( "time".equals(sortBy) )
            if (sortAsc)
                Collections.sort(alerts, new Comparator<ExecutionAlert>() {
                     @Override
                     public int compare(ExecutionAlert o1, ExecutionAlert o2) {
                        Long myTime = o2.getTimestamp();
                        Long otherTime = o1.getTimestamp();
                        return otherTime.compareTo(myTime);
        
                     }
                   }
                );
            else
                Collections.sort(alerts, new Comparator<ExecutionAlert>() {
                     @Override
                     public int compare(ExecutionAlert o1, ExecutionAlert o2) {
                        Long myTime = o1.getTimestamp();
                        Long otherTime = o2.getTimestamp();
                        return otherTime.compareTo(myTime);
                
                     }
                   }
                );   
        
        if ( "statement".equals(sortBy) )
            if (sortAsc)
                Collections.sort(alerts, new Comparator<ExecutionAlert>() {
                     @Override
                     public int compare(ExecutionAlert o1, ExecutionAlert o2) {
                        String myTime = o2.getStatement();
                        String otherTime = o1.getStatement();
                        return otherTime.compareTo(myTime);
        
                     }
                   }
                );
            else
                Collections.sort(alerts, new Comparator<ExecutionAlert>() {
                     @Override
                     public int compare(ExecutionAlert o1, ExecutionAlert o2) {
                        String myTime = o1.getStatement();
                        String otherTime = o2.getStatement();
                        return otherTime.compareTo(myTime);
                
                     }
                   }
                );     
        
        }

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
