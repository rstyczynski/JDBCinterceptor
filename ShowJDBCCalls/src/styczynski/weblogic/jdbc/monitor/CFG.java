package styczynski.weblogic.jdbc.monitor;

public class CFG {
    
    static long sqlMaxExecutionTime = 1000;    //SQL execution time threshold
    static int topAlertsToStore = 50;          //Number of alerts to keep per thread
    static int topHistogramsToStore = 5;       //Number of histograms to keep per thread
    static int histogramSlots = 100;
    static int histogramMax = 10*1000;
    
    static boolean printHeadersAlways = true;
    static boolean debugNormal = true;
    static boolean debugDetailed = true;

    public static int getTopHistogramsToStore() {
        return topHistogramsToStore;
    }

    public static int getHistogramSlots() {
        return histogramSlots;
    }

    public static int getHistogramMax() {
        return histogramMax;
    }

    public static void setTopAlertsToStore(int topAlertsToStore) {
        CFG.topAlertsToStore = topAlertsToStore;
    }

    public static int getTopAlertsToStore() {
        return CFG.topAlertsToStore;
    }

    public static void setPrintHeadersAlways(boolean printHeadersAlways) {
        CFG.printHeadersAlways = printHeadersAlways;
    }

    public static boolean isPrintHeadersAlways() {
        return CFG.printHeadersAlways;
    }

    public static void setDebugNormal(boolean debugNormal) {
        CFG.debugNormal = debugNormal;
    }

    public static boolean isDebugNormal() {
        return CFG.debugNormal;
    }

    public static void setDebugDetailed(boolean debugDetailed) {
        CFG.debugDetailed = debugDetailed;
    }

    public static boolean isDebugDetailed() {
        return CFG.debugDetailed;
    }
}
