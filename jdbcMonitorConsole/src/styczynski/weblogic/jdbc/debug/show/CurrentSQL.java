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

import org.apache.commons.logging.LogFactory;

import styczynski.weblogic.jdbc.debug.JDBCcallFSMstate;
import styczynski.weblogic.jdbc.debug.report.ExecutionAlert;
import styczynski.weblogic.jdbc.debug.report.ExecutionHistogram;
import styczynski.weblogic.jdbc.debug.security.Authorization;
import styczynski.weblogic.jdbc.monitor.JDBCmonitor;

//TODO currentSQL must show query start time, last activity time, and execution time.

public class CurrentSQL extends HttpServlet {
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
        HTMLhelper.addHeaders(out, "Current SQL", props, request);

        out.println("<div class=\"tabletitle\">" + "Current statements" + "</div>");
        
        SortInfo sortInfo = HTMLhelper.switchSort(request);
        String sortBy="(none)";
        String sortBySuffix="";
        String href = "(none)";
        
        out.println("<table class=\"datatable\" id=\"genericTableFormtable\">");
        out.println("<tbody>");
        out.println("<tr>");
        
        
        sortBy="uptime";
        sortBySuffix = " [ms]";
        href = sortInfo.first ? sortInfo.myURL + "?sortBy=" + sortBy + "&sortAsc=false" : sortInfo.myURL + 
                      (sortInfo.sortExists ? "&sortBy=" + sortBy : "&sortBy=" + sortBy + "&sortAsc=" + sortInfo.nextSortAsc);
        out.println("<th>" + "<a href=" + href + ">" + sortBy + sortBySuffix + "</a>" + "</th>");

        sortBy="lasted";
        sortBySuffix = " [ms]";
        href = sortInfo.first ? sortInfo.myURL + "?sortBy=" + sortBy + "&sortAsc=false" : sortInfo.myURL + 
                      (sortInfo.sortExists ? "&sortBy=" + sortBy : "&sortBy=" + sortBy + "&sortAsc=" + sortInfo.nextSortAsc);
        out.println("<th>" + "<a href=" + href + ">" + sortBy + sortBySuffix + "</a>" + "</th>");

        sortBy="statement";
        sortBySuffix = "";
        href = sortInfo.first ? sortInfo.myURL + "?sortBy=" + sortBy + "&sortAsc=false" : sortInfo.myURL + 
                      (sortInfo.sortExists ? "&sortBy=" + sortBy : "&sortBy=" + sortBy + "&sortAsc=" + sortInfo.nextSortAsc);
        out.println("<th>" + "<a href=" + href + ">" + sortBy + sortBySuffix + "</a>" + "</th>");

        sortBy="state";
        sortBySuffix = "";
        href = sortInfo.first ? sortInfo.myURL + "?sortBy=" + sortBy + "&sortAsc=false" : sortInfo.myURL + 
                      (sortInfo.sortExists ? "&sortBy=" + sortBy : "&sortBy=" + sortBy + "&sortAsc=" + sortInfo.nextSortAsc);
        out.println("<th>" + "<a href=" + href + ">" + sortBy + sortBySuffix + "</a>" + "</th>");

        out.println("<th>" + "modifiers" + "</th>");
        
        sortBy="updated";
        sortBySuffix = "";
        href = sortInfo.first ? sortInfo.myURL + "?sortBy=" + sortBy + "&sortAsc=false" : sortInfo.myURL + 
                      (sortInfo.sortExists ? "&sortBy=" + sortBy : "&sortBy=" + sortBy + "&sortAsc=" + sortInfo.nextSortAsc);
        out.println("<th>" + "<a href=" + href + ">" + sortBy + sortBySuffix + "</a>" + "</th>");


        out.println("<th>" + "state timing" + "</th>");
        
        sortBy="interceptor";
        sortBySuffix = "";
        href = sortInfo.first ? sortInfo.myURL + "?sortBy=" + sortBy + "&sortAsc=false" : sortInfo.myURL + 
                      (sortInfo.sortExists ? "&sortBy=" + sortBy : "&sortBy=" + sortBy + "&sortAsc=" + sortInfo.nextSortAsc);
        out.println("<th>" + "<a href=" + href + ">" + sortBy + sortBySuffix + "</a>" + "</th>");

        //out.println("<th>" + "fsmState" + "</th>");
        
        out.println("</tr>");
        
        //COLLECT DATA FROM THREADS
        LinkedList<JDBCcallFSMstate> allJDBCcallFSMstates = new LinkedList<JDBCcallFSMstate>();
        final Enumeration<String> jdbcEnum = Collections.enumeration(JDBCmonitor.getJdbcGlobalState().keySet());
        while(jdbcEnum.hasMoreElements()) {
            String jdbcCall = jdbcEnum.nextElement();
            JDBCcallFSMstate fsmState = JDBCmonitor.getJdbcGlobalState().get(jdbcCall);
            fsmState.setHoldingThreadName(jdbcCall);
            allJDBCcallFSMstates.add(fsmState);
        }
      
      
        //FILTERING
        String filter = null;
        boolean regExp = false;
        boolean include = true;
        if (request.getParameter("filter") != null) filter = request.getParameter("filter");
        if (request.getParameter("regexp") != null) regExp = Boolean.valueOf(request.getParameter("regexp"));   
        if (request.getParameter("include") != null) include = Boolean.valueOf(request.getParameter("include"));   

        if (filter != null) {
            List<JDBCcallFSMstate> removeThem = new ArrayList<JDBCcallFSMstate>();
            for(JDBCcallFSMstate fsmState : allJDBCcallFSMstates)
                if (include) {
                    if (regExp) {
                        if ( ! fsmState.getStatement().matches(filter)) 
                            removeThem.add(fsmState);
                    } else {
                        if ( fsmState.getStatement().indexOf(filter) < 0) 
                            removeThem.add(fsmState);
                    }
                } else {
                    if (regExp) {
                        if ( fsmState.getStatement().matches(filter)) 
                            removeThem.add(fsmState);
                    } else {
                        if ( fsmState.getStatement().indexOf(filter) > 0) 
                            removeThem.add(fsmState);
                    }                    
                }
            allJDBCcallFSMstates.removeAll(removeThem);
        }
      
        // SORTING START
        {
        sortBy = "(none)";
        boolean sortAsc = false;
        if (request.getParameter("sortBy") != null) sortBy = request.getParameter("sortBy");
        if (request.getParameter("sortAsc") != null) sortAsc = Boolean.valueOf(request.getParameter("sortAsc"));      
        
        
        if ( "uptime".equals(sortBy) )
            if (sortAsc)
                Collections.sort(allJDBCcallFSMstates, new Comparator<JDBCcallFSMstate>() {
                     @Override
                     public int compare(JDBCcallFSMstate o1, JDBCcallFSMstate o2) {
                        Long myTime = o2.getCurrentExecutionTime();
                        Long otherTime = o1.getCurrentExecutionTime();
                        return otherTime.compareTo(myTime);
        
                     }
                   }
                );
            else
                Collections.sort(allJDBCcallFSMstates, new Comparator<JDBCcallFSMstate>() {
                     @Override
                     public int compare(JDBCcallFSMstate o1, JDBCcallFSMstate o2) {
                        Long myTime = o1.getCurrentExecutionTime();
                        Long otherTime = o2.getCurrentExecutionTime();
                        return otherTime.compareTo(myTime);
                
                     }
                   }
                );    
            
        if ( "lasted".equals(sortBy) )
            if (sortAsc)
                Collections.sort(allJDBCcallFSMstates, new Comparator<JDBCcallFSMstate>() {
                     @Override
                     public int compare(JDBCcallFSMstate o1, JDBCcallFSMstate o2) {
                        Long myTime = o2.getLasted();
                        Long otherTime = o1.getLasted();
                        return otherTime.compareTo(myTime);
        
                     }
                   }
                );
            else
                Collections.sort(allJDBCcallFSMstates, new Comparator<JDBCcallFSMstate>() {
                     @Override
                     public int compare(JDBCcallFSMstate o1, JDBCcallFSMstate o2) {
                        Long myTime = o1.getLasted();
                        Long otherTime = o2.getLasted();
                        return otherTime.compareTo(myTime);
                
                     }
                   }
                );     

        if ( "statement".equals(sortBy) )
            if (sortAsc)
                Collections.sort(allJDBCcallFSMstates, new Comparator<JDBCcallFSMstate>() {
                     @Override
                     public int compare(JDBCcallFSMstate o1, JDBCcallFSMstate o2) {
                        String myTime = o2.getStatement();
                        String otherTime = o1.getStatement();
                        return otherTime.compareTo(myTime);
        
                     }
                   }
                );
            else
                Collections.sort(allJDBCcallFSMstates, new Comparator<JDBCcallFSMstate>() {
                     @Override
                     public int compare(JDBCcallFSMstate o1, JDBCcallFSMstate o2) {
                        String myTime = o1.getStatement();
                        String otherTime = o2.getStatement();
                        return otherTime.compareTo(myTime);
                
                     }
                   }
                );   
            
        if ( "state".equals(sortBy) )
            if (sortAsc)
                Collections.sort(allJDBCcallFSMstates, new Comparator<JDBCcallFSMstate>() {
                     @Override
                     public int compare(JDBCcallFSMstate o1, JDBCcallFSMstate o2) {
                        String myTime = o2.getCurrentState().toString();
                        String otherTime = o1.getCurrentState().toString();
                        return otherTime.compareTo(myTime);
        
                     }
                   }
                );
            else
                Collections.sort(allJDBCcallFSMstates, new Comparator<JDBCcallFSMstate>() {
                     @Override
                     public int compare(JDBCcallFSMstate o1, JDBCcallFSMstate o2) {
                        String myTime = o1.getCurrentState().toString();
                        String otherTime = o2.getCurrentState().toString();
                        return otherTime.compareTo(myTime);
                
                     }
                   }
                );   
            
        if ( "updated".equals(sortBy) )
            if (sortAsc)
                Collections.sort(allJDBCcallFSMstates, new Comparator<JDBCcallFSMstate>() {
                     @Override
                     public int compare(JDBCcallFSMstate o1, JDBCcallFSMstate o2) {
                        Long myTime = o2.getTimeUpdated();
                        Long otherTime = o1.getTimeUpdated();
                        return otherTime.compareTo(myTime);
        
                     }
                   }
                );
            else
                Collections.sort(allJDBCcallFSMstates, new Comparator<JDBCcallFSMstate>() {
                     @Override
                     public int compare(JDBCcallFSMstate o1, JDBCcallFSMstate o2) {
                         Long myTime = o1.getTimeUpdated();
                         Long otherTime = o2.getTimeUpdated();
                        return otherTime.compareTo(myTime);
                
                     }
                   }
                );   


        if ( "interceptor".equals(sortBy) )
            if (sortAsc)
                Collections.sort(allJDBCcallFSMstates, new Comparator<JDBCcallFSMstate>() {
                     @Override
                     public int compare(JDBCcallFSMstate o1, JDBCcallFSMstate o2) {
                        String myTime = o2.getHoldingThreadName().toString();
                        String otherTime = o1.getHoldingThreadName().toString();
                        return otherTime.compareTo(myTime);
        
                     }
                   }
                );
            else
                Collections.sort(allJDBCcallFSMstates, new Comparator<JDBCcallFSMstate>() {
                     @Override
                     public int compare(JDBCcallFSMstate o1, JDBCcallFSMstate o2) {
                        String myTime = o1.getHoldingThreadName().toString();
                        String otherTime = o2.getHoldingThreadName().toString();
                        return otherTime.compareTo(myTime);
                
                     }
                   }
                );  
            
        }


        // PRINT DATA
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy 'at' hh:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("CET"));
        

        int rowNo=0;
        ListIterator itr = allJDBCcallFSMstates.listIterator();
        while(itr.hasNext()) {
            rowNo++;
            if (rowNo % 2 != 0) {
                out.println("<tr class=\"rowEven\">");
            } else {
                out.println("<tr class=\"rowOdd\">");
            } 

            JDBCcallFSMstate fsmState = (JDBCcallFSMstate) itr.next();

            out.println("<td>" + fsmState.getCurrentExecutionTime() + "</td>");
            out.println("<td>" + fsmState.getLasted() + "</td>");
            out.println("<td>" + fsmState.getStatement() + "</td>");
            out.println("<td>" + fsmState.getCurrentState() + "</td>");
            out.println("<td>" + fsmState.getModifiers() + "</td>");  
            out.println("<td>" + sdf.format(fsmState.getTimeUpdated()) + "</td>");
            out.println("<td>" + fsmState.getStatesTiming() + "</td>");
            out.println("<td>" + fsmState.getHoldingThreadName().replace("styczynski.weblogic.jdbc.monitor.", "") + "</td>");
            //out.println("<td>" + Integer.toHexString(fsmState.hashCode()) + "</td>");  
            
            out.println("<tr>");
        }
        out.println("</tbody>");
        out.println("</table>");
        

        
        out.println("</body></html>");
        out.close();
    }
}
