package styczynski.weblogic.jdbc.debug;

/**
 * Execution timer. Starts time measurement at time of creation.
 */
public class StateExecutionTimer {
    long start;
    int cnt = 0;
    long spent = 0;
    
    public StateExecutionTimer() {
        this.start();
    }

    public long getStarted() {
        return this.start;
    }

    public void start() {
        this.start = System.currentTimeMillis();
        this.cnt++;
    }
    
    public void stop() {
        this.spent =+ getLasted();
        this.start = 0;
    }

    public long getLasted() {
        return System.currentTimeMillis() - this.start;
    }

    public String toString() {
        StringBuffer result = new StringBuffer();

        result.append("[ spent=");
        result.append(this.spent);
        result.append(", cnt=");
        result.append(this.cnt);
        result.append("]");

        return result.toString();
    }
}
