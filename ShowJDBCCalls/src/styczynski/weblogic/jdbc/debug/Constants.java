package styczynski.weblogic.jdbc.debug;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Constants {
    // class/method names are used to recognize FSM states and transitions
    private static final String[] connectionObjectList = {
        "oracle.jdbc.driver.T4CConnection", 
        "oracle.jdbc.driver.LogicalConnection",
        "weblogic.jdbc.wrapper.XAConnection_oracle_jdbc_driver_LogicalConnection"
    };
    public static final Set<String> connectionObjectSet = getConnectionObjectSet();
    private static Set<String> getConnectionObjectSet(){
        final Set set = new HashSet<String>();
        
        for (String key : connectionObjectList ){
          set.add(key);
        }
        
        return Collections.unmodifiableSet(set);
    }

    private static final String[] connectionMethodList = {
        "prepareCall", "prepareStatement", "createStatement", "CreateStatement" };
    public static final Set<String> connectionMethodSet = getConnectionMethodSet();
    private static Set<String> getConnectionMethodSet(){
        final Set set = new HashSet<String>();
        
        for (String key : connectionMethodList ){
          set.add(key);
        }
        
        return Collections.unmodifiableSet(set);
    }
    
    private static final String[] connectionMethodPrePostList = {
        "prepareCall_preInvokeCallback", 
        "prepareStatement_preInvokeCallback", 
        "createStatement_preInvokeCallback",
        "CreateStatement_preInvokeCallback"
    };
    public static final Set<String> connectionMethodPrePostSet = getConnectionMethodPrePostSet();
    private static Set<String> getConnectionMethodPrePostSet(){
        final Set set = new HashSet<String>();
        
        for (String key : connectionMethodPrePostList ){
          set.add(key);
        }
        
        return Collections.unmodifiableSet(set);
    }

    private static final String[] statementObjectList = {
        "oracle.jdbc.driver.OracleCallableStatementWrapper", 
        "oracle.jdbc.driver.OraclePreparedStatementWrapper",
        "oracle.jdbc.driver.OracleStatementWrapper"
    };
    public static final Set<String> statementObjectSet = getStatementObjectSet();
    private static Set<String> getStatementObjectSet(){
        final Set set = new HashSet<String>();
        
        for (String key : statementObjectList ){
          set.add(key);
        }
        
        return Collections.unmodifiableSet(set);
    }
    
    private static final String[] statementMethodList = { "execute", "executeQuery", "executeUpdate", "executeBatch" };
    public static final Set<String> statementMethodSet = getStatementMethodSet();
    private static Set<String> getStatementMethodSet(){
        final Set set = new HashSet<String>();
        
        for (String key : statementMethodList ){
          set.add(key);
        }
        
        return Collections.unmodifiableSet(set);
    }
    
    private static final String[] statementMethodPostList = {
        //post callback is mandatory to compute execution time
        "execute_postInvokeCallback", "executeQuery_postInvokeCallback", "executeUpdate_postInvokeCallback",
        "executeBatch_postInvokeCallback"
    };
    public static final Set<String> statementMethodPostSet = getStatementMethodPostSet();
    private static Set<String> getStatementMethodPostSet(){
        final Set set = new HashSet<String>();
        
        for (String key : statementMethodPostList ){
          set.add(key);
        }
        
        return Collections.unmodifiableSet(set);
    }

    private static final String[] statementMethodClosePrePostList = {
        //Warning: postInvoke is executed with null object, as statement is already closed.
        "close_preInvokeCallback"
    };
    public static final Set<String> statementMethodClosePrePostSet = getStatementMethodClosePrePostSet();
    private static Set<String> getStatementMethodClosePrePostSet(){
        final Set set = new HashSet<String>();
        
        for (String key : statementMethodClosePrePostList ){
          set.add(key);
        }
        
        return Collections.unmodifiableSet(set);
    }

    private static final String[] statementUnwrapObjectList = {
        "weblogic.jdbc.wrapper.CallableStatement_oracle_jdbc_driver_OracleCallableStatementWrapper",
        "weblogic.jdbc.wrapper.PreparedStatement_oracle_jdbc_driver_OraclePreparedStatementWrapper",
        "weblogic.jdbc.wrapper.Statement_oracle_jdbc_driver_OracleStatementWrapper"
    };
    public static final Set<String> statementUnwrapObjectSet = getStatementUnwrapObjectSet();
    private static Set<String> getStatementUnwrapObjectSet(){
        final Set set = new HashSet<String>();
        
        for (String key : statementUnwrapObjectList ){
          set.add(key);
        }
        
        return Collections.unmodifiableSet(set);
    }
    
}
