package qbql.lattice;

import java.util.HashMap;
import java.util.Map;

import qbql.index.IndexedPredicate;

public class Predicate {
    public HashMap<String,Integer> header = new HashMap<String,Integer>();
    public String[] colNames;
    public Predicate( String[] columns ) {
        colNames = columns;
        for( int i = 0; i < columns.length; i++ ) {
            header.put(colNames[i],i);
        }               
    }
    public Predicate() {}
    
    public static Predicate join( Predicate x, Predicate y ) throws Exception {
        if( x instanceof Relation && y instanceof Relation )
            return Relation.join((Relation)x, (Relation)y);
        
        if( !(y instanceof IndexedPredicate) ) {
            Predicate tmp = x;
            x = y;
            y = tmp;
        }
        
        if( x instanceof Relation && y instanceof IndexedPredicate ) 
            return IndexedPredicate.join((Relation)x,(IndexedPredicate)y);
        if( x instanceof IndexedPredicate && y instanceof IndexedPredicate ) 
            return IndexedPredicate.join((IndexedPredicate)x,(IndexedPredicate)y);
        
        throw new RuntimeException("Not implemented");
    }

    /*
     * Set intersection join
     */
    public static Predicate setIX( Predicate x, Predicate y ) throws Exception {
        if( x instanceof Relation && y instanceof Relation )
            return Relation.setIX((Relation)x, (Relation)y);
        
        if( !(x instanceof Relation) && !(x instanceof EqualityPredicate) && !(x instanceof IndexedPredicate)) {
            Predicate tmp = x;
            x = y;
            y = tmp;
        }
        if( (!(x instanceof Relation) && !(x instanceof EqualityPredicate) && !(x instanceof IndexedPredicate)) 
          || !(y instanceof EqualityPredicate) ) 
            throw new RuntimeException("Not implemented");
        
        return EqualityPredicate.setIX(x,(EqualityPredicate)y);
    }

    public String toString() {
        return toString(0, false);
    }
    public String toString( int dummy, boolean dummy2 ) {
        StringBuffer ret = new StringBuffer("");
        ret.append("[");
        for( int i = 0; i < colNames.length; i++ )
            ret.append((i>0?"  ":"")+colNames[i]);
        ret.append("]\n");
        return ret.toString();
    }
}
