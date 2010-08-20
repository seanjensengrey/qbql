package qbql.lattice;

import java.util.Set;
import java.util.TreeSet;

public class ComplementPredicate extends Predicate {
	Predicate src = null;

    public ComplementPredicate( Predicate src ) {
        super(src.colNames);
        this.src = src;
    }
    
    public static Relation join( Relation x, ComplementPredicate y ) throws Exception {
        if( !x.header.keySet().containsAll(y.header.keySet()) )
            throw new AssertionError("Unsafe join with complement");
        Relation ret = new Relation(x.colNames);
        for( Tuple tupleX: x.content ) {
        	Relation tX = new Relation(x.colNames);
        	Object[] tXTuple = new Object[tupleX.data.length];
        	System.arraycopy(tupleX.data, 0, tXTuple, 0, tXTuple.length);
        	Tuple tCopy = new Tuple(tXTuple);
			tX.content.add(tCopy);
        	Predicate join = Predicate.join(tX, y.src);
        	if( !(join instanceof Relation) )
                throw new AssertionError("!(join instanceof Relation)");
        	if( ((Relation)join).content.size()==0 )
        		ret.content.add(tCopy);
        }
        return ret;
    }
    
}
