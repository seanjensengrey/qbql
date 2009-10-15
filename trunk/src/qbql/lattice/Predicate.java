package qbql.lattice;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import qbql.index.IndexedPredicate;
import qbql.util.Util;

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
        
        if( !(y instanceof IndexedPredicate) 
         && !(y instanceof ComplementPredicate) 
         && !(y instanceof EqualityPredicate) 
        ) {
            Predicate tmp = x;
            x = y;
            y = tmp;
        }
        
        if( x instanceof Relation && y instanceof IndexedPredicate ) 
            return IndexedPredicate.join((Relation)x,(IndexedPredicate)y);
        if( x instanceof IndexedPredicate && y instanceof IndexedPredicate ) 
            return IndexedPredicate.join((IndexedPredicate)x,(IndexedPredicate)y);
        if( x instanceof Relation && y instanceof ComplementPredicate ) 
            return ComplementPredicate.join((Relation)x,(ComplementPredicate)y);
        if( x instanceof Relation && y instanceof EqualityPredicate ) 
            return EqualityPredicate.join((Relation)x,(EqualityPredicate)y);
        
        throw new RuntimeException("Not implemented");
    }

    public static Predicate innerUnion( Predicate x, Predicate y ) throws Exception {
        if( x instanceof Relation && y instanceof Relation )
            return Relation.innerUnion((Relation)x, (Relation)y);
        
        if( !(y instanceof IndexedPredicate) ) {
            Predicate tmp = x;
            x = y;
            y = tmp;
        }
        
        if( x instanceof Relation && y instanceof IndexedPredicate ) 
            return IndexedPredicate.innerUnion((Relation)x,(IndexedPredicate)y);
        
        throw new RuntimeException("Not implemented");
    }

    /*
     * Set intersection join
     */
    public static Predicate setIX( Predicate x, Predicate y ) throws Exception {
        if( x instanceof EqualityPredicate && !(y instanceof EqualityPredicate) ) {
            Predicate tmp = x;
            x = y;
            y = tmp;
        }
        
        if( y instanceof EqualityPredicate ) 
            return EqualityPredicate.setIX(x,(EqualityPredicate)y);
        
        Set<String> headerXmY = new TreeSet<String>();
        headerXmY.addAll(x.header.keySet());
        headerXmY.removeAll(y.header.keySet());            
        Set<String> headerYmX = new TreeSet<String>();
        headerYmX.addAll(y.header.keySet());
        headerYmX.removeAll(x.header.keySet());
        Set<String> headerSymDiff = new TreeSet<String>();
        headerSymDiff.addAll(headerXmY);
        headerSymDiff.addAll(headerYmX);
        Relation hdr = new Relation(headerSymDiff.toArray(new String[0]));
        return Relation.innerUnion(hdr,join(x, y));
    }
    public static Predicate setEQ( Predicate x, Predicate y ) throws Exception {
        if( x instanceof IndexedPredicate && !(y instanceof IndexedPredicate) ) {
            Predicate tmp = x;
            x = y;
            y = tmp;
        }
        
        if( x instanceof Relation && y instanceof IndexedPredicate ) 
            return IndexedPredicate.setEQ((Relation)x,(IndexedPredicate)y);
        
        throw new Exception("x.className="+x.getClass().getSimpleName()+",y.className="+y.getClass().getSimpleName());
    }

    public String toString() {
        return toString(0, false);
    }
    public String toString( int ident, boolean dummy2 ) {
        StringBuffer ret = new StringBuffer("");
        ret.append("[");
        for( int i = 0; i < colNames.length; i++ )
            ret.append((i>0?"  ":"")+colNames[i]);
        ret.append("]\n");
        ret.append(Util.identln(ident," "));
        ret.append("...\n");
        return ret.toString();
    }
}
