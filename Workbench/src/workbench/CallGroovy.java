package workbench;

import javax.script.*;

public class CallGroovy {

    public static void main(String[] args) throws Exception {
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("groovy");

        //static script - slowest (900) - compilation done each time
        String script1Txt = "(1..1000).sum()";
        Object result = null;
          
        long start = System.currentTimeMillis();
        int i=0;
        for(i=1; i<1000; i++) {  
            result = engine.eval(script1Txt);
        }
        System.out.println(result + ", avg.executiuon time:" + (System.currentTimeMillis()-start));
        
        //script with parameters - fastest (100). Interesting. Compilation is probably done in transparent way.
        String script2Txt = "(start..stop).sum()";
        
        Bindings bindings = engine.createBindings();
        bindings.put("start", 1);
        bindings.put("stop", 1000);
        
        start = System.currentTimeMillis();
        for(i=1; i<1000; i++) {  
            result = engine.eval(script1Txt);
        }
        System.out.println(result + ", avg.executiuon time:" + (System.currentTimeMillis()-start));
        
        //script with compilation - medium speed (150). 
        CompiledScript script = ((Compilable) engine).compile(script2Txt);
        bindings = script.getEngine().createBindings();
        bindings.put("start", 1);
        bindings.put("stop", 1000);
        
        start = System.currentTimeMillis();
        for(i=1; i<1000; i++) {  
            result = script.eval(bindings);
        }
        System.out.println(result + ", avg.executiuon time:" + (System.currentTimeMillis()-start));
        
        
    }
    
    
}
