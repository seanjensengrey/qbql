package qbql.lang;

import java.util.HashMap;
import java.util.Map;

import qbql.lattice.Relation;

public class IntegerInterval {

    public static String[] getSymbolicNames() {
    	return new String[] {
        		"for(int i = from; i<to; i++)",
        		"from <= int i < to",
    	};
    }
    public static Relation from_to_i( int from, int to ) {
        Relation ret = new Relation(
            new String[]{"from","to","i"}
        );
        for( int j = from; j < to; j++ ) {
            Map<String,Object> content = new HashMap<String,Object>();
            content.put("from", from);
            content.put("to", to);
            content.put("i", j);
            ret.addTuple(content);
        }
        return ret;
    }  
}
