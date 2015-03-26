package styczynski.weblogic.jdbc.debug;

public class Constants {
    // class/method names are used to recognize FSM states and transitions
    public static final String[] connectionObjectList = {
        "oracle.jdbc.driver.T4CConnection", 
        "oracle.jdbc.driver.LogicalConnection",
        "weblogic.jdbc.wrapper.XAConnection_oracle_jdbc_driver_LogicalConnection"
    };

    public static final String[] connectionMethodList = {
        "prepareCall", "prepareStatement", "createStatement", "CreateStatement" };

    public static final String[] connectionMethodPrePostList = {
        "prepareCall_preInvokeCallback", 
        "prepareStatement_preInvokeCallback", 
        "createStatement_preInvokeCallback",
        "CreateStatement_preInvokeCallback"
    };

    public static final String[] statementObjectList = {
        "oracle.jdbc.driver.OracleCallableStatementWrapper", 
        "oracle.jdbc.driver.OraclePreparedStatementWrapper",
        "oracle.jdbc.driver.OracleStatementWrapper"
    };

    public static final String[] statementMethodList = { "execute", "executeQuery", "executeUpdate", "exacuteBatch" };

    public static final String[] statementMethodPostList = {
        //post callback is mandatory to compute execution time
        "execute_postInvokeCallback", "executeQuery_postInvokeCallback", "executeUpdate_postInvokeCallback",
        "exacuteBatch_postInvokeCallback"
    };

    public static final String[] statementMethodClosePrePostList = {
        //Warning: postInvoke is executed with null object, as statement is already closed.
        "close_preInvokeCallback"
    };


    public static final String[] statementUnwrapObjectList = {
        "weblogic.jdbc.wrapper.CallableStatement_oracle_jdbc_driver_OracleCallableStatementWrapper",
        "weblogic.jdbc.wrapper.PreparedStatement_oracle_jdbc_driver_OraclePreparedStatementWrapper",
        "weblogic.jdbc.wrapper.Statement_oracle_jdbc_driver_OracleStatementWrapper"
    };

}
