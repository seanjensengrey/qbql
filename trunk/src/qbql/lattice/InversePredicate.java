package qbql.lattice;

import java.util.HashSet;
import java.util.Set;

public class InversePredicate extends Predicate {
	Predicate src = null;

    public InversePredicate( Predicate src ) {
        super(src.colNames);
        this.src = src;
    }
    
    public static Relation union( Relation x, InversePredicate y )  {
        if( !(y.src instanceof Relation ))
            throw new AssertionError("!(y.src instanceof Relation )");
        if( ((Relation)y.src).content.size() != 0 )
            throw new AssertionError("((Relation)y.src).content.size() != 0");
    	
    	Set<String> header = new HashSet<String>();
    	header.addAll(x.header.keySet());
    	header.removeAll(y.header.keySet());
    	Relation tmp = new Relation(header.toArray(new String[0]));
    	return Relation.union(x, tmp);
    }
    
}
