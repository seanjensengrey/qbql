package qbql.index;

import qbql.lattice.Database;
import qbql.util.Util;

public class Cat {
    public static NamedTuple source_from_prefix_postfix( String source, int from ) {
        String[] columns = new String[]{"prefix","postfix"};
        Object[] data = new Object[]{source.substring(0,from),source.substring(from)};
        return new NamedTuple(columns,data);
    }
    
    public static void main( String[] args ) throws Exception {
        String prg = Util.readFile(Cat.class,"strings.prg");
        Database.run(prg, "");    
    }
}
