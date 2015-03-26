package styczynski.weblogic.jdbc.debug.report;

import java.util.LinkedList;

import styczynski.weblogic.jdbc.debug.MethodDescriptor;

/**
 * Object used to report execution lasting more that defined threshold.
 * It's used in both log based reporting and in the web based one.
 */
public class ExecutionAlert {
    private String statement;
    private long lasted;
    LinkedList<MethodDescriptor> modifiers;
    long timestamp;

    public ExecutionAlert(String statement, long lasted) {
        this.statement = statement;
        this.lasted = lasted;
        this.modifiers = new LinkedList<MethodDescriptor>();
        this.timestamp = System.currentTimeMillis();
    }

    public ExecutionAlert(String statement, long lasted, LinkedList<MethodDescriptor> modifiers) {
        this.statement = statement;
        this.lasted = lasted;
        this.modifiers = modifiers;
        this.timestamp = System.currentTimeMillis();
    }

    public ExecutionAlert(String statement, long lasted, LinkedList<MethodDescriptor> modifiers, long timestamp) {
        this.statement = statement;
        this.lasted = lasted;
        this.modifiers = modifiers;
        this.timestamp = timestamp;
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
