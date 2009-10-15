package qbql.lattice;

import java.util.Set;
import java.util.TreeSet;

public class ComplementPredicate extends Predicate {
    Relation src = null;

    public ComplementPredicate( Relation src ) {
        super(src.colNames);
        this.src = src;
    }
    
    public static Relation join( Relation x, ComplementPredicate y ) {
        if( !x.header.keySet().containsAll(y.header.keySet()) )
            throw new AssertionError("Unsafe join with complement");
        Relation ret = new Relation(x.colNames);
        for( Tuple tupleX: x.content )
            for( Tuple tupleY: y.src.content ) {                            
                Object[] retTuple = new Object[ret.header.size()];
                for( String attr : ret.colNames ) {
                    Integer yAttr = y.header.get(attr);
                    if( yAttr == null )
                        retTuple[ret.header.get(attr)] = tupleX.data[x.header.get(attr)];
                    else {
                        if( tupleY.data[y.header.get(attr)].equals(tupleX.data[x.header.get(attr)]) ) {
                            retTuple = null;
                            break;
                        } else
                            retTuple[ret.header.get(attr)] = tupleX.data[x.header.get(attr)];
                    }
                }
                if( retTuple != null )
                    ret.content.add(new Tuple(retTuple));
            }
        return ret;
    }

}
