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
        
//        out.println("<li>");
//        out.println("<a href=\"currentSQL\">Current SQL</a>");
//        out.println("</li>");
//        
//        out.println("<li>");
//        out.println("<a href=\"latestAlerts\">Latest Alerts</a>");
//        out.println("</li>");

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


    //TODO Not available in array based alert queue
    //        out.println("<li>");
    //        out.println("<a href=\"setparameters?topAlertsToStore=10\">Show alerts: 10</a>");
    //        out.println("</li>");
    //
    //        out.println("<li>");
    //        out.println("<a href=\"setparameters?topAlertsToStore=50\">50</a>");
    //        out.println("</li>");

        out.println("<li>");
        out.println("<a href=\"setparameters?printHeadersAlways=true&debugNormal=true&\">Debug: normal</a>");
        out.println("</li>");
        
        out.println("<li>");
        out.println("<a href=\"setparameters?printHeadersAlways=true&debugNormal=true&debugDetailed=true\">detailed</a>");
        out.println("</li>");

        out.println("<li>");
        out.println("<a href=\"setparameters?printHeadersAlways=false&debugNormal=false&debugDetailed=false\">off</a>");
        out.println("</li>");
        
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
        
        out.println("<li>");
        out.println("<a href=\"setparameters?resetGlobalStatus=true\">Reset stats</a>");
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
