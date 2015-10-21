package styczynski.weblogic.jdbc.debug.show;

import java.io.PrintWriter;

import java.util.HashMap;

public class HTMLhelper {


    public static void addHeaders(PrintWriter out, String title) {
        HashMap props = new HashMap();
        props.put("testInNewPage",true);
        addHeaders(out, title, props);
        
    }
    
    public static void addHeaders(PrintWriter out, String title, HashMap props) {
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

    
//When state is cleared by thread local, it's not possible to reinitialize. After this step references will be lost.
//        out.println("<li>");
//        out.println("<a href=\"setparameters?resetGlobalStatus\">Reset global status</a>");
//        out.println("</li>");
//        
        out.println("</ul>");
        out.println("</div>");
        out.println("</div>");
        //            out.println("</div>");
        out.println("</div>");
    }
    
}
