package qbql.lang;

import java.util.HashMap;
import java.util.Map;

import qbql.lattice.Relation;

public class IntegersLT {

    public static String[] getSymbolicNames() {
    	return new String[] {
        		"for(int i = 0; i<bound; i++)",
        		"int i < bound",
    	};
    }
    public static Relation bound_i( int bound ) {
        Relation ret = new Relation(
            new String[]{"bound","i"}
        );
        final int limit = bound;//Integer.parseInt(bound);
        for( int j = 0; j < limit; j++ ) {
            Map<String,Object> content = new HashMap<String,Object>();
            content.put("bound", bound);
            content.put("i", j);
            ret.addTuple(content);
        }
        return ret;
    }  
}
