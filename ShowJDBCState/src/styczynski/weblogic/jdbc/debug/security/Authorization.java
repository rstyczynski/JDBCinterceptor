package styczynski.weblogic.jdbc.debug.security;

import javax.servlet.http.HttpServletRequest;

import styczynski.weblogic.jdbc.monitor.CFG;

public class Authorization {
    
    public static boolean canUserView(HttpServletRequest request) {
        return 
        request.isUserInRole(CFG.getRoleMonitor()) ||        
        request.isUserInRole(CFG.getRoleOperator()) ||
        request.isUserInRole(CFG.getRoleTester()) ||        
        request.isUserInRole(CFG.getRoleAdmin());   
    }

    public static boolean canUserReset(HttpServletRequest request) {
        return 
        request.isUserInRole(CFG.getRoleMonitor()) ||        
        request.isUserInRole(CFG.getRoleOperator()) ||
        request.isUserInRole(CFG.getRoleTester()) ||        
        request.isUserInRole(CFG.getRoleAdmin());   
    }

    public static boolean canUserSetThreshold(HttpServletRequest request) {
        return 
        //request.isUserInRole(CFG.getRoleMonitor()) ||        
        request.isUserInRole(CFG.getRoleOperator()) ||
        request.isUserInRole(CFG.getRoleTester()) ||        
        request.isUserInRole(CFG.getRoleAdmin());   
    }
    
    public static boolean canUserEnableDisable(HttpServletRequest request) {
        return 
        //request.isUserInRole(CFG.getRoleMonitor()) ||        
        request.isUserInRole(CFG.getRoleOperator()) ||
        request.isUserInRole(CFG.getRoleTester()) ||        
        request.isUserInRole(CFG.getRoleAdmin());   
    }
    
    public static boolean canUserExecuteSQL(HttpServletRequest request) {
        return 
        //request.isUserInRole(CFG.getRoleMonitor()) ||        
        //request.isUserInRole(CFG.getRoleOperator()) ||
        request.isUserInRole(CFG.getRoleTester()) ||        
        request.isUserInRole(CFG.getRoleAdmin());   
    }
    
    public static boolean canUserControlDebug(HttpServletRequest request) {
        return 
        //request.isUserInRole(CFG.getRoleMonitor()) ||        
        //request.isUserInRole(CFG.getRoleOperator()) ||
        //request.isUserInRole(CFG.getRoleTester()) ||        
        request.isUserInRole(CFG.getRoleAdmin());   
    }

    public static boolean canUserConfigure(HttpServletRequest request) {
        return 
        //request.isUserInRole(CFG.getRoleMonitor()) ||        
        //request.isUserInRole(CFG.getRoleOperator()) ||
        //request.isUserInRole(CFG.getRoleTester()) ||        
        request.isUserInRole(CFG.getRoleAdmin());   
    }    
    
    
}
