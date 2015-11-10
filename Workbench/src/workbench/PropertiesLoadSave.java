package workbench;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.InputStream;

import java.util.Properties;

public class PropertiesLoadSave {
    
    final String propsFile="/Users/rstyczynski/jdeveloper/mywork/JDBCinterceptor/Workbench/src/workbench/PropertiesLoadSave.properties";


    public void setProps(Properties props) {
        this.props = props;
    }
    
    boolean cfgReadOnly = true;
        
    long key1ok = 0;
    int key2ok = 0;
    boolean key3ok = false;

    long key1bad = 0;
    int key2bad = 0;
    boolean key3bad = true;
    
    Properties props = new Properties();
    
    public Properties getProperties(){
        return props;
    }
    
    public PropertiesLoadSave() {
        init();
    }
    
    boolean store() {
        boolean result = true;
        
        if (!cfgReadOnly) {
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(propsFile);
                props.store(out, "");
            } catch (Throwable e) {
                result = false;
                System.err.println("Writing exception:" + e);
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
            System.err.println("Reading exception:" + e);
        } finally {
            try {
                if (in != null) in.close();
            } catch (IOException e) { /* ignore*/ }
        }
        
        String propName = null;
        
        propName="key1ok";
        if (props.containsKey(propName))
            try { setKey1ok(Long.valueOf(props.getProperty(propName))); } 
            catch (NumberFormatException nfe) { System.out.println("Error reading property:" + propName + "=" + props.getProperty(propName));}

        propName="key2ok";
        if (props.containsKey(propName))
            try { setKey2ok(Integer.valueOf(props.getProperty(propName))); } 
            catch (NumberFormatException nfe) { System.out.println("Error reading property:" + propName + "=" + props.getProperty(propName));}

        propName="key3ok";
        if (props.containsKey(propName))
            try { setKey3ok(Boolean.valueOf(props.getProperty(propName))); } 
            catch (NumberFormatException nfe) { System.out.println("Error reading property:" + propName + "=" + props.getProperty(propName));}

        //wrong values
        propName="key1bad";
        if (props.containsKey(propName))
            try { setKey1bad(Long.valueOf(props.getProperty(propName))); } 
            catch (NumberFormatException nfe) { System.out.println("Error reading property:" + propName + "=" + props.getProperty(propName));}

        propName="key2bad";
        if (props.containsKey(propName))
            try { setKey2bad(Integer.valueOf(props.getProperty(propName))); } 
            catch (NumberFormatException nfe) { System.out.println("Error reading property:" + propName + "=" + props.getProperty(propName));}

        propName="key3bad";
        if (props.containsKey(propName))
            try { setKey3bad(Boolean.valueOf(props.getProperty(propName))); } 
            catch (NumberFormatException nfe) { System.out.println("Error reading property:" + propName + "=" + props.getProperty(propName));}

    }
    
    public void setKey1ok(long key1ok) {
        this.key1ok = key1ok;
        props.setProperty("key1ok", String.valueOf(key1ok));
    }

    public long getKey1ok() {
        return key1ok;
    }

    public void setKey2ok(int key2ok) {
        this.key2ok = key2ok;
        props.setProperty("key2ok", String.valueOf(key2ok));
    }

    public int getKey2ok() {
        return key2ok;
    }

    public void setKey3ok(boolean key3ok) {
        this.key3ok = key3ok;
        props.setProperty("key3ok", String.valueOf(key3ok));
    }

    public boolean isKey3ok() {
        return key3ok;
    }

    public void setKey1bad(long key1bad) {
        this.key1bad = key1bad;
        props.setProperty("key1bad", String.valueOf(key1bad));
    }

    public long getKey1bad() {
        return key1bad;
    }

    public void setKey2bad(int key2bad) {
        this.key2bad = key2bad;
        props.setProperty("key2bad", String.valueOf(key2bad));
    }

    public int getKey2bad() {
        return key2bad;
    }

    public void setKey3bad(boolean key3bad) {
        this.key3bad = key3bad;
        props.setProperty("key3bad", String.valueOf(key3bad));
    }

    public boolean isKey3bad() {
        return key3bad;
    }

    public static void main(String[] args) {
        PropertiesLoadSave properties = new PropertiesLoadSave();
        
        System.out.println("====================");      
        System.out.println(properties.getProperties());
        
        System.out.println("====================");
        System.out.println(properties.props);
        System.out.println(properties.key1ok);
        System.out.println(properties.key2ok);
        System.out.println(properties.key3ok);
        System.out.println("=====");    
        System.out.println(properties.key1bad);
        System.out.println(properties.key2bad);
        System.out.println(properties.key3bad);
        
        System.out.println("====================");        
        properties.props.setProperty("key1", "value");

        System.out.println("====================");
        properties.store();
        
    }
}
