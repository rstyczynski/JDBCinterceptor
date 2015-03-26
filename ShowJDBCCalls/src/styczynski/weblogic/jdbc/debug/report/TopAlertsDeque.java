package styczynski.weblogic.jdbc.debug.report;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.ListIterator;

/**
 * Bounded queue storing N elements.
 * When N+1 is added, element 0 is removed, what makes constant length of the queue.
 * 
 * Switched to more simple TopAlertsArray structure.
 */
public class TopAlertsDeque  {
    private int maxSize = 20;
    private ArrayDeque <ExecutionAlert>alertList;
    private int size = 0;
    
    public TopAlertsDeque() {
       alertList = new ArrayDeque<ExecutionAlert>(maxSize);
    }

    public TopAlertsDeque(int _size) {
       this.maxSize = _size;
        alertList = new ArrayDeque<ExecutionAlert>(maxSize);
    }

    synchronized public void addAlert(ExecutionAlert alert){
        if(size < maxSize){
            alertList.add(alert);
            size++;
        } else {
            alertList.remove();
            alertList.add(alert);
        }    
    }
    
    synchronized public ArrayDeque getAlerts(){
        return alertList;
    }

    public static void main(String[] args){
        ArrayDeque <ExecutionAlert>alertList = new ArrayDeque<ExecutionAlert>();;
        
        alertList.add(new ExecutionAlert("sql1",1));
        alertList.add(new ExecutionAlert("sql2",1));
        alertList.add(new ExecutionAlert("sql3",1));
        alertList.add(new ExecutionAlert("sql4",1));
        
        Iterator itr = alertList.iterator();
        while (itr.hasNext()) {
            ExecutionAlert alert = (ExecutionAlert) itr.next();
            System.out.println(alert);
        }
        
        System.out.println("Removing head - remove()");
        alertList.remove();
        itr = alertList.iterator();
        while (itr.hasNext()) {
            ExecutionAlert alert = (ExecutionAlert) itr.next();
            System.out.println(alert);
        }
        
        System.out.println("Adding tail - add()");
        alertList.add(new ExecutionAlert("sql5",1));
        itr = alertList.iterator();
        while (itr.hasNext()) {
            ExecutionAlert alert = (ExecutionAlert) itr.next();
            System.out.println(alert);
        }
        
    }
}
