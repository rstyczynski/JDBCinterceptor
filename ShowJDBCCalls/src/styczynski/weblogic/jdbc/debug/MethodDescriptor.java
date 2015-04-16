package styczynski.weblogic.jdbc.debug;

/**
 * Method descriptor is used to capure modifiers executed on prepared statement
 * A modifier is any method like e.g. setInteger(1,2001), but it may setPollable or execute
 * Modifiers are stored and reported together with statement*
*/
public class MethodDescriptor {

    private String name;
    private Object[] parameters;
    
    
    public MethodDescriptor(String name, Object[] parameters) {
        super();
        this.name = name;
        this.parameters = parameters;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public String toString() {
        StringBuffer result = new StringBuffer();

        result.append("[");
        result.append("name=" + getName() + ", ");
        
        if (this.parameters == null) {
            result.append("parameters=(none)"); 
        } else {
            result.append("parameters=[");
            for (int i = 0; i < getParameters().length; i++) {
                result.append(getParameters()[i]);
                if (i < getParameters().length - 1)
                    result.append(", ");
            }
            result.append("]");            
        }
        result.append("]");
        return result.toString();
    }
}
