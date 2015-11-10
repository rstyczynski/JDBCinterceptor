package styczynski.weblogic.jdbc.debug.report;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.ListIterator;

import styczynski.weblogic.jdbc.monitor.CFG;
import styczynski.weblogic.jdbc.monitor.JDBCmonitor;

/**
 * Bounded array storing N elements.
 *
 * Data is not sorted. Each new element is placed on position related to data count modulo array size.
 * Such rule gives an array which overrites oldest entry when new is added to the full structure.
 *
 * Example:
Adding initial 5 elements. Note thtat 5 is at location 0 as cnt(5) % 5(size) = 0
[sql=sql5,lasted=5,modifiers=[],timestamp=1427230523816]
[sql=sql1,lasted=1,modifiers=[],timestamp=1427230523816]
[sql=sql2,lasted=2,modifiers=[],timestamp=1427230523816]
[sql=sql3,lasted=3,modifiers=[],timestamp=1427230523816]
[sql=sql4,lasted=4,modifiers=[],timestamp=1427230523816]
Adding one more
[sql=sql5,lasted=5,modifiers=[],timestamp=1427230523816]
[sql=sql6,lasted=6,modifiers=[],timestamp=1427230523817]
[sql=sql2,lasted=2,modifiers=[],timestamp=1427230523816]
[sql=sql3,lasted=3,modifiers=[],timestamp=1427230523816]
[sql=sql4,lasted=4,modifiers=[],timestamp=1427230523816]
Adding one more
[sql=sql5,lasted=5,modifiers=[],timestamp=1427230523816]
[sql=sql6,lasted=6,modifiers=[],timestamp=1427230523817]
[sql=sql7,lasted=7,modifiers=[],timestamp=1427230523817]
[sql=sql3,lasted=3,modifiers=[],timestamp=1427230523816]
[sql=sql4,lasted=4,modifiers=[],timestamp=1427230523816]
Adding one more
[sql=sql5,lasted=5,modifiers=[],timestamp=1427230523816]
[sql=sql6,lasted=6,modifiers=[],timestamp=1427230523817]
[sql=sql7,lasted=7,modifiers=[],timestamp=1427230523817]
[sql=sql8,lasted=8,modifiers=[],timestamp=1427230523817]
[sql=sql4,lasted=4,modifiers=[],timestamp=1427230523816]
Adding one more
[sql=sql5,lasted=5,modifiers=[],timestamp=1427230523816]
[sql=sql6,lasted=6,modifiers=[],timestamp=1427230523817]
[sql=sql7,lasted=7,modifiers=[],timestamp=1427230523817]
[sql=sql8,lasted=8,modifiers=[],timestamp=1427230523817]
[sql=sql9,lasted=9,modifiers=[],timestamp=1427230523817]
Adding one more
[sql=sql10,lasted=10,modifiers=[],timestamp=1427230523817]
[sql=sql6,lasted=6,modifiers=[],timestamp=1427230523817]
[sql=sql7,lasted=7,modifiers=[],timestamp=1427230523817]
[sql=sql8,lasted=8,modifiers=[],timestamp=1427230523817]
[sql=sql9,lasted=9,modifiers=[],timestamp=1427230523817]
Adding one more
[sql=sql10,lasted=10,modifiers=[],timestamp=1427230523817]
[sql=sql11,lasted=11,modifiers=[],timestamp=1427230523818]
[sql=sql7,lasted=7,modifiers=[],timestamp=1427230523817]
[sql=sql8,lasted=8,modifiers=[],timestamp=1427230523817]
[sql=sql9,lasted=9,modifiers=[],timestamp=1427230523817]
Adding one more
[sql=sql10,lasted=10,modifiers=[],timestamp=1427230523817]
[sql=sql11,lasted=11,modifiers=[],timestamp=1427230523818]
[sql=sql12,lasted=12,modifiers=[],timestamp=1427230523818]
[sql=sql8,lasted=8,modifiers=[],timestamp=1427230523817]
[sql=sql9,lasted=9,modifiers=[],timestamp=1427230523817]
Adding one more
[sql=sql10,lasted=10,modifiers=[],timestamp=1427230523817]
[sql=sql11,lasted=11,modifiers=[],timestamp=1427230523818]
[sql=sql12,lasted=12,modifiers=[],timestamp=1427230523818]
[sql=sql13,lasted=13,modifiers=[],timestamp=1427230523818]
[sql=sql9,lasted=9,modifiers=[],timestamp=1427230523817]
 */
public class TopAlertsArray  {
    private int maxSize = CFG.getTopAlertsToStore();
    private ExecutionAlert[] alertList;
    private int cnt = 0;
    
    public TopAlertsArray() {
       alertList = new ExecutionAlert[maxSize];
    }

    public TopAlertsArray(int _size) {
       this.maxSize = _size;
        alertList = new ExecutionAlert[maxSize];
    }

    public void addAlert(ExecutionAlert alert){
        //cnt counts in loop from 1 to maxSize
        if (cnt++ == maxSize) cnt=1;
        
        alertList[cnt % maxSize] = alert;

        //System.out.println("cnt=" + cnt + ", cnt % maxSize=" + (cnt % maxSize));
    
    }
    
    public void clear(){
        for(int cnt=0; cnt<alertList.length; cnt++){
            alertList[cnt] = null;
        }
            
    }
    
    public ExecutionAlert[] getAlerts(){
        return alertList;
    }

    public static void main(String[] args){
        TopAlertsArray alertList = new TopAlertsArray();
        
        alertList.addAlert(new ExecutionAlert("sql1",1));
        alertList.addAlert(new ExecutionAlert("sql2",2));
        alertList.addAlert(new ExecutionAlert("sql3",3));
        alertList.addAlert(new ExecutionAlert("sql4",4));
        alertList.addAlert(new ExecutionAlert("sql5",5));
        
        for (int i=0; i<alertList.maxSize; i++) {
            ExecutionAlert alert = (ExecutionAlert)alertList.getAlerts()[i];
            System.out.println(alert);
        }
        
        System.out.println("Adding one more");
        alertList.addAlert(new ExecutionAlert("sql6",6));

        for (int i=0; i<alertList.maxSize; i++) {
            ExecutionAlert alert = (ExecutionAlert)alertList.getAlerts()[i];
            System.out.println(alert);
        }
        
        
        System.out.println("Adding one more");
        alertList.addAlert(new ExecutionAlert("sql7",7));

        for (int i=0; i<alertList.maxSize; i++) {
            ExecutionAlert alert = (ExecutionAlert)alertList.getAlerts()[i];
            System.out.println(alert);
        }
        
        System.out.println("Adding one more");
        alertList.addAlert(new ExecutionAlert("sql8",8));

        for (int i=0; i<alertList.maxSize; i++) {
            ExecutionAlert alert = (ExecutionAlert)alertList.getAlerts()[i];
            System.out.println(alert);
        }
        
        System.out.println("Adding one more");
        alertList.addAlert(new ExecutionAlert("sql9",9));

        for (int i=0; i<alertList.maxSize; i++) {
            ExecutionAlert alert = (ExecutionAlert)alertList.getAlerts()[i];
            System.out.println(alert);
        }
        
        
        System.out.println("Adding one more");
        alertList.addAlert(new ExecutionAlert("sql10",10));

        for (int i=0; i<alertList.maxSize; i++) {
            ExecutionAlert alert = (ExecutionAlert)alertList.getAlerts()[i];
            System.out.println(alert);
        }
        
        
        System.out.println("Adding one more");
        alertList.addAlert(new ExecutionAlert("sql11",11));

        for (int i=0; i<alertList.maxSize; i++) {
            ExecutionAlert alert = (ExecutionAlert)alertList.getAlerts()[i];
            System.out.println(alert);
        }
        
        
        System.out.println("Adding one more");
        alertList.addAlert(new ExecutionAlert("sql12",12));

        for (int i=0; i<alertList.maxSize; i++) {
            ExecutionAlert alert = (ExecutionAlert)alertList.getAlerts()[i];
            System.out.println(alert);
        }
        
        
        System.out.println("Adding one more");
        alertList.addAlert(new ExecutionAlert("sql13",13));

        for (int i=0; i<alertList.maxSize; i++) {
            ExecutionAlert alert = (ExecutionAlert)alertList.getAlerts()[i];
            System.out.println(alert);
        }
        
        
    }
}
