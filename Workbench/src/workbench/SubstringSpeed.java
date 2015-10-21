package workbench;

import java.util.regex.Pattern;

//Source: http://stackoverflow.com/questions/18340097/what-is-the-fastest-substring-search-method-in-java

public class SubstringSpeed {
    public SubstringSpeed() {
        super();
    }

    public static void main(String[] args) throws Exception {
        
        String[] names = new String[]{"jack", "jackson", "jason", "dijafu"};
        long start = 0;
        long stop = 0;

        //Contains
        start = System.nanoTime();
        for (int i = 0; i < names.length; i++){
            names[i].contains("ja");
        }
        stop = System.nanoTime();
        System.out.println("Contains: " + (stop-start));

        //IndexOf
        start = System.nanoTime();
        for (int i = 0; i < names.length; i++){
            names[i].indexOf("ja");
        }
        stop = System.nanoTime();
        System.out.println("IndexOf: " + (stop-start));

        //Matches
        Pattern p = Pattern.compile("ja");
        start = System.nanoTime();
        for (int i = 0; i < names.length; i++){
            p.matcher(names[i]).matches();
            //names[i].matches("ja");
        }
        stop = System.nanoTime();
        System.out.println("Matches: " + (stop-start));
    }

}
