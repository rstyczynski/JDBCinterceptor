package styczynski.weblogic.jdbc.debug.show;

import java.io.PrintWriter;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.servlet.http.HttpServletRequest;

import styczynski.weblogic.jdbc.debug.security.Authorization;
import styczynski.weblogic.jdbc.monitor.CFG;

public class HTMLhelper {

    public static void addHeaders(PrintWriter out, String title, HttpServletRequest request) {
        HashMap props = new HashMap();
        props.put("testInNewPage",true);
        addHeaders(out, title, props, request);
        
    }
    
    public static void addHeaders(PrintWriter out, String title, HashMap props, HttpServletRequest request) {
        out.println("<html>");
        out.println("<head>");
        out.println("<title>" + title + "</title>");
        out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"resources/css/general.css\">");
        out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"resources/css/format.css\">");
        
        out.println("<meta http-equiv=\"cache-control content=\"max-age=0\" />");
        out.println("<meta http-equiv=\"cache-control content=\"no-cache\" />");
        out.println("<meta http-equiv=\"expires content=\"0\"/>");
        out.println("<meta http-equiv=\"expires content=\"Tue, 01 Jan 1980 1:00:00 GMT\" />");
        out.println("<meta http-equiv=\"pragma content=\"no-cache\" />");
                                      
        out.println("<style type=\"text/css\"> code{white-space:pre} </style>");
        out.println("</head>");

        out.println("<body>");

        //if (props.containsKey("histogram")) {
            out.println("<table style=\"width:1400px; border: 2px; border-collapse: collapse;\">");
            out.println("<tr>");
            out.println("<td style=\"width:150px;\">");
        //}
        
        out.println("<div class=\"toolbar\">");
        //            out.println("<div class=\"toolbar-menu\">");
        out.println("<div class=\"tbframe\">");
        out.println("<div id=\"topMenu\" class=\"tbframeContent\">");

        out.println("<ul>");
        out.println("<li>");
                                                                 
        if(Authorization.canUserEnableDisable(request)) { 
            if(CFG.isJDBCmonitoringDisabled()){
                out.println("<a href=\"setparameters?JDBCmonitoringDisabled=false\">");
                out.println("<img src=\"images/OFF.png\" alt=\"ON/OFF\" style=\"height:10px;\">");
            } else {
                out.println("<a href=\"setparameters?JDBCmonitoringDisabled=true\">");
                out.println("<img src=\"images/ON.png\" alt=\"ON/OFF\" style=\"height:10px;\">");            
            }
            out.println("</a>");
        } else {
            if(CFG.isJDBCmonitoringDisabled()){
                out.println("<img src=\"images/OFF.png\" alt=\"ON/OFF\" style=\"height:10px;\">");
            } else {
                out.println("<img src=\"images/ON.png\" alt=\"ON/OFF\" style=\"height:10px;\">");            
            }
        }
        out.println("</li>");                                                                                


        
        out.println("<li>");
        out.println("<a href=\"currentSQL\">Current SQL</a>");
        out.println("</li>");
        
        out.println("<li>");
        out.println("<a href=\"latestAlerts\">Latest Alerts</a>");
        out.println("</li>");

        out.println("<li>");
        out.println("<a href=\"latestHistograms\">Latest Histograms</a>");
        out.println("</li>");

        
        //if (props.containsKey("histogram")) {
            out.println("</td>");
            
            if (props.containsKey("histogram")) {
                out.println("<td style=\"width:250px;\">");
                out.println("<div class=\"toolbar\">");
                
                boolean first=true;
                boolean nextSortAsc = false;
                StringBuffer myURL = request.getRequestURL();
                for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
                    String name = entry.getKey();
                    String[] value = entry.getValue();
                                
                    if (! name.equals("filter") ) {
                        if (first) {
                            myURL.append("?");
                            first=false;
                        } else {
                            myURL.append("&");
                        }
                        myURL.append(name + "=" + value[0]);
                    }
                }
                
                String href = myURL.toString();
                
                out.println("<form method=GET action=" + href + " style=\"text-align: left; margin-bottom:5px 0; height:10px\">");
        
                out.println("<label style=\"font-size:11px;\" >");  
                out.println("Filter SQL");  
                out.println("</label>"); 
                
                
                String filter = "";
                if (request.getParameter("filter") != null) filter = request.getParameter("filter");
                out.println("<input type=\"text\" name=\"filter\" value=\"" + filter + "\">");
                
                out.println("<button style=\"background: transparent url(images/search_ena.gif); padding-right: 0px; width: 20px; height: 20px;\"  type=\"button\" class=\"formButton\" onclick=\"this.form.submit();return false;\" name=\"search\" title=\"Search\">");
                out.println("</form>");
                
                out.println("</div>");
                out.println("</td>");   
            }
        //}
        

        //if (props.containsKey("histogram")) {
            out.println("<td>");
        //}
        
        out.println("<div class=\"toolbar\">");
        //            out.println("<div class=\"toolbar-menu\">");
        out.println("<div class=\"tbframe\">");
        out.println("<div id=\"topMenu\" class=\"tbframeContent\">");
        out.println("<ul>");
        
        if(Authorization.canUserSetThreshold(request)) {
            out.println("<li>");
            out.println("<a href=\"setparameters?sqlMaxExecutionTime=1\">Execution threshold: 1ms</a>");
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
            out.println("<a href=\"setparameters?sqlMaxExecutionTime=100000\">100s</a>");
            out.println("</li>");
        }

        //TODO Not available in array based alert queue
        //        out.println("<li>");
        //        out.println("<a href=\"setparameters?topAlertsToStore=10\">Show alerts: 10</a>");
        //        out.println("</li>");
        //
        //        out.println("<li>");
        //        out.println("<a href=\"setparameters?topAlertsToStore=50\">50</a>");
        //        out.println("</li>");

        if(Authorization.canUserControlDebug(request)) {
            out.println("<li>");
            out.println("<a href=\"setparameters?printHeadersAlways=true&debugNormal=true&\">Debug: normal</a>");
            out.println("</li>");
            
            out.println("<li>");
            out.println("<a href=\"setparameters?printHeadersAlways=true&debugNormal=true&debugDetailed=true\">detailed</a>");
            out.println("</li>");
        
            out.println("<li>");
            out.println("<a href=\"setparameters?printHeadersAlways=false&debugNormal=false&debugDetailed=false\">off</a>");
            out.println("</li>");
        }
        
        if(Authorization.canUserExecuteSQL(request)) {
            if(props.containsKey("testInNewPage") && (Boolean)props.get("testInNewPage") == true) {
                out.println("<li>");
                out.println("<a href=\"executesql?param1=1\" target=\"_blank\">Execute SQL: 1s</a>");
                out.println("</li>");
            
                out.println("<li>");
                out.println("<a href=\"executesql?param1=10\" target=\"_blank\">10s</a>");
                out.println("</li>");
            } else {
                out.println("<li>");
                out.println("<a href=\"executesql?param1=1\">Execute SQL: 1s</a>");
                out.println("</li>");
                
                out.println("<li>");
                out.println("<a href=\"executesql?param1=10\">10s</a>");
                out.println("</li>");
            }
        
            
            if(props.containsKey("testInNewPage") && (Boolean)props.get("testInNewPage") == true) {
                out.println("<li>");
                out.println("<a href=\"executesql?param1=1&noClose\" target=\"_blank\">Execute SQL w/o close: 1s</a>");
                out.println("</li>");
            
                out.println("<li>");
                out.println("<a href=\"executesql?param1=10&noClose\" target=\"_blank\">10s</a>");
                out.println("</li>");
            } else {
                out.println("<li>");
                out.println("<a href=\"executesql?param1=1&noClose\">Execute SQL w/o close: 1s</a>");
                out.println("</li>");
                
                out.println("<li>");
                out.println("<a href=\"executesql?param1=10&noClose\">10s</a>");
                out.println("</li>");
            
            }
        }
        
        if(Authorization.canUserReset(request)) {
            out.println("<li>");
            out.println("<a href=\"setparameters?resetGlobalStatus=true\">Reset stats</a>");
            out.println("</li>");
        }
        
        out.println("</ul>");
        out.println("</div>");
        out.println("</div>");
        out.println("</div>");
        
        //if (props.containsKey("histogram")) {
            out.println("</td>");
            out.println("</table>");
        //}
        

        out.println("</ul>");
        out.println("</div>");
        out.println("</div>");
        out.println("</div>");
        
    }
    
}
