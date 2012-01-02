package qbql.lang;

import java.util.HashMap;
import java.util.Map;

import qbql.lattice.Database;
import qbql.lattice.Relation;

public class LE {
    public static String[] getSymbolicNames() {
    	return new String[] {
    		"lft <= rgt", 
    	    "rgt >= lft"
    	};
    }

    public static Relation lft_rgt( Object lft, Object rgt ) {
    	if( rgt.getClass().equals(lft.getClass()) ) {
    		if( lft instanceof Comparable ) {
    			Comparable l = (Comparable) lft;
    			Comparable r = (Comparable) rgt;
    			if( l.compareTo(r)<=0 )
    				return Database.R01;
    			else
    				return Database.R00;
    		}
    	}
    	if( !(lft instanceof Number) || !(rgt instanceof Number) )
    		throw new AssertionError("LE !lft/rgt instanceof Number");
    	Double l = ((Number)lft).doubleValue();
    	Double r = ((Number)rgt).doubleValue();
    	if( l.compareTo(r)<=0 )
    		return Database.R01;
    	else
    		return Database.R00;
    } 
    
}
