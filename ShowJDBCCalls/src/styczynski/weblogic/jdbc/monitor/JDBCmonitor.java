package styczynski.weblogic.jdbc.monitor;

import java.io.PrintWriter;
import java.io.StringWriter;

import java.sql.SQLException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import styczynski.weblogic.jdbc.debug.ExecutionTimer;
import styczynski.weblogic.jdbc.debug.JDBCcallFSM;
import styczynski.weblogic.jdbc.debug.JDBCcallFSMstate;
import styczynski.weblogic.jdbc.debug.MethodDescriptor;
import styczynski.weblogic.jdbc.debug.NotificationDescriptor;
import styczynski.weblogic.jdbc.debug.StateExecutionTimer;
import styczynski.weblogic.jdbc.debug.StateInterface;
import styczynski.weblogic.jdbc.debug.report.ExecutionAlert;
import styczynski.weblogic.jdbc.debug.report.ExecutionHistogram;
import styczynski.weblogic.jdbc.debug.report.TopHistogramMap;

/**
 * WebLogic JDBC interceptor reporting SQL executions lasting for more than defined threashold.
 * In case of sloq SQL detection, alert is reported with (a) SQL, (b) execution time, and (c) execution parameters.
 *
 * Example:
 * SQL execution lasted more than expected.
 * |------lasted [ms]:1
 * |------SQL:SELECT MAX(TXN_CN) FROM MDS_TRANSACTIONS WHERE TXN_PARTITION_ID = ?
 * |------with following modifiers:
 *        \------0: [name=setPoolable, parameters=[true]]
 *        \------1: [name=setInt, parameters=[1, 26]]
 *        \------2: [name=executeQuery, parameters=[]]
 *
 * Configuration:
 * 1. modify sqlMaxExecutionTime varaible to set threadshold, and debug flags if needed
 * 2. compile and create jar file
 * 3. add wlsJDBCmonitor.jar to POST_CLASSPATH
 * 4. specify fully qualified class name in WLS configuration by Console: DataSource/Diagnostics/Driver Interceptor
 * 5. comnfigure ODL to handle interceptor events
 * 6. restart server
 * 7. expect alerts in ODL *-sql.log file
 *
 * Processing of intercepted events is handled by internal state machine
 *
 * Notes:
 * 1. Interceptor jar located in domain/lib directory is not recognized. Must be placed in system classpath.
 *
 * @version 0.4
 */
@SuppressWarnings("deprecation")
public class JDBCmonitor implements weblogic.jdbc.extensions.DriverInterceptor {

    //logger uses class name to make possible deplyment of multiple interceptors with own log files
    private Log log = LogFactory.getLog(this.getClass());

    //TODO Check if loggers with the same name are safe in multithreaded environment
    //     
    
    //TODO Externalize connfigurables
    //     (a) MBean
    //     (b) configuration file
    //     DONE (c) servlet

    //TODO Maintain number of executions per SQL
    //     (a) all captured SQL
    //          available by default as FSM handles all events
    //     (b) only filtered ones
    //

    //TODO Maintain histogram of executions per all SQL
    //     (a) all captured SQL
    //          available by default as FSM handles all events
    //     (b) only filtered ones
    //

    //TODO Externalize runtime metrics
    //     (a) MBean
    //
    //     Problem: how to store metrics in efficient way
    //     DONE (a) not to overload memory
    //          define space for top XX SQL. Use LinkedBlockingDeque ?
    //
    //     DONE (b) to avoid synchronisation in multithreaded execution environment
    //          store metrics in ThreadLocal
    //              Problem: it's not possible to access JVM ThreadLocal collection
    //              Solutiuon: push metrics from time to time to LinkedBlockingDeque()
    
    //TODO to support prepared statement preprepared in different
    //     thread/call it's necessary to maintain map sql_text=map(ps)
    
    //TODO Decide if unwrapping is necessary in current code.
    //     Current flow does not use result object to correlate events
    //     It is assumed that full invocation chain (prepare, confiugre, execute)
    //     is done during one call on the same thread.

    //TODO
    // Unwrapping is necessary in case of tracing preapred statements objects
    // not necessary now
    //            //unwrap
    //            if (isOnList(Constants.statementUnwrapObjectList, executedObject)) {
    //                if (currentObject instanceof weblogic.jdbc.wrapper.PreparedStatement) {
    //                    weblogic.jdbc.wrapper.PreparedStatement wrappedPS =
    //                        (weblogic.jdbc.wrapper.PreparedStatement) currentObject;
    //                    PreparedStatement psOrg = (PreparedStatement) wrappedPS.getVendorObj();
    //                    executedObject = psOrg.getClass().getName();
    //                }
    //                if (debugNormal) printUnwrapInfo(level, "executedObject:", executedObject);
    //            }
    //
    //
    //            //unwrap result
    //            if (returnedObject != null && isOnList(Constants.statementUnwrapObjectList, returnedObject)) {
    //
    //                if (currentResult instanceof weblogic.jdbc.wrapper.PreparedStatement) {
    //                    weblogic.jdbc.wrapper.PreparedStatement wrappedPS =
    //                        (weblogic.jdbc.wrapper.PreparedStatement) currentResult;
    //                    PreparedStatement psOrg = (PreparedStatement) wrappedPS.getVendorObj();
    //                    returnedObject = psOrg.getClass().getName();
    //                }
    //                if (debugNormal)  printUnwrapInfo(level, "resultObject:", returnedObject);
    //            }


    //FSM state variable
    //------------------
    //Processing state is kept in locally as it's expected that each
    //jdbc call consist of prepare->configure->execute executed in a single thread.
    //
    //Such strategy eliminates a need to maintain shared state between threads. Thanks to this 
    //operation is almost synchronisation free - each thread works in independent mode.
    //
    //Note that prepared statment in some other place, possibly at start of the system i not supported.
    //Such handling of the statement makes it impossible to determine SQL text and correlate it with 
    //configure->execute operations.
    //
    //
    //Note that the same instance of interceptor may be used by many threads thus ThreadLocal varable must be used.
    //#1 styczynski.weblogic.jdbc.monitor.JDBCmonitor3@e0c135d@[ACTIVE] ExecuteThread: '0' for queue: 'weblogic.kernel.Default (self-tuning)'
    //#2 styczynski.weblogic.jdbc.monitor.JDBCmonitor3@e0c135d@[ACTIVE] ExecuteThread: '4' for queue: 'weblogic.kernel.Default (self-tuning)'
    //#3 styczynski.weblogic.jdbc.monitor.JDBCmonitor3@e0c135d@[ACTIVE] ExecuteThread: '1' for queue: 'weblogic.kernel.Default (self-tuning)'
    //
    //Thread local value is initialized once. 
    private ThreadLocal <JDBCcallFSMstate>fsmStateThreadLocal = 
        new ThreadLocal <JDBCcallFSMstate>() {
            protected JDBCcallFSMstate initialValue() {
                if (CFG.debugNormal)
                    printMethodHeader("initializeState");
                
                JDBCcallFSMstate result = new JDBCcallFSMstate();
                    
                //thread is enough as a key, but I'll keep reference to a interceptor class
                //it makes possible to derive assiciated data source name
                //this information will be displayed on a web interface or in logs
                String thisInstance = JDBCmonitor.this.toString() + "@" + Thread.currentThread().getName();
                JDBCmonitor.jdbcGlobalState.put(thisInstance, result);
                    
                if (CFG.debugNormal)
                    printMethodHeader("initializeState completed");
                
                return result;
            }
    };
    private JDBCcallFSMstate getFsmState() {
        return (JDBCcallFSMstate)fsmStateThreadLocal.get();
    }

    //CRITICAL Must be local thread. JDBC call depth. Kept out of getFsmStateNotThreadSafe() by intension
    private ThreadLocal <Integer>threadLevel = new ThreadLocal <Integer>() {
        protected Integer initialValue() { 
            return 0;
        }
    };

    //CRITICAL TODO - verify that this does not affect performance of the system
    //Rather not. It's ued only during thread creation to add new entry
    //After theat client (web) reads this map. Only reads.
    //
    //status map - interface to reporting interfaces
    static private ConcurrentHashMap<String, JDBCcallFSMstate> jdbcGlobalState =
        new ConcurrentHashMap<String, JDBCcallFSMstate>();
    
    public static ConcurrentHashMap<String, JDBCcallFSMstate> getJdbcGlobalState() {
        return jdbcGlobalState;
    }

    public JDBCmonitor() {
        //FSM state initialization moved to thread local init
        //WLS uses the same interceptor instance to handle all threads. 
        //No instance level variables may be used.
    }

    //WebLogic JDBC interceptor method
    //called before JDBC call
    //
    //Sequence: pre -> jdbc -> post
    //
    public Object preInvokeCallback(Object jdbcObject, String jdbcMethod, Object[] jdbcParams) throws SQLException {

        final String interceptorStage = "preInvokeCallback";
            
        final String   executedObject = jdbcObject != null ? jdbcObject.getClass().getName() : "(none)";
        final String   executedMethod = jdbcMethod != null ? jdbcMethod : "(none)";
        final Object[] executedParameter = jdbcParams;

        log.debug("preInvokeCallback:" + executedObject + ":" + executedMethod);

        StateInterface currentState = null;
        try {
            currentState = getFsmState().getCurrentState();
            
            boolean headerPrinted = false;
            if ((CFG.debugNormal || CFG.debugDetailed) && CFG.printHeadersAlways) {
                printMethodHeader("pre:" + currentState);
                headerPrinted = true;
            }
            
            if ((CFG.debugNormal || CFG.debugDetailed) && CFG.printHeadersAlways) {
                printMethodHeader(currentState, executedObject, executedMethod, executedParameter);
                headerPrinted = true;
            }
            
            boolean processCommand = currentState.willProcess(getFsmState(), executedObject, executedMethod, executedParameter, interceptorStage);
            if (!processCommand) {
                //added handler for unexpected INITIAL state - e.g. in case of execute w/o close
                processCommand = JDBCcallFSM.INITIAL.willProcess(getFsmState(), executedObject, executedMethod, executedParameter, interceptorStage);
                if(processCommand) {
                    log.debug("Initial command in unexpected place. Was previous operation executed w/o proper close? Initializing state for this new flow.");
                    currentState = getFsmState().initialize();
                }
            }
            
            if (processCommand) {
                if (!headerPrinted) {
                    if (CFG.debugNormal) {
                        printMethodHeader(interceptorStage);
                        printMethodHeader(currentState, executedObject, executedMethod, executedParameter);
                        headerPrinted = true;
                    }
                }
                if ((CFG.debugNormal || CFG.debugDetailed) && CFG.printHeadersAlways)
                log.debug("Info: " + "Current interception has execution handler for state : " +
                          currentState + ". Will proceed.");

                //measure state processing time                        
                if( getFsmState().getStatesTiming().containsKey(currentState)){
                    getFsmState().getStatesTiming().get(currentState).start();
                    log.debug("timing start:" + currentState);
                } else { //first time mesaurement - initialize
                    log.debug("timing init:" + currentState);
                    getFsmState().getStatesTiming().put(currentState, new StateExecutionTimer());
                    getFsmState().getStatesTiming().get(currentState).start();
                }
                
                //start time measurment if configured for this state
                if (currentState.isTimeMeasurementStartPoint()) {
                    ExecutionTimer timer = new ExecutionTimer();
                    log.debug("Starting timer " + timer);
                    getFsmState().setTimer(timer);
                }
            } else {
                log.debug("Nothing to do in preInvokeCallback." + ", " + executedObject + ":" + executedMethod);
            }
            
        } catch (Throwable th) {
            log.error("Error processing preInvokeCallback", th);
            printException("preInvokeCallback", currentState, executedObject, executedMethod, executedParameter, "none", th);
        }
        return null;
    }

    //WebLogic JDBC interceptor method
    public void postInvokeCallback(Object jdbcObject, String jdbcMethod, 
                                   Object[] jdbcParams, Object jdbcResult) throws SQLException {
        
        final String interceptorStage = "postInvokeCallback";
            
        
        final String   executedObject = jdbcObject != null ? jdbcObject.getClass().getName() : "(none)";
        final String   executedMethod = jdbcMethod != null ? jdbcMethod : "(none)";
        final Object[] executedParameter = jdbcParams;
        final String   returnedObject = jdbcResult != null ? jdbcResult.getClass().getName() : "(none)";
        
        log.debug("postInvokeCallback:" + executedObject + ":" + executedMethod + ", "  + Arrays.toString(executedParameter) + ", " + returnedObject);
        
        StateInterface currentState = null;
        try {
            currentState = getFsmState().getCurrentState();
            
            boolean headerPrinted = false;
            if ((CFG.debugNormal || CFG.debugDetailed) && CFG.printHeadersAlways) {
                printMethodHeader("post:" + currentState);
                headerPrinted = true;
            }

            if ((CFG.debugNormal || CFG.debugDetailed) && CFG.printHeadersAlways) {
                printMethodHeader(currentState, executedObject, executedMethod, executedParameter, returnedObject);
                headerPrinted = true;
            }
            
            boolean processCommand = currentState.willProcess(getFsmState(), executedObject, executedMethod, executedParameter, interceptorStage);
            if (processCommand) {

                //measure state finalization time
                if( getFsmState().getStatesTiming().containsKey(currentState)){
                    getFsmState().getStatesTiming().get(currentState).stop();
                    log.debug("timing stop:" + currentState);
                } else { //first time mesaurement - not possible
                    log.debug("State time measurement not initialized in post state:" + currentState);
                }
                
                JDBCcallFSM nextState = currentState.process(getFsmState(), executedObject, executedMethod, executedParameter, interceptorStage);
                
                if (CFG.debugDetailed)
                    log.debug("State processing completed. Next state:" + nextState);


                if (nextState.isTimeMeasurementStopPoint()) {
                    ExecutionTimer timer = (ExecutionTimer) getFsmState().getTimer();
                    log.debug("Stopping timer: " + timer);
                    
                    if (timer != null) {
                        getFsmState().setLasted(timer.getLasted());
                        getFsmState().setTimer(null);
                    } else {
                        log.warn("Requested time measurement but timer was not started. Error in definition of state machine.");
                    }
                }
                               
                if (nextState.isTimeMeasurementStopPoint()) {
//                    NotificationDescriptor notification = getFsmState().getNotification();
//                    if (debugDetailed)
//                        log.debug("Notification raised:" + notification);

                    Long lasted = getFsmState().getLasted();
                    if (CFG.debugDetailed)
                        log.debug("Lasted=" + lasted);

                    if (lasted == null) {
                        log.debug("Time measurement stop requested, but has not be started.");
                        lasted = 0L;
                    }

                    //TODO6) Put histogram here.
                    String statement = getFsmState().getStatement();
                    TopHistogramMap topHistogram = getFsmState().getTopHistograms();
                    if ( topHistogram.containsKey(statement)){
                        //statement already known
                        ExecutionHistogram histogram = (ExecutionHistogram)topHistogram.get(statement);
                        histogram.add(lasted);
                    } else {
                        //new statement
                        ExecutionHistogram histogram = new ExecutionHistogram(CFG.getHistogramSlots(), CFG.getHistogramMax());
                        histogram.add(lasted);
                        topHistogram.put(statement, histogram);
                    }

                    if (lasted >= CFG.sqlMaxExecutionTime) {
                        if (CFG.debugDetailed)
                            log.debug("Time bigger than defined:" + lasted);

                        //it's a good way to pass data. no need to copy data
                        ExecutionAlert alert = new ExecutionAlert(getFsmState().getStatement(), lasted, getModifiers(), getFsmState().getStatesTiming().toString());
                        getFsmState().getTopAlerts().addAlert(alert);

                        //TODO Externalize Alert reporting
                        //     Use logger with dedicated log destination
                        StringBuffer buffer = new StringBuffer();

                        buffer.append("SQL execution lasted more than expected.");
                        buffer.append("\n");
                        buffer.append("|------lasted [ms]:" + lasted);
                        buffer.append("\n");
                        buffer.append("|------SQL:" + getFsmState().getStatement());
                        buffer.append("\n");


                        if (getModifiers() != null && getModifiers().size() > 0) {
                            buffer.append("|------with following modifiers:");
                            buffer.append("\n");
                            for (int i = 0; i < getModifiers().size(); i++) {
                                buffer.append("       \\------" + i + getModifiers().get(i).toString());
                                buffer.append("\n");
                            }
                        }
                        log.warn(buffer.toString());
                    }
                    //clearNotification();
                }
                
                getFsmState().setCurrentState(nextState);
                //if next state is FINAL -> initialize state
                if (nextState.equals(JDBCcallFSM.FINAL)) {
                    if (CFG.debugNormal)
                        log.debug("FINAL state detected. Resetting state.");
                    getFsmState().initialize();   
                }
                
            } else {
                log.debug("Nothing to do in postInvokeCallback." + ", " + executedObject + ":" + executedMethod);
            }
                        
        } catch (Throwable th) {
            log.error("Error processing postInvokeCallback", th);
            printException("postInvokeCallback", currentState, executedObject, executedMethod, executedParameter, returnedObject, th);
        }
    }

    //WebLogic JDBC interceptor method
    public void postInvokeExceptionCallback(Object currentObject, String currentMethod, Object[] currentParams,
                                            Throwable exception) throws SQLException {

        //TODO Decide what should be done in case of intercepted exception
        //     ignore - it does not influence db call analysis
        //     report as normal post processing - if e.g. execution may fail after some time e.g.
        //          (a) somewhere inside of long runnig PL/SQL
        //          (b) execute may be broken by db side timeout exeption
        //
        //     Decission: execution status reporting must be implemented

        //TODO Add execution result - to repoert exceptional finalization

        log.debug("postInvokeExceptionCallback: " + ", " + currentObject + ":" + currentMethod);
        StateInterface currentState = null;
        try {
            //measure state invocation time
            //EXPERIMENTAL
            currentState = getFsmState().getCurrentState();
            if( getFsmState().getStatesTiming().containsKey(currentState)){
                getFsmState().getStatesTiming().get(currentState).stop();
                log.debug("timing stop:" + currentState);
            } else { //first time mesaurement - not possible
                log.debug("State time measurement not initialized in post state:" + currentState);
            }
            
            
            String callBack = "postInvokeExceptionCallback";
            //initializeStateIfNeeded();
            printMethodHeader(callBack);
            
        } catch (Throwable th) {
            log.debug("Error processing postInvokeExceptionCallback", th);
            
            //added exception logging as logger does not print stack trace
            String executedObject = currentObject != null ? currentObject.getClass().getName() : "(none)";
            String executedMethod = currentMethod != null ? currentMethod : "(none)";
            Object[] executedParameter = currentParams;
            String returnedObject = "(none)";
            printException("preInvokeCallback", currentState, executedObject, executedMethod, executedParameter, returnedObject, th);

        }
    }


    // getters / seters

    public static void setSqlMaxExecutionTime(long sqlMaxExecutionTime) {
        CFG.sqlMaxExecutionTime = sqlMaxExecutionTime;
    }

    public static long getSqlMaxExecutionTime() {
        return CFG.sqlMaxExecutionTime;
    }


    public LinkedList<MethodDescriptor> getModifiers() {
        return getFsmState().getModifiers();
    }

    private void clearNotification() {
        getFsmState().setNotification(null);
    }

    private boolean isNotificationRaised() {
        boolean notificationRaised = false;

        if (getFsmState().getNotification() != null) {
            notificationRaised = true;
        }

        return notificationRaised;
    }

    public void updateState(StateInterface state, boolean processed) {
        if (CFG.debugDetailed)
            log.debug("updateState: Setting " + state + " to " + processed);
        getFsmState().getProcessedStates().put(state, processed);
    }

    public boolean isProcessed(StateInterface state) {
        boolean result = false;

        HashMap<StateInterface, Boolean> states = getFsmState().getProcessedStates();

        if (state != null) {
            if (states.containsKey(state)) {
                result = (Boolean) states.get(state);
            } else {
                if (CFG.debugDetailed)
                    log.debug("isProcessed: State " + state + " not on a status map! Adding...");
                states.put(state, false);
                result = false;
            }
        } else {
            if (CFG.debugDetailed)
                log.debug("isProcessed: State " + state + " is null!");
            result = false;
        }

        return result;
    }

    //helper debug methods
    private void printException(int level, Throwable th) {

        StringBuffer buffer = new StringBuffer();

        buffer.append("Exception: " + th.getMessage());
        buffer.append("\n");
        for (int i = 0; i < th.getStackTrace().length; i++) {
            buffer.append(th.getStackTrace()[i]);
            buffer.append("\n");
        }

        log.debug(buffer.toString(), th);
    }


    // debug support code
    private void printMethodHeader(String method) {
        StringBuffer buffer = new StringBuffer();

        buffer.append("---------------------------");
        buffer.append("\n");
        buffer.append(method + " on " + Thread.currentThread().getId() + ":" +
                      Thread.currentThread().getName() + " via " + this);

        log.debug(buffer.toString());
    }

    private void printMethodHeader(StateInterface state,
                                   String executedObject, String executedMethod,
                                   Object[] executedParameter) {
        StringBuffer buffer = new StringBuffer();

        buffer.append("----- Parameters");
        buffer.append("\n");
        buffer.append("State...:" + state);
        buffer.append("\n");
        buffer.append("Object...:" + executedObject);
        buffer.append("\n");
        buffer.append("Method...:" + executedMethod);
        buffer.append("\n");
        buffer.append("Parameter:");
        if (executedParameter != null) {
            buffer.append("\n");
            String parameterStr;
            String parameterValStr;
            for (int i = 0; i < executedParameter.length; i++) {
                if (executedParameter[i] != null) {
                    parameterStr = executedParameter[i].toString();
                    parameterValStr = executedParameter[i].getClass().getName();
                } else {
                    parameterStr = "(none)";
                    parameterValStr = "(none)";
                }
                
                buffer.append("\\------" + i + ":" + parameterStr + " " + parameterValStr);
                buffer.append("\n");
            }
        } else {
            buffer.append("(none)");
            buffer.append("\n");
        }
        buffer.append("Modifiers:");
        if (getModifiers() != null) {
            buffer.append("\n");
            for (int i = 0; i < getModifiers().size(); i++) {
                buffer.append("\\------" + i + getModifiers().get(i).toString());
                buffer.append("\n");
            }
        } else {
            buffer.append("(none)");
            buffer.append("\n");
        }

        log.debug(buffer.toString());
    }

    private void printMethodHeader(StateInterface state, 
                                   String executedObject, String executedMethod, Object[] executedParameter,
                                   String returnedObject) {
        StringBuffer buffer = new StringBuffer();

        buffer.append("----- Parameters");
        buffer.append("\n");
        buffer.append("State...:" + state);
        buffer.append("\n");
        buffer.append("Object...:" + executedObject);
        buffer.append("\n");
        buffer.append("Method...:" + executedMethod);
        buffer.append("\n");
        buffer.append("Parameter:");

        if (executedParameter != null) {
            buffer.append("\n");
            String parameterStr;
            String parameterValStr;
            for (int i = 0; i < executedParameter.length; i++) {
                if (executedParameter[i] != null) {
                    parameterStr = executedParameter[i].toString();
                    parameterValStr = executedParameter[i].getClass().getName();
                } else {
                    parameterStr = "(none)";
                    parameterValStr = "(none)";
                }
                
                buffer.append("\\------" + i + ":" + parameterStr + " " + parameterValStr);
                buffer.append("\n");
            }
        } else {
            buffer.append("(none)");
            buffer.append("\n");
        }
        buffer.append("Modifiers:");
        if (getModifiers() != null) {
            buffer.append("\n");
            for (int i = 0; i < getModifiers().size(); i++) {
                buffer.append("\\------" + i + getModifiers().get(i).toString());
                buffer.append("\n");
            }
        } else {
            buffer.append("(none)");
            buffer.append("\n");
        }

        buffer.append("Returned.:" + returnedObject);
        buffer.append("\n");

        log.debug(buffer.toString());
    }

    private void printUnwrapInfo(int level, String comment, String unwrappedObject) {

        StringBuffer buffer = new StringBuffer();

        buffer.append("----- Unwrapped");
        buffer.append("\n");
        buffer.append("What...:" + comment);
        buffer.append("\n");
        buffer.append("Object...:" + unwrappedObject);
        buffer.append("\n");

        log.debug(buffer.toString());
    }

    private void printState(int level) {

        StringBuffer buffer = new StringBuffer();

        buffer.append("----- State");
        buffer.append("\n");
        buffer.append("current state:" + getFsmState().getCurrentState());
        buffer.append("\n");
        buffer.append("next state(s):" + Arrays.toString(getFsmState().getCurrentState().getNextStates()));
        buffer.append("\n");
        buffer.append("statement....:" + getFsmState().getStatement());
        buffer.append("\n");
        buffer.append("started......:" + getFsmState().getTimer());
        buffer.append("\n");
        buffer.append("lasted.......:" + getFsmState().getLasted());
        buffer.append("\n");

        log.debug(buffer.toString());
    }

    private void printException(String callBack, StateInterface currentState, String currentObject, String currentMethod,
                                Object[] currentParams, String currentResult, Throwable th) {
        
        StringBuffer buffer = new StringBuffer();

        buffer.append("----- Exception");
        printMethodHeader(currentState, currentObject, currentMethod, currentParams);
        buffer.append("Exception.:" + th.toString());
        buffer.append("\n");
        //thorwable to string
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        th.printStackTrace(pw);
        buffer.append(sw);
        
        log.debug(buffer.toString());

    }

    // helper methods

    //TODO move to external helper class
    //helper method to check is string in on list of strings
    public static boolean isOnList(String[] list, String word) {
        for (int i = 0; i < list.length; i++) {
            //DONE 0.4 
            //reported as slow operation by JFR
            //if (list[i].toLowerCase().startsWith(word.toLowerCase())) {
            if (list[i].startsWith(word)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isInMap(Map map, String word){
        return map.containsKey(word);
    }
    
    public static boolean isInSet(Set map, String word){
        return map.contains(word);
    }
    
    //NONONO Experimental way to detect connection closed by WebLogic Data Source connection pool.
    //on this stage I have no other means
//    protected void finalize() throws Throwable {
//         try {
//            System.out.println("EXPERIMENTAL: wlsJDBCmonitor finalization");
//         } finally {
//             super.finalize();
//         }
//     }
}
