package styczynski.weblogic.jdbc.debug;

import java.util.HashMap;
import java.util.LinkedList;

import styczynski.weblogic.jdbc.debug.report.TopAlertsArray;
import styczynski.weblogic.jdbc.monitor.JDBCmonitor;

/**
 * Keeps state machine state.
 */
public class JDBCcallFSMstate {
    StateInterface currentState;
    HashMap<StateInterface, Boolean> processedStates;
    String statement;
    LinkedList<MethodDescriptor> modifiers;
    ExecutionTimer timer;
    long lasted = 0;
    NotificationDescriptor notification = null;
    TopAlertsArray topAlerts = new TopAlertsArray(JDBCmonitor.getTopAlertsToStore());
    
    long timeUpdated;
    
    public JDBCcallFSMstate(){
        initialize();
    }

    public JDBCcallFSM initialize(){
        this.setTimer(null);
        this.setCurrentState(JDBCcallFSM.INITIAL);
        this.setProcessedStates(new HashMap<StateInterface, Boolean>());
        this.setLasted(new Long(0));
        this.setStatement("(none)");
        this.setModifiers(new LinkedList<MethodDescriptor>());
        this.setNotification(null);
        
        return JDBCcallFSM.INITIAL;
    }

    public void setCurrentState(StateInterface currentState) {
        this.currentState = currentState;
        this.timeUpdated = System.currentTimeMillis();;
    }

    public StateInterface getCurrentState() {
        return currentState;
    }

    public void setProcessedStates(HashMap<StateInterface, Boolean> processedStates) {
        this.processedStates = processedStates;
        this.timeUpdated = System.currentTimeMillis();;
    }

    public HashMap<StateInterface, Boolean> getProcessedStates() {
        return processedStates;
    }

    public void setStatement(String statement) {
        this.statement = statement;
        this.timeUpdated = System.currentTimeMillis();;
    }

    public String getStatement() {
        return statement;
    }

    public void setModifiers(LinkedList<MethodDescriptor> modifiers) {
        this.modifiers = modifiers;
        this.timeUpdated = System.currentTimeMillis();;
    }

    public LinkedList<MethodDescriptor> getModifiers() {
        return modifiers;
    }

    public void setTimer(ExecutionTimer timer) {
        this.timer = timer;
        this.timeUpdated = System.currentTimeMillis();;
    }

    public ExecutionTimer getTimer() {
        return timer;
    }

    public void setLasted(Long lasted) {
        this.lasted = lasted;
        this.timeUpdated = System.currentTimeMillis();;
    }

    //long is used here to distinguish between 0 execution time and not initialized
    public Long getLasted() {
        return lasted;
    }
    
    public long getCurrentExecutionTime() {
        long result;
        
        if (timer != null) {
            return timer.getLasted();
        } else {
            result = 0;
        }
        return result;
    }

    public void setNotification(NotificationDescriptor notification) {
        this.notification = notification;
        this.timeUpdated = System.currentTimeMillis();;
    }

    public NotificationDescriptor getNotification() {
        return notification;
    }

    public void setTopAlerts(TopAlertsArray topAlerts) {
        this.topAlerts = topAlerts;
        this.timeUpdated = System.currentTimeMillis();;
    }

    public TopAlertsArray getTopAlerts() {
        return topAlerts;
    }

    public long getTimeUpdated() {
        return timeUpdated;
    }

    public String toString() {
        StringBuffer result = new StringBuffer();

        result.append("[ state=");
        result.append(currentState);
        result.append(", statement=");
        result.append(statement);
        
        result.append(", started=");
        if (timer != null)
            result.append(timer.getStarted());
        else
            result.append("(none)");
        
        result.append(", lasted=");
        if (timer != null)
            result.append(timer.getLasted());
        else
            result.append("(none)");

        result.append(", timeUpdated=");
        result.append(timeUpdated);
        
        result.append("]");

        return result.toString();
    }

}
