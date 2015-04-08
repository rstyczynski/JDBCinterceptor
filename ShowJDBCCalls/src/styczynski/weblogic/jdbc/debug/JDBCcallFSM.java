package styczynski.weblogic.jdbc.debug;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import styczynski.weblogic.jdbc.monitor.JDBCmonitor;


/**
 *
 * State machine definition for JDBC prepare-configure-execute calls.
 *
 * Following state transitions are defined:
 * <table><tr><th></th><th>non existing</th><th>INITIAL</th><th>CREATED</th><th>PREPARED</th><th>CONFIGURED</th><th>EXECUTED</th></tr><tr><td>non existing</td><td>n/a</td><td>*</td><td>n/a</td><td>n/a</td><td>n/a</td><td>n/a</td></tr><tr><td>INITIAL</td><td>n/a</td><td>*</td><td>createStatement()</td><td>prepareCall(sql)<br>prepareStatement(sql)</td><td>n/a</td><td>n/a</td></tr><tr><td>CREATED</td><td>n/a</td><td>n/a</td><td>n/a</td><td>n/a</td><td>n/a</td><td>executeQuery(sql)<br>executeUpdate(sql)</td></tr><tr><td>PREPARED</td><td>n/a</td><td>n/a</td><td>n/a</td><td>n/a</td><td>*</td><td>executeQuery<br>executeUpdate</td></tr><tr><td>CONFIGURED</td><td>n/a</td><td>n/a</td><td>n/a</td><td>n/a</td><td>*</td><td>executeQuery<br>executeUpdate</td></tr><tr><td>EXECUTED</td><td>close</td><td>n/a</td><td>n/a</td><td>n/a</td><td>n/a</td><td></td></tr></table>
 */
    public enum JDBCcallFSM implements StateInterface {

        // copy/paste this state when creating new one
        template {

            public JDBCcallFSM[] getNextStates() {
                final JDBCcallFSM[] nextStates = new JDBCcallFSM[]{ JDBCcallFSM.INITIAL};
                return nextStates;
            }
            
            public boolean isTimeMeasurementStartPoint() {
                return false;
            };

            public boolean isTimeMeasurementStopPoint() {
                return false;
            };

            public boolean willProcess(JDBCcallFSMstate state, String executedObject, String executedMethod,
                                       Object[] executedParameter, String callBack) {
                boolean result = true;

                return result;
            }

            public JDBCcallFSM process(JDBCcallFSMstate state, String executedObject, String executedMethod,
                                 Object[] executedParameter, String callBack) {
                JDBCcallFSM result = this;

                if (willProcess(state, executedObject, executedMethod, executedParameter, callBack)) {
                    state.setCurrentState(this);

                    result = JDBCcallFSM.INITIAL;

                    //proceesed(state);

                    if (JDBCmonitor.isDebugDetailed())
                        LogFactory.getLog("weblogic.jdbc.debug.fsm").debug("Executed process:" + this);
                }

                return result;
            }

            public void proceesed(JDBCmonitor state) {
                state.updateState(this, true);
            }

            public void reset(JDBCmonitor state) {
                state.updateState(this, false);
            }

            public boolean isProceesed(JDBCmonitor state) {
                return state.isProcessed(this);
            }

            public boolean hasMultipleSates() {
                return this.getNextStates().length > 1;
            }
        },
        
        INITIAL {

            public boolean isTimeMeasurementStartPoint() {
                return true;
            };

            public boolean isTimeMeasurementStopPoint() {
                return false;
            };
            
            public JDBCcallFSM[] getNextStates() {
                final JDBCcallFSM[] nextStates = new JDBCcallFSM[]{ JDBCcallFSM.INITIAL, JDBCcallFSM.PREPARED};
                return nextStates;
            }

            public boolean willProcess(JDBCcallFSMstate state, String executedObject, String executedMethod,
                                       Object[] executedParameter, String callBack) {
                boolean result = false;

                if (JDBCmonitor.isInSet(Constants.connectionObjectSet, executedObject)) {
                    if (JDBCmonitor.isInSet(Constants.connectionMethodPrePostSet, executedMethod + "_" + callBack)) {
                        result = true;
                        if (JDBCmonitor.isDebugDetailed())
                            LogFactory.getLog("weblogic.jdbc.debug.fsm").debug("Executed willProcess:" + this);
                    }
                }

                return result;
            }

            public JDBCcallFSM process(JDBCcallFSMstate state, String executedObject, String executedMethod,
                                 Object[] executedParameter, String callBack) {
                JDBCcallFSM result = this;

                if (willProcess(state, executedObject, executedMethod, executedParameter, callBack)) {

                    state.setCurrentState(this);
                    
                    result = JDBCcallFSM.INITIAL;
                    //prepare* methods prepare statements
                    if (executedMethod.toLowerCase().startsWith("prepare")) {
                        
                        //CRITICAL: save captured SQL
                        state.setStatement(executedParameter.length > 0 ? (String)executedParameter[0] : "(none)");
                                                
                        result = JDBCcallFSM.PREPARED;

                    //create* methods create statements
                    } else if (executedMethod.toLowerCase().startsWith("create")) {
                            result = JDBCcallFSM.CREATED;
                    }

                    //proceesed(state);

                    if (JDBCmonitor.isDebugDetailed())
                        LogFactory.getLog("weblogic.jdbc.debug.fsm").debug("Executed process:" + this);
                }

                return result;
            }

//            public void proceesed(ShowJDBCCalls state) {
//                state.updateState(this, true);
//            }
//
//            public void reset(ShowJDBCCalls state) {
//                state.updateState(this, false);
//            }
//
//            public boolean isProceesed(ShowJDBCCalls state) {
//                return state.isProcessed(this);
//            }

            public boolean hasMultipleSates() {
                return this.getNextStates().length > 1;
            }

        },
        
        CREATED {

            public JDBCcallFSM[] getNextStates() {
                final JDBCcallFSM[] nextStates = new JDBCcallFSM[]{ JDBCcallFSM.EXECUTED};
                return nextStates;
            }
            
            public boolean isTimeMeasurementStartPoint() {
                return false;
            };

            public boolean isTimeMeasurementStopPoint() {
                return false;
            };

            public boolean willProcess(JDBCcallFSMstate state, String executedObject, String executedMethod,
                                       Object[] executedParameter, String callBack) {
                boolean result = true;

                //do not handle execute* w/o parameters
                if (JDBCmonitor.isInSet(Constants.statementObjectSet, executedObject) 
                    &&
                    executedMethod.toLowerCase().startsWith("execute")
                    &&
                    executedParameter.length == 0 //no parameters execute is only for prepared statements
                    &&
                    callBack.toLowerCase().startsWith("post")
                ) {
                    result = false;
                }

                if (JDBCmonitor.isDebugDetailed())
                    LogFactory.getLog("weblogic.jdbc.debug.fsm").debug("Executed willProcess:" + this);

                return result;
            }

            public JDBCcallFSM process(JDBCcallFSMstate state, String executedObject, String executedMethod,
                                 Object[] executedParameter, String callBack) {
                JDBCcallFSM result = this;

                if (willProcess(state, executedObject, executedMethod, executedParameter, callBack)) {

                    state.setCurrentState(this);

                    if (JDBCmonitor.isInSet(Constants.statementObjectSet, executedObject) 
                        &&
                        executedMethod.toLowerCase().startsWith("execute")
                        &&
                        executedParameter.length > 0 
                        &&
                        callBack.toLowerCase().startsWith("post")
                    ) {

                        //CRITICAL: save captured SQL
                        state.setStatement(executedParameter.length > 0 ? (String)executedParameter[0] : "(none)");
                    
                        //CRITICAL
                        state.setNotification(new NotificationDescriptor("jdbc", "execute", "finished:" + this));
                        
                        result = JDBCcallFSM.EXECUTED;                            
                    } 

                    //proceesed(state);

                    if (JDBCmonitor.isDebugDetailed())
                        LogFactory.getLog("weblogic.jdbc.debug.fsm").debug("Executed process:" + this);
                }

                return result;
            }

            public void proceesed(JDBCmonitor state) {
                state.updateState(this, true);
            }

            public void reset(JDBCmonitor state) {
                state.updateState(this, false);
            }

            public boolean isProceesed(JDBCmonitor state) {
                return state.isProcessed(this);
            }

            public boolean hasMultipleSates() {
                return this.getNextStates().length > 1;
            }

        },
        
        PREPARED {

            public JDBCcallFSM[] getNextStates() {
                final JDBCcallFSM[] nextStates = new JDBCcallFSM[]{
                JDBCcallFSM.PREPARED, JDBCcallFSM.CONFIGURED, JDBCcallFSM.EXECUTED};
                return nextStates;
            }

            public boolean isTimeMeasurementStartPoint() {
                return false;
            };

            public boolean isTimeMeasurementStopPoint() {
                return false;
            };

            public boolean willProcess(JDBCcallFSMstate state, String executedObject, String executedMethod,
                                       Object[] executedParameter, String callBack) {
                boolean result = false;

                //execute all commands but not createStatement
                result = true;
                
                if (JDBCmonitor.isDebugDetailed())
                    LogFactory.getLog("weblogic.jdbc.debug.fsm").debug("Executed willProcess:" + this);

                return result;
            }

            public JDBCcallFSM process(JDBCcallFSMstate state, String executedObject, String executedMethod,
                                 Object[] executedParameter, String callBack) {
                JDBCcallFSM result = this;

                if (willProcess(state, executedObject, executedMethod, executedParameter, callBack)) {

                    state.setCurrentState(this);

                    if (JDBCmonitor.isInSet(Constants.statementObjectSet, executedObject) &&
                    JDBCmonitor.isInSet(Constants.statementMethodPostSet, executedMethod + "_" + callBack) &&
                        executedMethod.toLowerCase().startsWith("execute")) {

                        //CRITICAL
                        state.setNotification(new NotificationDescriptor("jdbc", "execute", "finished:" + this));
                        
                        result = JDBCcallFSM.EXECUTED;                            
                    } else {

                        //CRITICAL
                        if ( callBack.startsWith("pre"))
                            state.getModifiers().add(new MethodDescriptor(executedMethod, executedParameter));
                        
                        result = JDBCcallFSM.CONFIGURED;
                    }

                    //proceesed(state);

                    if (JDBCmonitor.isDebugDetailed())
                        LogFactory.getLog("weblogic.jdbc.debug.fsm").debug("Executed process:" + this);
                }

                return result;
            }

            public void proceesed(JDBCmonitor state) {
                state.updateState(this, true);
            }

            public void reset(JDBCmonitor state) {
                state.updateState(this, false);
            }

            public boolean isProceesed(JDBCmonitor state) {
                return state.isProcessed(this);
            }

            public boolean hasMultipleSates() {
                return this.getNextStates().length > 1;
            }

        },
        
        CONFIGURED {

            public JDBCcallFSM[] getNextStates() {
                final JDBCcallFSM[] nextStates = new JDBCcallFSM[]{ JDBCcallFSM.CONFIGURED, JDBCcallFSM.EXECUTED};
                return nextStates;
            }
            

            public boolean isTimeMeasurementStartPoint() {
                return false;
            };

            public boolean isTimeMeasurementStopPoint() {
                return false;
            };

            public boolean willProcess(JDBCcallFSMstate state, String executedObject, String executedMethod,
                                       Object[] executedParameter, String callBack) {
                boolean result = false;

                result = true;
                if (JDBCmonitor.isDebugDetailed())
                    LogFactory.getLog("weblogic.jdbc.debug.fsm").debug("Executed willProcess:" + this);

                return result;
            }

            public JDBCcallFSM process(JDBCcallFSMstate state, String executedObject, String executedMethod,
                                 Object[] executedParameter, String callBack) {
                JDBCcallFSM result = this;

                if (willProcess(state, executedObject, executedMethod, executedParameter, callBack)) {

                    state.setCurrentState(this);

                    if (JDBCmonitor.isInSet(Constants.statementObjectSet, executedObject) &&
                    JDBCmonitor.isInSet(Constants.statementMethodPostSet, executedMethod + "_" + callBack)) {

                        //CRITICAL
                        state.setNotification(new NotificationDescriptor("jdbc", "execute", "finished:" + this));

                        result = JDBCcallFSM.EXECUTED;                            
                    } else {
                        //CRITICAL
                        if ( callBack.startsWith("pre"))
                            state.getModifiers().add(new MethodDescriptor(executedMethod, executedParameter));
                        result = JDBCcallFSM.CONFIGURED;
                    }

                    //proceesed(state);

                    if (JDBCmonitor.isDebugDetailed())
                        LogFactory.getLog("weblogic.jdbc.debug.fsm").debug("Executed process:" + this);
                }

                return result;
            }

//            public void proceesed(ShowJDBCCalls state) {
//                state.updateState(this, true);
//            }
//
//            public void reset(ShowJDBCCalls state) {
//                state.updateState(this, false);
//            }
//
//            public boolean isProceesed(ShowJDBCCalls state) {
//                return state.isProcessed(this);
//            }
//
//            public boolean hasMultipleSates() {
//                return this.getNextStates().length > 1;
//            }

        },
        
        EXECUTED {

            public JDBCcallFSM[] getNextStates() {
                final JDBCcallFSM[] nextStates = new JDBCcallFSM[]{ JDBCcallFSM.CLOSED, JDBCcallFSM.EXECUTED};
                return nextStates;
            }
            
            public boolean isTimeMeasurementStartPoint() {
                return false;
            };

            public boolean isTimeMeasurementStopPoint() {
                return true;
            };

            public boolean willProcess(JDBCcallFSMstate state, String executedObject, String executedMethod,
                                       Object[] executedParameter, String callBack) {
                boolean result = false;

                if (JDBCmonitor.isInSet(Constants.statementObjectSet, executedObject)) {
                    if (JDBCmonitor.isInSet(Constants.statementMethodClosePrePostSet, executedMethod + "_" + callBack)) {
                        result = true;
                        if (JDBCmonitor.isDebugDetailed())
                            LogFactory.getLog("weblogic.jdbc.debug.fsm").debug("Executed willProcess:" + this);
                    }
                }

                return result;
            }

            public JDBCcallFSM process(JDBCcallFSMstate state, String executedObject, String executedMethod,
                                 Object[] executedParameter, String callBack) {
                JDBCcallFSM result = this;

                if (willProcess(state, executedObject, executedMethod, executedParameter, callBack)) {

                    state.setCurrentState(this);

                    if (executedMethod.toLowerCase().equals("close")) {
                        result = JDBCcallFSM.CLOSED;
                    } else {
                        result = this;
                    }

                    //proceesed(state);

                    if (JDBCmonitor.isDebugDetailed())
                        LogFactory.getLog("weblogic.jdbc.debug.fsm").debug("Executed process:" + this);
                }

                return result;
            }

//            public void proceesed(JDBCcallFSMstate state) {
//                state.updateState(this, true);
//            }
//
//            public void reset(JDBCcallFSMstate state) {
//                state.updateState(this, false);
//            }
//
//            public boolean isProceesed(JDBCcallFSMstate state) {
//                return state.isProcessed(this);
//            }

            public boolean hasMultipleSates() {
                return this.getNextStates().length > 1;
            }

    },
        
        CLOSED {
    
            public JDBCcallFSM[] getNextStates() {
                final JDBCcallFSM[] nextStates = new JDBCcallFSM[]{ JDBCcallFSM.FINAL};
                return nextStates;
            }
            
            public boolean isTimeMeasurementStartPoint() {
                return false;
            };

            public boolean isTimeMeasurementStopPoint() {
                return false;
            };

            public boolean willProcess(JDBCcallFSMstate state, String executedObject, String executedMethod,
                                       Object[] executedParameter, String callBack) {
                boolean result = true;

                return result;
            }

            public JDBCcallFSM process(JDBCcallFSMstate state, String executedObject, String executedMethod,
                                 Object[] executedParameter, String callBack) {
                JDBCcallFSM result = this;

                if (willProcess(state, executedObject, executedMethod, executedParameter, callBack)) {

                    state.setCurrentState(this);
                      
                    //CRITICAL to initialize state after completion  
                    //it's handled automatically by FINAL state
                    //state.setNotification(new NotificationDescriptor("jdbc", "closed", "resetFSM"));

                    result = JDBCcallFSM.FINAL;

                    //proceesed(state);

                    if (JDBCmonitor.isDebugDetailed())
                        LogFactory.getLog("weblogic.jdbc.debug.fsm").debug("Executed process:" + this);
                }

                return result;
            }

//            public void proceesed(ShowJDBCCalls state) {
//                state.updateState(this, true);
//            }
//
//            public void reset(ShowJDBCCalls state) {
//                state.updateState(this, false);
//            }
//
//            public boolean isProceesed(ShowJDBCCalls state) {
//                return state.isProcessed(this);
//            }
//
//            public boolean hasMultipleSates() {
//                return this.getNextStates().length > 1;
//            }

        }, 
        
        FINAL {

            public JDBCcallFSM[] getNextStates() {
                final JDBCcallFSM[] nextStates = new JDBCcallFSM[]{ JDBCcallFSM.INITIAL};
                return nextStates;
            }
            
            public boolean isTimeMeasurementStartPoint() {
                return false;
            };

            public boolean isTimeMeasurementStopPoint() {
                return false;
            };

            public boolean willProcess(JDBCcallFSMstate state, String executedObject, String executedMethod,
                                       Object[] executedParameter, String callBack) {
                boolean result = true;

                return result;
            }

            public JDBCcallFSM process(JDBCcallFSMstate state, String executedObject, String executedMethod,
                                 Object[] executedParameter, String callBack) {
                JDBCcallFSM result = this;

                if (willProcess(state, executedObject, executedMethod, executedParameter, callBack)) {
                    state.setCurrentState(this);

                    result = JDBCcallFSM.INITIAL;

                    //proceesed(state);

                    if (JDBCmonitor.isDebugDetailed())
                        LogFactory.getLog("weblogic.jdbc.debug.fsm").debug("Executed process:" + this);
                }

                return result;
            }

            public void proceesed(JDBCmonitor state) {
                state.updateState(this, true);
            }

            public void reset(JDBCmonitor state) {
                state.updateState(this, false);
            }

            public boolean isProceesed(JDBCmonitor state) {
                return state.isProcessed(this);
            }

            public boolean hasMultipleSates() {
                return this.getNextStates().length > 1;
            }
        }
    }