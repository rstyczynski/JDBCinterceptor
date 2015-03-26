package styczynski.weblogic.jdbc.debug;

/**
 * State machine interface
 */
public interface StateInterface {

    /**
     * Informs if current context is valid for processing any transition
     * Note that this method does not inform about next state
     *
     * @param FSMstate             Object keeping state machine's state
     * @param executedObject       Name of intercepted object
     * @param executedMethod       Name of interepted method
     * @param executedParameter    Array of intercepted arguments
     * @param callBack             Name of a interception function:
     *                                  preInvokeCallback, postInvokeCallback, postInvokeExceptionCallback
     * @return                     True/false indication if any transition may be processed
     */
    public boolean willProcess(JDBCcallFSMstate FSMstate, String executedObject, String executedMethod,
                               Object[] executedParameter, String callBack);

    /**
     * Process transition logic and return next state.
     * Note that transition logic belongs to current state.
     *
     * Logic to handle entering EXECUTED state must be handled in previous state: PREPARED, CONFIGURED
     *
     * [PREPARED---logic--->] [EXECUTED]
     * [CONFIGURED---logic--->] [EXECUTED]
     *
     * @param FSMstate Object keeping state machine's state
     * @param executedObject Name of intercepted object
     * @param executedMethod Name of interepted method
     * @param executedParameter Array of intercepted arguments
     * @param callBack Name of a interception function:
     * preInvokeCallback, postInvokeCallback, postInvokeExceptionCallback
     * @return Next state, after execution of transition logic
     */
    public JDBCcallFSM process(JDBCcallFSMstate FSMstate, String executedObject, String executedMethod,
                               Object[] executedParameter, String callBack);

    /**
     * Returns array of next states.
     */
    public JDBCcallFSM[] getNextStates(); 

    /**
     * Informs if state should be used to start time measurement
     *
     */
    public boolean isTimeMeasurementStartPoint();

    /**
     * Informs if state should be used to stop time measurement
     *
     */
    public boolean isTimeMeasurementStopPoint();

}
