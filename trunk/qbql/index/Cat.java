package qbql.index;

import java.util.HashMap;
import java.util.Map;

import qbql.lattice.Database;
import qbql.lattice.Relation;
import qbql.util.Util;

public class Cat {
    public static NamedTuple source_from_prefix_postfix( String source, int from ) {
        String[] columns = new String[]{"prefix","postfix"};
        Object[] data = new Object[]{source.substring(0,from),source.substring(from)};
        return new NamedTuple(columns,data);
    }
    public static Relation source_from_prefix_postfix( String source ) {
        Relation ret = new Relation(new String[]{"from","prefix","postfix"});
        for( int i = 0; i <= source.length(); i++ ) {
            Map<String,Object> content = new HashMap<String,Object>();
            content.put("from", i);
            content.put("prefix", source.substring(0,i));
            content.put("postfix", source.substring(i));
            ret.addTuple(content);
        }
        return ret;
    }
    
    public static void main( String[] args ) throws Exception {
        String prg = Util.readFile(Cat.class,"strings.prg");
        Database.run(prg, "");    
    }
}
