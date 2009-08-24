package qbql.lattice;

import java.util.HashMap;
import java.util.Map;

import qbql.index.IndexedPredicate;

public class Predicate {
    public Map<String,Integer> header = new HashMap<String,Integer>();
    public String[] colNames;
    
    public static Relation join( Predicate x, Predicate y ) throws Exception {
        if( x instanceof Relation && y instanceof Relation )
            return Relation.join((Relation)x, (Relation)y);
        
        if( !(x instanceof Relation) ) {
            Predicate tmp = x;
            x = y;
            y = tmp;
        }
        if( !(x instanceof Relation) || !(y instanceof IndexedPredicate) ) 
            throw new RuntimeException("Not implemented");
        
        return IndexedPredicate.join((Relation)x,(IndexedPredicate)y);
    }

}