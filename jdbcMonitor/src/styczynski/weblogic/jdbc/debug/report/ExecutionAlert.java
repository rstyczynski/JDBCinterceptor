package styczynski.weblogic.jdbc.debug.report;

import java.util.HashMap;
import java.util.LinkedList;

import styczynski.weblogic.jdbc.debug.MethodDescriptor;
import styczynski.weblogic.jdbc.debug.StateExecutionTimer;
import styczynski.weblogic.jdbc.debug.StateInterface;

/**
 * Object used to report execution lasting more that defined threshold.
 * It's used in both log based reporting and in the web based one.
 */
public class ExecutionAlert {
    private String statement;
    private long lasted;
    LinkedList<MethodDescriptor> modifiers;
    long timestamp;
    String statesTiming;

    public ExecutionAlert(String statement, long lasted) {
        this.statement = statement;
        this.lasted = lasted;
        this.modifiers = new LinkedList<MethodDescriptor>();
        this.statesTiming = "";
        this.timestamp = System.currentTimeMillis();
    }

    public ExecutionAlert(String statement, long lasted, LinkedList<MethodDescriptor> modifiers, String statesTiming) {
        this.statement = statement;
        this.lasted = lasted;
        this.modifiers = modifiers;
        this.timestamp = System.currentTimeMillis();
        this.statesTiming = statesTiming;
    }

    public ExecutionAlert(String statement, long lasted, LinkedList<MethodDescriptor> modifiers, long timestamp) {
        this.statement = statement;
        this.lasted = lasted;
        this.modifiers = modifiers;
        this.timestamp = timestamp;
    }


    public void setStatesTiming(String statesTiming) {
        this.statesTiming = statesTiming;
    }

    public String getStatesTiming() {
        return statesTiming;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public String getStatement() {
        return statement;
    }

    public void setLasted(long lapsed) {
        this.lasted = lapsed;
    }

    public long getLasted() {
        return lasted;
    }

    public void setModifiers(LinkedList<MethodDescriptor> modifiers) {
        this.modifiers = modifiers;
    }

    public LinkedList<MethodDescriptor> getModifiers() {
        return modifiers;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[");
        buffer.append("sql=" + statement + ",");
        buffer.append("lasted=" + lasted + ",");
        buffer.append("modifiers=" + modifiers + ",");
        buffer.append("timestamp=" + timestamp + "]");

        return buffer.toString();
    }
}
