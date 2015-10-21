package workbench;

import java.util.LinkedHashMap;

import java.util.Map;

public class FixedSizeMap<K, V> extends LinkedHashMap<K, V> {
  private int cacheSize;

  public FixedSizeMap(int cacheSize) {
    super(16, 0.75f, true);
    this.cacheSize = cacheSize;
  }

  protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
    return size() > cacheSize;
  }

    public static void main(String[] args) {
        System.out.println("Prepare map for 5 items");
        FixedSizeMap fixedSizeMap = new FixedSizeMap(5);

        System.out.println("Initial load of 5 items");
        for(int cnt = 1; cnt<=5; cnt++){        
            fixedSizeMap.put(cnt, cnt);
        }
        System.out.println(fixedSizeMap);

        System.out.println("Load 5 more items");
        for(int cnt = 6; cnt<=10; cnt++){        
            fixedSizeMap.put(cnt, cnt);
            System.out.println(fixedSizeMap);
        }
        
        System.out.println("Touch item key=6, and load 5 more items");
        Object tmp = fixedSizeMap.get(6);
        for(int cnt = 11; cnt<=15; cnt++){        
            fixedSizeMap.put(cnt, cnt);
            System.out.println(fixedSizeMap);
        }
    }
}
