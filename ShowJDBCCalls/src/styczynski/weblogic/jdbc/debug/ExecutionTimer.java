package styczynski.weblogic.jdbc.debug;

/**
 * Execution timer. Start time measurement at time of creation.
 */
public class ExecutionTimer {
    long start;

    public ExecutionTimer() {
        this.start = System.currentTimeMillis();
    }

    public long getStarted() {
        return this.start;
    }

    public long getLasted() {
        return System.currentTimeMillis() - this.start;
    }

    public String toString() {
        StringBuffer result = new StringBuffer();

        result.append("[ started=");
        result.append(start);
        result.append(", lasted=");
        result.append(getLasted());
        result.append("]");

        return result.toString();
    }
}
