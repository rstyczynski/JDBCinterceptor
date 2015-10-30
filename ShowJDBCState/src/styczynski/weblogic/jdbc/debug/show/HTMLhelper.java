package styczynski.weblogic.jdbc.debug.show;

import java.io.PrintWriter;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.servlet.http.HttpServletRequest;

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

        if (props.containsKey("histogram")) {
            out.println("<table style=\"width:1400px; border: 0px; border-collapse: collapse;\">");
            out.println("<tr>");
            out.println("<td style=\"width:300px;\">");
        }
        
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
        out.println("<a href=\"latestHistograms\">Latest Histograms</a>");
        out.println("</li>");
        out.println("</ul>");
        out.println("</div>");
        out.println("</div>");
        out.println("</div>");
        
        
        if (props.containsKey("histogram")) {
            out.println("</td>");
    
            out.println("<td>");
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
                           
            out.println("</tr>");
            out.println("</table>");
        }

    }
    
}
