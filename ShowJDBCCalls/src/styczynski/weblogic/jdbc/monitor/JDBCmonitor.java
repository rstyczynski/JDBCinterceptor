package styczynski.weblogic.jdbc.monitor;

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
import styczynski.weblogic.jdbc.debug.StateInterface;
import styczynski.weblogic.jdbc.debug.report.ExecutionAlert;

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
public class JDBCmonitor implements weblogic.jdbc.extensions.DriverInterceptor {

    //TODO Change method name lookups from list to hashmap. Lookup time will be dramatically lower.

    //TODO Find out why level may become negative

    //TODO Report stack trace next to lasting sql call 
    //
    
    //TODO Recognize associated DataSource
    //     (a) to be able to configure different interceptrs in independent way
    //     (b) use connection to get this data, do it once in instance lifetime

    //TODO Add data masking for sensitive fields e.g. passwords
    //     (a) for SQL text in execute*(sql)
    //     (b) for parameters of prepared statement

    //TODO Add possibility to filter requests
    //SQL include/exclude
    //String sqlInclude = "";
    //String sqlExclude = "";
    //
    //     ex1: all UPDATE but not on MDS
    //              incl: UPDATE
    //              excl: MDS
    //     ex2: everything but not MDS
    //              incl: null
    //              excl: MDS
    //
    //     Filtering may be processed on alerting or state machine level
    //     (a) Alerting
    //          FSM captures all executions, and alerting filters what should be reported
    //     (b) State Machine
    //          State Machine processor has capability to hold processing until next initial state detection.
    //          Processing hold will be done during SQL detection:
    //          (a) prepare(sql) -> makes sense as after this step, a lot of processing may happen
    //          (b) execute(sql) -> in pre method
    //
    //          -> Implement it in willProcess method (hold check)
    //          -> hold should be kept in ThreadLocal
    //

    //SQL execution time threshold
    private static long sqlMaxExecutionTime = 1000;

    //Number of alerts to keep per thread
    private static int topAlertsToStore = 50;

    //debug control
    private static boolean printHeadersAlways = true; //print headers for each interception
    private static boolean debugNormal = false;
    private static boolean debugDetailed = false;

    //logger
    //logger uses class name to make possible deplyment of multiple interceptors with own log files
    private Log log = LogFactory.getLog(this.getClass());

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
    private ThreadLocal <JDBCcallFSMstate>fsmStateThreadLocal = new ThreadLocal <JDBCcallFSMstate>() {
        protected JDBCcallFSMstate initialValue() {
        
            if (debugNormal)
                printMethodHeader(levelCurrent(), "initializeState");
            
            JDBCcallFSMstate result = new JDBCcallFSMstate();
                
// initialization moved to constructor
//            result.setTimer(null);
//            result.setCurrentState(JDBCcallFSM.INITIAL);
//            result.setProcessedStates(new HashMap<StateInterface, Boolean>());
//            result.setLasted(new Long(0));
//            result.setStatement("(none)");
//            result.setModifiers(new LinkedList<MethodDescriptor>());
//            result.setNotification(null);
  
            //thread is enough as a key, but I'll keep reference to a interceptor class
            //it makes possible to derive data source name
            String thisInstance = JDBCmonitor.this.toString() + "@" + Thread.currentThread().getName();
            JDBCmonitor.jdbcGlobalState.put(thisInstance, result);
                
            if (debugNormal)
                printMethodHeader(levelCurrent(), "initializeState completed");
            
            return result;
        }
    };
    private JDBCcallFSMstate getFsmState() {
        return fsmStateThreadLocal.get();
    }
//    CRITICAL - there is no setter - it's created only once when thread is started
//    private void setFsmState(JDBCcallFSMstate fsmState) {
//        fsmStateThreadLocal.set(fsmState);
//    }

    //TODO to support prepared statement preprepared in different
    //thread/call it's necessary to maintain map sql_text=map(ps)

    //CRITICAL Must be local thread
    //jdbc call depth. kept out of getFsmStateNotThreadSafe() by intension
    private ThreadLocal <Integer>level = new ThreadLocal <Integer>() {
        protected Integer initialValue() { 
            return 1;
        }
    };

    //CRITICAL TODO - verify that this does not affect performance of the system
    //status map - interface to reporting interfaces
    static private ConcurrentHashMap<String, JDBCcallFSMstate> jdbcGlobalState =
        new ConcurrentHashMap<String, JDBCcallFSMstate>();
    public static ConcurrentHashMap<String, JDBCcallFSMstate> getJdbcGlobalState() {
        return jdbcGlobalState;
    }

    public JDBCmonitor() {
        //moved to thread local init
        //initializeState();
    }

    //WebLogic JDBC interceptor method
    public Object preInvokeCallback(Object currentObject, String currentMethod,
                                    Object[] currentParams) throws SQLException {

        try {
            //critical take fsm state from a map
            //getFsmStateNotThreadSafe() = fsmSharedlState.get(this.toString());

            //not needed. ThreadLocal will be always initialized
            //initializeStateIfNeeded();
            int level = levelIncrease();

            processInvocation("preInvokeCallback", level, currentObject, currentMethod, currentParams, null);
        } catch (Throwable th) {
            log.error("Error processing preInvokeCallback", th);
        }
        return null;
    }

    //WebLogic JDBC interceptor method
    public void postInvokeCallback(Object currentObject, String currentMethod, Object[] currentParams,
                                   Object currentResult) throws SQLException {
        try {
            //critical take fsm state from a map
            //getFsmStateNotThreadSafe() = fsmSharedlState.get(this.toString());

            //not needed. ThreadLocal will be always initialized
            //initializeStateIfNeeded();
            int level = levelCurrent();

            processInvocation("postInvokeCallback", level, currentObject, currentMethod, currentParams, currentResult);

        } catch (Throwable th) {
            log.error("Error processing postInvokeCallback", th);
        } finally {
            levelDecrease();    
        }
    }

    //WebLogic JDBC interceptor method
    public void postInvokeExceptionCallback(Object o, String string, Object[] os,
                                            Throwable thrwbl) throws SQLException {

        //TODO Decide what should be done in case of intercepted exception
        //     ignore - it does not influence db call analysis
        //     report as normal post processing - if e.g. execution may fail after some time e.g.
        //          (a) somewhere inside of long runnig PL/SQL
        //          (b) execute may be broken by db side timeout exeption
        //
        //     Decission: execution status reporting must be implemented

        //TODO Add execution result - to repoert exceptional finalization
        try {
            //critical take fsm state from a map
            //getFsmStateNotThreadSafe() = fsmSharedlState.get(this.toString());

            String callBack = "postInvokeExceptionCallback";
            //initializeStateIfNeeded();
            int level = levelCurrent();
            printMethodHeader(level, callBack);
        } catch (Throwable th) {
            log.error("Error processing postInvokeExceptionCallback", th);
        } 

        levelDecrease();
    }

    //main logic for WebLogic JDBC interceptor
    //(a) initializes FSM if needed
    //(b) calls FSM processor
    private void processInvocation(String callBack, int level, Object currentObject, String currentMethod,
                                   Object[] currentParams, Object currentResult) {

        boolean headerPrinted = false;
        if (printHeadersAlways) {
            printMethodHeader(level, callBack);
            headerPrinted = true;
        }

        try {
            String executedObject = currentObject != null ? currentObject.getClass().getName() : "(none)";
            String executedMethod = currentMethod;
            //Object executedParameter = currentParams.length > 0 ? currentParams[0] : "(none)";
            Object[] executedParameter = currentParams;
            String returnedObject = currentResult != null ? currentResult.getClass().getName() : "(none)";

            if (printHeadersAlways) {
                printMethodHeader(level, executedObject, executedMethod, executedParameter);
                headerPrinted = true;
            }

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


            //process state machine transition
            processState(callBack, level, headerPrinted, executedObject, executedMethod, executedParameter);

        //Internal exception of interceptor CANNOT be thrown to intercepted method.
        //Interceptor must be invisible for the system
        } catch (Throwable th) {
            //always log exception
            log.error("Error processing intercepted method", th);
        }
    }

    //FSM processor
    //(a) checks if current event fits current state
    //(b) executes transition logic
    //(c) updates FSM state
    //(d) receives alert from transition logic
    private void processState(String callBack, int level, boolean headerPrinted, String executedObject,
                              String executedMethod, Object[] executedParameter) {

        JDBCcallFSM currentState = (JDBCcallFSM) getFsmState().getCurrentState();
        boolean processCommand = false;
        if (currentState.willProcess(getFsmState(), executedObject, executedMethod, executedParameter, callBack)) {
            processCommand = true;
        } else {
            //added handler for unexpected INITIAL state - e.g. in case of execute w/o close
            processCommand = JDBCcallFSM.INITIAL.willProcess(getFsmState(), executedObject, executedMethod, executedParameter, callBack);
            if(processCommand) {
                log.debug(level + "Initial command in unexpected place. Was previous operation executed w/o proper close? Initializing state for this new flow.");
                currentState = getFsmState().initialize();
                
            }
        }
        if (processCommand) {
            if (!headerPrinted) {
                if (debugNormal) {
                    printMethodHeader(level, callBack);
                    printMethodHeader(level, executedObject, executedMethod, executedParameter);
                    headerPrinted = true;
                }
            }
            if (printHeadersAlways)
                log.debug(level + ": " + "Info: " + "Current interception has execution handler for state : " +
                          currentState + ". Will proceed.");

            //start time measurment if configured for this state
            if (currentState.isTimeMeasurementStartPoint()) {
                getFsmState().setTimer(new ExecutionTimer());
            }

            //process state transition
            JDBCcallFSM nextState =
                currentState.process(getFsmState(), executedObject, executedMethod, executedParameter, callBack);
            if (debugDetailed)
                log.debug(level + ": " + "Info: " + "State processing completed. Next state:" + nextState);


            //stop time measurement if configured for next state
            //next state retruend from processor means that processing is in the the next state
            //current event (invocation) moved processing from stateA->stateB
            if (nextState.isTimeMeasurementStopPoint()) {
                ExecutionTimer timer = (ExecutionTimer) getFsmState().getTimer();

                if (timer != null) {
                    getFsmState().setLasted(timer.getLasted());
                    //getFsmStateNotThreadSafe().setTimer(null);
                } else {
                    log.warn("Requested time measurement but timer was not started. Error in definition of state machine.");
                }
            }

            //if next state is FINAL -> initialize state
            if (nextState.equals(JDBCcallFSM.FINAL)) {
                if (debugNormal)
                    log.debug("FINAL state detected. Resetting state.");
                
                //NONONO -> I'm keeping alerts here
                //CRITICAL ThreadLocal must be cleared after completion of processing
                //fsmStateThreadLocal.remove();
                //thread local init will do
                initializeState();
            }

            //check if notification flag was set by FSM
            //notofication is triggered after finishing FSM
            //this logic need to check if execution result should be reported to operator
            if (isNotificationRaised()) {
                NotificationDescriptor notification = getFsmState().getNotification();
                if (debugDetailed)
                    log.debug("Notification raised:" + notification);

                Long lasted = getFsmState().getLasted();
                if (debugDetailed)
                    log.debug("Lasted=" + lasted);

                if (lasted == null) {
                    log.debug("Raised alert, but has no time stop flag defined in state machine");
                    ExecutionTimer timer = (ExecutionTimer) getFsmState().getTimer();
                    lasted = timer.getLasted();
                    getFsmState().setLasted(lasted);
                }

                if (lasted >= sqlMaxExecutionTime) {
                    if (debugDetailed)
                        log.debug("Time bigger than defined:" + lasted);

                    //it's a good way to pass data. no need to copy data
                    ExecutionAlert alert = new ExecutionAlert(getFsmState().getStatement(), lasted, getModifiers());
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
                            buffer.append("       \\------" + i + ": " + getModifiers().get(i).toString());
                            buffer.append("\n");
                        }
                    }
                    log.warn(buffer.toString());
                }
                clearNotification();
            }

            //cleanup when state machine reached initial state
            //NONONO! Why? none->none is possible
            //handle it by notification
            //            if(nextState == JDBCcallFSM.none){
            //                initializeState();
            //            } else {
            getFsmState().setCurrentState(nextState);

            if (debugNormal)
                printState(level);
        } else {
            if (printHeadersAlways)
                log.debug(level + ": " + "Info: " + "Current interception has no execution handler for state: " +
                          currentState);
        }
    }
// not needed - init of thread local will do
//    //Conditional FSM initialization
//    //(a) initializes FSM for a new thread
//    //(b) initializes FSM if entry state is detected
//    private void reinitializeStateIfNeeded(String callBack, int level, String executedObject, String executedMethod,
//                                           Object[] executedParameter) {
//        //Note about threading
//
//
//        //---------------------
//        //in case of spawning new thread, interceptor is not created.
//        //old, existing interceptor class is used. The reason of this is that interceptor is
//        //created per jdbc object - when data source is created.
//        //
//        //It's what you see in server log file during DataSource startup:
//        //<Driver Interceptor class styczynski.weblogic.jdbc.debug.ShowJDBCCalls loaded.> 
//        //
//        //in case of failure:
//        //<Unable to load class "styczynski.weblogic.jdbc.debug.ShowJDBCCallsC", got exception : java.lang.ClassNotFoundException: styczynski.weblogic.jdbc.debug.ShowJDBCCallsC. Driver Interception feature disabled.>
//        initializeStateIfNeeded();
//        if (JDBCcallFSM.INITIAL.willProcess(getFsmState(), executedObject, executedMethod, executedParameter, callBack)) {
//            if (debugNormal)
//                log.debug(level + ": " + "Info: " + "Initial state processing condition. Will do.");
//            initializeState();
//        }
//    }

// not neede. init is done by thread local.
//    //Conditional FSM initialization
//    //(a) initializes FSM for a new thread
//    private void initializeStateIfNeeded() {
//        //Note about threading
//        //---------------------
//        //in case of spawning new thread, interceptor is not created.
//        //old, existing interceptor class is used. The reason if this is that interceptor is
//        //created per jdbc object - possibly when connection is created.
//        //ThreadLocal varaibles are identified by thread name - changing thread gives unitialized variables.
//
//
//        //replaced from ThreadLocal to map indexed by class identifier. class is assigned with jndbc thread pool objects
//        //threads are changing....
//        if (getFsmState() == null) {
//            initializeState();
//        }
//    }

    //FSM initialization moved to thread local init
    public StateInterface initializeState() {

        StateInterface result = null;

        try {

            if (debugNormal)
                printMethodHeader(levelCurrent(), "initializeState");

//          CRITICAL. State initialisation is not conditional. Always create new state.
//          NONONO! Moved to thread initialisation
//            if (getFsmState() == null) {
//                setFsmState(new JDBCcallFSMstate());
//            }
            
            //getFsmStateNotThreadSafe() is always initialized by declaration
            //            //Thread local may NOT be initialized
            //            if (getFsmStateNotThreadSafe() == null) {
            //                getFsmStateNotThreadSafe() = new JDBCcallFSMstate();
            //
            //                //connect fsm state with fsm execution class
            //                //fsmSharedlState.put(this.toString(), getFsmStateNotThreadSafe());
            //            }

            //timer is initialized in process method. Reset if timer here.
       
            getFsmState().setTimer(null);

            getFsmState().setCurrentState(JDBCcallFSM.INITIAL);
            getFsmState().setProcessedStates(new HashMap<StateInterface, Boolean>());
            getFsmState().setLasted(new Long(0));
            getFsmState().setStatement("(none)");
            getFsmState().setModifiers(new LinkedList<MethodDescriptor>());
            getFsmState().setNotification(null);

            //clear jdbc call depth
            //this.level.remove();
            level.set(1);
            //levelIncrease();

            //TODO add possibility to disable global reporting. Alerting to ODL will work, but user interface not.
            //global reporting. structure used to comunicate with user interface
            //ment to be synchronization free
            //String thisInstance = this.toString() + "@" + Thread.currentThread().getName();
            //if (!JDBCmonitor.jdbcGlobalState.contains(thisInstance)) {
            //JDBCmonitor.jdbcGlobalState.put(thisInstance, getFsmState());
            //}

            if (debugNormal)
                printMethodHeader(levelCurrent(), "initializeState completed");

        } catch (Throwable th) {
            log.error("Can't initialize JDBC interceptor state machine.", th);
        }

        return JDBCcallFSM.INITIAL;
    }


    // getters / seters

    public static void setSqlMaxExecutionTime(long sqlMaxExecutionTime) {
        JDBCmonitor.sqlMaxExecutionTime = sqlMaxExecutionTime;
    }

    public static long getSqlMaxExecutionTime() {
        return sqlMaxExecutionTime;
    }


    public static void setTopAlertsToStore(int topAlertsToStore) {
        JDBCmonitor.topAlertsToStore = topAlertsToStore;
    }

    public static int getTopAlertsToStore() {
        return topAlertsToStore;
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
        if (debugDetailed)
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
                if (debugDetailed)
                    log.debug("isProcessed: State " + state + " not on a status map! Adding...");
                states.put(state, false);
                result = false;
            }
        } else {
            if (debugDetailed)
                log.debug("isProcessed: State " + state + " is null!");
            result = false;
        }

        return result;
    }

    //helper debug methods
    private void printException(int level, Throwable th) {

        StringBuffer buffer = new StringBuffer();

        buffer.append(level + ": " + "Exception: " + th.getMessage());
        buffer.append("\n");
        for (int i = 0; i < th.getStackTrace().length; i++) {
            buffer.append(level + ": " + th.getStackTrace()[i]);
            buffer.append("\n");
        }

        log.error(buffer.toString(), th);
    }


    public static void setPrintHeadersAlways(boolean printHeadersAlways) {
        JDBCmonitor.printHeadersAlways = printHeadersAlways;
    }

    public static boolean isPrintHeadersAlways() {
        return printHeadersAlways;
    }

    public static void setDebugNormal(boolean debugNormal) {
        JDBCmonitor.debugNormal = debugNormal;
    }

    public static boolean isDebugNormal() {
        return debugNormal;
    }

    public static void setDebugDetailed(boolean debugDetailed) {
        JDBCmonitor.debugDetailed = debugDetailed;
    }

    public static boolean isDebugDetailed() {
        return debugDetailed;
    }

    // debug support code
    private void printMethodHeader(int level, String method) {
        StringBuffer buffer = new StringBuffer();

        buffer.append(level + ": " + "---------------------------");
        buffer.append("\n");
        buffer.append(level + ": " + method + " on " + Thread.currentThread().getId() + ":" +
                      Thread.currentThread().getName() + " via " + this);

        log.debug(buffer.toString());
    }

    private void printMethodHeader(int level, String executedObject, String executedMethod,
                                   Object[] executedParameter) {
        StringBuffer buffer = new StringBuffer();

        buffer.append(level + ": " + "----- Parameters");
        buffer.append("\n");
        buffer.append(level + ": Object...:" + executedObject);
        buffer.append("\n");
        buffer.append(level + ": Method...:" + executedMethod);
        buffer.append("\n");
        buffer.append(level + ": Parameter:");
        if (executedParameter != null) {
            buffer.append("\n");
            for (int i = 0; i < executedParameter.length; i++) {

                buffer.append(level + ": \\------" + i + ": " +
                              //Info: parameter may be set to null value.
                              executedParameter[i] != null ? executedParameter[i].toString() :
                              "(null)" + ", " + executedParameter[i] != null ?
                              executedParameter[i].getClass().getName() : "(null)");
                buffer.append("\n");
            }
        } else {
            buffer.append("(none)");
            buffer.append("\n");
        }
        buffer.append(level + ": Modifiers:");
        if (getModifiers() != null) {
            buffer.append("\n");
            for (int i = 0; i < getModifiers().size(); i++) {
                buffer.append(level + ": \\------" + i + ": " + getModifiers().get(i).toString());
                buffer.append("\n");
            }
        } else {
            buffer.append("(none)");
            buffer.append("\n");
        }

        log.debug(buffer.toString());
    }

    private void printMethodHeader(int level, String executedObject, String executedMethod, Object[] executedParameter,
                                   String returnedObject) {
        StringBuffer buffer = new StringBuffer();

        buffer.append(level + ": " + "----- Parameters");
        buffer.append("\n");
        buffer.append(level + ": Object...:" + executedObject);
        buffer.append("\n");
        buffer.append(level + ": Method...:" + executedMethod);
        buffer.append("\n");
        buffer.append(level + ": Parameter:");

        if (executedParameter != null) {
            buffer.append("\n");
            for (int i = 0; i < executedParameter.length; i++) {
                buffer.append(level + ": \\------" + i + ": " + executedParameter[i].toString() + ", " +
                              executedParameter[i].getClass().getName());
                buffer.append("\n");
            }
        } else {
            buffer.append("(none)");
            buffer.append("\n");
        }
        buffer.append(level + ": Modifiers:");
        if (getModifiers() != null) {
            buffer.append("\n");
            for (int i = 0; i < getModifiers().size(); i++) {
                buffer.append(level + ": \\------" + i + ": " + getModifiers().get(i).toString());
                buffer.append("\n");
            }
        } else {
            buffer.append("(none)");
            buffer.append("\n");
        }

        buffer.append(level + ": Returned.:" + returnedObject);
        buffer.append("\n");

        log.debug(buffer.toString());
    }

    private void printUnwrapInfo(int level, String comment, String unwrappedObject) {

        StringBuffer buffer = new StringBuffer();

        buffer.append(level + ": " + "----- Unwrapped");
        buffer.append("\n");
        buffer.append(level + ": What...:" + comment);
        buffer.append("\n");
        buffer.append(level + ": Object...:" + unwrappedObject);
        buffer.append("\n");

        log.debug(buffer.toString());
    }

    private void printState(int level) {

        StringBuffer buffer = new StringBuffer();

        buffer.append(level + ": " + "----- State");
        buffer.append("\n");
        buffer.append(level + ": " + "current state:" + getFsmState().getCurrentState());
        buffer.append("\n");
        buffer.append(level + ": " + "next state(s):" + Arrays.toString(getFsmState().getCurrentState().getNextStates()));
        buffer.append("\n");
        buffer.append(level + ": " + "statement....:" + getFsmState().getStatement());
        buffer.append("\n");
        buffer.append(level + ": " + "started......:" + getFsmState().getTimer());
        buffer.append("\n");
        buffer.append(level + ": " + "lasted.......:" + getFsmState().getLasted());
        buffer.append("\n");

        log.debug(buffer.toString());
    }

    //JDBC call depth helper methods
    //one JDBC call may call another jdbc methods e.g. getConnectionMetadata is called internally by another
    //jdbc method. Level is added to be able to recognize such internal calls.
    private int levelIncrease() {
//        int level = 1;
//        Integer _level =  this.level.get();
//        if (_level != null) {
//            level = _level.intValue() + 1;
//        }
//        this.level.set(new Integer(level));

        level.set(level.get() + 1);
        if (debugDetailed)
            log.debug("Level increase:" + level.get());
        return level.get();
    }

    private void levelDecrease() {
//        int level = 1;
//        Integer _level = this.level.get();
//        if (_level != null ) {
//            level = _level.intValue() - 1;
//        }
//        this.level.set(new Integer(level));

        level.set(level.get() - 1);
        if (debugDetailed)
            log.debug("Level decrease:" + level.get());
    }

    private int levelCurrent() {
//        int level = 1;
//        Integer _level = (Integer) this.level.get();
//        if (_level != null) {
//            level = _level.intValue();
//        }
        return this.level.get();
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
