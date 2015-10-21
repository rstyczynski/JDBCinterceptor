package styczynski.weblogic.jdbc.debug.report;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//http://chriswu.me/blog/a-lru-cache-in-10-lines-of-java/
public class TopHistogramMap<K, ExecutionHistogram> extends LinkedHashMap<K, ExecutionHistogram> {
    private int cacheSize;

    private ConcurrentHashMap<K, ExecutionHistogram> readBuffer = new ConcurrentHashMap<K, ExecutionHistogram>();

    public TopHistogramMap(int cacheSize) {
        super(16, 0.75f, true);
        this.cacheSize = cacheSize;
    }

    @Override
    public ExecutionHistogram get(Object key) {
        
        //return (ExecutionHistogram)super.get(key);
        
        return this.readBuffer.get(key);
    }

    // synchronization is not required as change operation on histogram is always single threaded
    @Override
    public ExecutionHistogram put(K key, ExecutionHistogram value) {
        
        this.readBuffer.put(key, value);
        
        return super.put(key, value);
    }
    
    protected boolean removeEldestEntry(Map.Entry<K, ExecutionHistogram> eldest) {
        boolean removeThisElement = (size() > cacheSize);
        
        if (removeThisElement) {
            //TODO6) 
            // -> flush this element to log file
            this.readBuffer.remove(eldest.getKey());
            
        } else {
            //TODO6) 
            this.readBuffer.put(eldest.getKey(), eldest.getValue());
        }
        
        return removeThisElement;
    }


    public ConcurrentHashMap<K, ExecutionHistogram> getReadBuffer() {
        return readBuffer;
    }

    public static void main(String[] args) {
        System.out.println("Prepare map for 5 items");
        TopHistogramMap fixedSizeMap = new TopHistogramMap(5);

        System.out.println("Initial load of 5 items");
        for (int cnt = 1; cnt <= 5; cnt++) {
            fixedSizeMap.put(cnt, cnt);
        }
        System.out.println(fixedSizeMap + "->" + fixedSizeMap.getReadBuffer()); 

        System.out.println("Load 5 more items");
        for (int cnt = 6; cnt <= 10; cnt++) {
            fixedSizeMap.put(cnt, cnt);
            System.out.println(cnt + ":" + fixedSizeMap + "->" + fixedSizeMap.getReadBuffer()); 
        }

        System.out.println("Update item key=6, and load 5 more items");
        Object tmp = fixedSizeMap.put(6,1);
        for (int cnt = 11; cnt <= 15; cnt++) {
            fixedSizeMap.put(cnt, cnt);
            System.out.println(cnt + ":" + fixedSizeMap + "->" + fixedSizeMap.getReadBuffer()); 
        }
        
        System.out.println("Get item key=11, and load 5 more items");
        tmp = fixedSizeMap.get(11);
        for (int cnt = 16; cnt <= 20; cnt++) {
            fixedSizeMap.put(cnt, cnt);
            System.out.println(cnt + ":" + fixedSizeMap + "->" + fixedSizeMap.getReadBuffer()); 
        }
    }
}

