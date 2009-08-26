package qbql.index;

import qbql.lattice.Database;
import qbql.util.Util;

public class Cat {
    public static String prefix_source_from( String source, int from ) {
        return source.substring(0,from);
    }
    public static String postfix_source_from( String source, int from ) {
        return source.substring(from);
    }
    
    public static void main( String[] args ) throws Exception {
        String prg = Util.readFile(Cat.class,"strings.prg");
        Database.run(prg, "");    
    }
}
