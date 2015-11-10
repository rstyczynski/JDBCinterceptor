package styczynski.weblogic.jdbc.monitor;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CFG {
    
    static private Log log = LogFactory.getLog("styczynski.weblogic.jdbc.monitor.CFG");
    
    static Properties props = new Properties(); //used to load/store configuration
    static final String propsFile="config/jdbcMonitor.properties"; //expect file file in domain config directory. Unchangeabnle
    
    //technical properties
    static boolean cfgReadOnly = false; //by default config file is writeable. May be disabled by setting true value in cfg file itself

    //logic related properties    
    static int topAlertsToStore = 50;          //Number of alerts to keep per thread
    static int topHistogramsToStore = 50;      //Number of histograms to keep per thread
    static int sqlMaxExecutionTime = 1000;    //SQL execution time threshold in ms
                                               //   -> 1s
    static int histogramMax = 10*1000;         //Maximum value of the histogram 
                                               //   -> 10s.
    static int histogramSlots = 100;           //Number of histogram buckets. 
                                               //   -> X axis width and resolution. 
                                               //   -> now each bucket shows 100ms time slot

    static boolean printHeadersAlways = false; // how debug is printed
    static boolean debugNormal = false;        // low traffic debug
    static boolean debugDetailed = false;      // detaild debug


    public static void setCfgReadOnly(boolean cfgReadOnly) {
        CFG.cfgReadOnly = cfgReadOnly;
        props.setProperty("cfgReadOnly", String.valueOf(cfgReadOnly));
        store();
    }

    public boolean isCfgReadOnly() {
        return cfgReadOnly;
    }

    public static void setTopHistogramsToStore(int topHistogramsToStore) {
        CFG.topHistogramsToStore = topHistogramsToStore;
        props.setProperty("topHistogramsToStore", String.valueOf(topHistogramsToStore));
        store();
    }

    public static void setSqlMaxExecutionTime(int sqlMaxExecutionTime) {
        CFG.sqlMaxExecutionTime = sqlMaxExecutionTime;
        props.setProperty("sqlMaxExecutionTime", String.valueOf(sqlMaxExecutionTime));
        store();
    }

    public static int getSqlMaxExecutionTime() {
        return sqlMaxExecutionTime;
    }

    public static void setHistogramMax(int histogramMax) {
        CFG.histogramMax = histogramMax;
        props.setProperty("histogramMax", String.valueOf(histogramMax));
    }

    public static void setHistogramSlots(int histogramSlots) {
        CFG.histogramSlots = histogramSlots;
        props.setProperty("histogramSlots", String.valueOf(histogramSlots));
    }

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
        props.setProperty("topAlertsToStore", String.valueOf(topAlertsToStore));
        store();
    }

    public static int getTopAlertsToStore() {
        return CFG.topAlertsToStore;
    }

    public static void setPrintHeadersAlways(boolean printHeadersAlways) {
        CFG.printHeadersAlways = printHeadersAlways;
        props.setProperty("printHeadersAlways", String.valueOf(printHeadersAlways));
        store();
    }

    public static boolean isPrintHeadersAlways() {
        return CFG.printHeadersAlways;
    }

    public static void setDebugNormal(boolean debugNormal) {
        CFG.debugNormal = debugNormal;
        props.setProperty("debugNormal", String.valueOf(debugNormal));
        store();
    }

    public static boolean isDebugNormal() {
        return CFG.debugNormal;
    }

    public static void setDebugDetailed(boolean debugDetailed) {
        CFG.debugDetailed = debugDetailed;
        props.setProperty("debugDetailed", String.valueOf(debugDetailed));
        store();
    }

    public static boolean isDebugDetailed() {
        return CFG.debugDetailed;
    }
    
    public static boolean store() {
        boolean result = true;
        
        if (!cfgReadOnly) {
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(CFG.propsFile);
                props.store(out, "");
            } catch (Throwable e) {
                result = false;
                log.warn("Configuration cannot be stored. Reason:" + e);
            } finally {
                try {
                    if (out != null) out.close();
                    } catch (IOException e) { /* ignore*/ }
            }
        }        
        return result;
    }
    
    void init(){
        
        FileInputStream in = null;
        try {
            in = new FileInputStream(propsFile);
            props.load(in);
        } catch (Throwable e) {
            log.warn("Configuration cannot be loaded. Using defaults. Reason:" + e);
            // props will be empty
            // will be updated in case of using setXX
            // only explicity set data will be stored in file
        } finally {
            try {
                if (in != null) in.close();
            } catch (IOException e) { /* ignore*/ }
        }
        
        String propName = null;

        propName="topAlertsToStore";
        if (props.containsKey(propName))
            try { setTopAlertsToStore(Integer.valueOf(props.getProperty(propName))); } 
            catch (NumberFormatException nfe) { log.warn("Error reading property:" + propName + "=" + props.getProperty(propName));}

        propName="topHistogramsToStore";
        if (props.containsKey(propName))
            try { setTopHistogramsToStore(Integer.valueOf(props.getProperty(propName))); } 
            catch (NumberFormatException nfe) { log.warn("Error reading property:" + propName + "=" + props.getProperty(propName));}

        propName="sqlMaxExecutionTime";
        if (props.containsKey(propName))
            try { setSqlMaxExecutionTime(Integer.valueOf(props.getProperty(propName))); } 
            catch (NumberFormatException nfe) { log.warn("Error reading property:" + propName + "=" + props.getProperty(propName));}

        propName="histogramMax";
        if (props.containsKey(propName))
            try { setHistogramMax(Integer.valueOf(props.getProperty(propName))); } 
            catch (NumberFormatException nfe) { log.warn("Error reading property:" + propName + "=" + props.getProperty(propName));}

        propName="histogramSlots";
        if (props.containsKey(propName))
            try { setHistogramSlots(Integer.valueOf(props.getProperty(propName))); } 
            catch (NumberFormatException nfe) { log.warn("Error reading property:" + propName + "=" + props.getProperty(propName));}

        propName="cfgReadOnly";
        if (props.containsKey(propName))
            try { setCfgReadOnly(Boolean.valueOf(props.getProperty(propName))); } 
            catch (NumberFormatException nfe) { log.warn("Error reading property:" + propName + "=" + props.getProperty(propName));}

        propName="printHeadersAlways";
        if (props.containsKey(propName))
            try { setPrintHeadersAlways(Boolean.valueOf(props.getProperty(propName))); } 
            catch (NumberFormatException nfe) { log.warn("Error reading property:" + propName + "=" + props.getProperty(propName));}

        propName="debugNormal";
        if (props.containsKey(propName))
            try { setDebugNormal(Boolean.valueOf(props.getProperty(propName))); } 
            catch (NumberFormatException nfe) { log.warn("Error reading property:" + propName + "=" + props.getProperty(propName));}

        propName="debugDetailed";
        if (props.containsKey(propName))
            try { setDebugDetailed(Boolean.valueOf(props.getProperty(propName))); } 
            catch (NumberFormatException nfe) { log.warn("Error reading property:" + propName + "=" + props.getProperty(propName));}


    }
    
}
