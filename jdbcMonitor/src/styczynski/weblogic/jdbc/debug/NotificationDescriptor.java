package styczynski.weblogic.jdbc.debug;

/**
 * Structure used to raise notifications from state machine logic to sm processor.
 */
public class NotificationDescriptor {
    private String who;
    private String why;
    private String what;


    public NotificationDescriptor(String who, String why, String what) {
        super();
        this.who = who;
        this.why = why;
        this.what = what;
    }

    public void setWho(String who) {
        this.who = who;
    }

    public String getWho() {
        return who;
    }

    public void setWhy(String why) {
        this.why = why;
    }

    public String getWhy() {
        return why;
    }

    public void setWhat(String what) {
        this.what = what;
    }

    public String getWhat() {
        return what;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[");
        buffer.append("who=" + who + ",");
        buffer.append("why=" + why + ",");
        buffer.append("what=" + what + "]");

        return buffer.toString();
    }
}

