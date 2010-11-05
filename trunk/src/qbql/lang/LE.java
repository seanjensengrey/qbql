package qbql.lang;

import java.util.HashMap;
import java.util.Map;

import qbql.lattice.Database;
import qbql.lattice.Relation;

public class LE {
    public static String getSymbolicName() {
    	return "lft <= rgt";
    }

    public static Relation lft_rgt( Object lft, Object rgt ) {
    	if( !(lft instanceof Comparable) || !(rgt instanceof Comparable) )
    		throw new AssertionError("LE !lft/rgt instanceof Comparable");
    	Comparable l = (Comparable) lft;
    	Comparable r = (Comparable) rgt;
        if( l.compareTo(r)<=0 )
            return Database.R01;
        else
            return Database.R00;
    } 
    
}
