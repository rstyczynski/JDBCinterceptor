package styczynski.weblogic.jdbc.debug.test;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.CallableStatement;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.HashMap;

import javax.naming.InitialContext;

import javax.servlet.*;
import javax.servlet.http.*;

import javax.sql.DataSource;

import styczynski.weblogic.jdbc.debug.security.Authorization;
import styczynski.weblogic.jdbc.debug.show.HTMLhelper;
import styczynski.weblogic.jdbc.monitor.JDBCmonitor;

public class ExecuteSQL extends HttpServlet {
    private static final String CONTENT_TYPE = "text/html; charset=UTF-8";

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        if(! Authorization.canUserExecuteSQL(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        String dataSource = "jdbc/SOADataSource";
        String sqlCmd = "{call dbms_lock.sleep(?)}";
        boolean noClose = false;
        String param1 = "10";
        String param2 = "5";
        String param3 = "1";

        if (request.getParameter("datasource") != null)
            dataSource = request.getParameter("datasource");
        
        if (request.getParameter("sqlCmd") != null)
            sqlCmd = request.getParameter("sqlCmd");
        
        if (request.getParameter("param1") != null)
            param1 = request.getParameter("param1");
        
        //DONE 0.4
        //fixed param1 was overriten by param2, param3
        if (request.getParameter("param2") != null)
            param2 = request.getParameter("param2");
        
        if (request.getParameter("param3") != null)
            param3 = request.getParameter("param3");

        if (request.getParameter("noClose") != null)
            noClose = true;

        response.setContentType(CONTENT_TYPE);
        PrintWriter out = response.getWriter();
        out.println("<html>");
        
        HashMap props = new HashMap();
        props.put("testInNewPage",false);

        HTMLhelper.addHeaders(out, "Execute SQL", props, request);
            
        out.println("<body>");
        out.println("<p>The servlet has received a GET. This is the reply.</p>");


        try {
            InitialContext ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup(dataSource);
            

            int executionCnt = 1;
            if(isInteger(param3)){
                executionCnt = Integer.valueOf(param3);
            } 
            
            Connection conn = ds.getConnection();
            
            for (int i=0; i<executionCnt; i++){

                CallableStatement cstmt = conn.prepareCall(sqlCmd);
                                    
                if(isInteger(param2)){
                    Thread.sleep(Integer.valueOf(param2));
                }
    
                try {
                    cstmt.setString(1, param1);
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                }
                
                if(isInteger(param2)){
                    Thread.sleep(Integer.valueOf(param2));
                }
                
                
                cstmt.execute();
                
                
                if(isInteger(param2)){
                    Thread.sleep(Integer.valueOf(param2));
                }
                
                if(!noClose) {
                    cstmt.close();
                }
            }
            
            if(!noClose) {
                conn.close();
            }
              
        } catch (Throwable th) {
            th.printStackTrace(out);
        }
        
        
        out.println("</body></html>");
        out.close();
    }
    
    public static boolean isInteger(String s) {
        
        if (s == null)
            return false;
        
        try { 
            Integer.parseInt(s); 
        } catch(NumberFormatException e) { 
            return false; 
        }
        // only got here if we didn't return false
        return true;
    }
}
