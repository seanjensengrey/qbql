package qbql.lattice;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import qbql.index.IndexedPredicate;
import qbql.util.Util;

public class Predicate implements Comparable {
    public HashMap<String,Integer> header = new HashMap<String,Integer>();
    public String[] colNames;
    
    public Predicate( String[] columns ) {
        colNames = columns;
        for( int i = 0; i < colNames.length; i++ ) {
            header.put(colNames[i],i);
        }               
    }
    public Predicate() {}
    
    // lazy evaluation
    Predicate lft = null;
    Predicate rgt = null;
    int oper;
    public Predicate( Predicate lft, Predicate rgt, int oper ) {
        if( oper == Program.naturalJoin )
            colNames = Util.union(lft.colNames,rgt.colNames);
        else if( oper == Program.setIX )
            colNames = Util.symmDiff(lft.colNames,rgt.colNames);
        else if( oper == Program.innerUnion )
            colNames = Util.intersect(lft.colNames,rgt.colNames);
        else
            throw new AssertionError("Lazy eval: not supported operation");
        
        for( int i = 0; i < colNames.length; i++ ) {
            header.put(colNames[i],i);
        }               
        this.lft = lft;
        this.rgt = rgt;
        this.oper = oper;
    }
    
    public void renameInPlace( String from, String to ) {
        if( from.equals(to) )
            throw new AssertionError("renameInPlace: from=to");
        
        int colFrom = header.get(from);
        Integer colTo = header.get(to);
        if( colTo == null ) {
            colNames[colFrom] = to;
            header.remove(from);
            header.put(to, colFrom);
        } else
            throw new AssertionError("Column collision");
        
        if( lft != null && lft.header.containsKey(from) )
            lft.renameInPlace(from, to);
        if( rgt != null && rgt.header.containsKey(from) )
            rgt.renameInPlace(from, to);
    }
    public void eqInPlace( String from, String to ) {
        if( from.equals(to) )
            throw new AssertionError("renameInPlace: from=to");
        
        Integer colTo = header.get(to);
        if( colTo == null ) {
            String[] newCols = new String[colNames.length+1];
            for( int i = 0; i < colNames.length; i++ ) {
                newCols[i] = colNames[i]; 
            }
            newCols[colNames.length]=to;
            header.put(to, colNames.length);
            colNames = newCols;
        } else
            throw new AssertionError("Column collision");
        
        if( lft != null && lft.header.containsKey(from) )
            lft.eqInPlace(from, to);
        if( rgt != null && rgt.header.containsKey(from) )
            rgt.eqInPlace(from, to);
    }
    
    public static Predicate join( Predicate x, Predicate y )  {
        if( x instanceof Relation && y instanceof Relation )
            return Relation.join((Relation)x, (Relation)y);
        
        if( x instanceof Relation && y.lft != null ) {
            if( y.oper == Program.naturalJoin ) {
                try {
                	Predicate test = join(x,y.lft);
                	if( test instanceof Relation )
                		return join(test,y.rgt);
                } catch( AssertionError e ) {}
                try {
                	Predicate test = join(x,y.rgt);
                	if( test instanceof Relation )
                		return join(test,y.lft);
                } catch( AssertionError e ) {}
            } else if( y.oper == Program.innerUnion ) {
                String[] xly = Util.intersect(x.colNames, y.lft.colNames);
                String[] xry = Util.intersect(x.colNames, y.rgt.colNames);
                Set<String> l = new HashSet<String>();
                for( String s : xly )
                    l.add(s);
                Set<String> r = new HashSet<String>();
                for( String s : xry )
                    r.add(s);
                if( l.equals(r) ) // SDC
                    return union(join(x,y.lft),join(x,y.rgt));
            } else if( y.oper == Program.setIX  ) {
                Relation hdr = new Relation(Util.symmDiff(y.lft.colNames, y.rgt.colNames));
            	return join(x,Relation.union(hdr,join(y.lft, y.rgt)));
            } else if( x == Database.R00 ) {
                return new Relation(y.colNames);
            } else
                throw new AssertionError("Unknown operation");
        } else if( y instanceof Relation && x.lft != null ) 
            return join(y,x);
        
        
        if( !(y instanceof IndexedPredicate) 
         && !(y instanceof ComplementPredicate) 
         && !(y instanceof EqualityPredicate) 
        ) {
            Predicate tmp = x;
            x = y;
            y = tmp;
        }
        
        if( x instanceof Relation && y instanceof IndexedPredicate ) 
            //try {
                return IndexedPredicate.join((Relation)x,(IndexedPredicate)y);
            //} catch( AssertionError e ) {
                //return new Predicate(x,y,Program.naturalJoin);
            //}           
        if( x instanceof Relation && y instanceof ComplementPredicate ) 
            return ComplementPredicate.join((Relation)x,(ComplementPredicate)y);
        if( x instanceof Relation && y instanceof EqualityPredicate ) 
            return EqualityPredicate.join((Relation)x,(EqualityPredicate)y);
        
        if( y instanceof EqualityPredicate ) 
            return EqualityPredicate.join(x,(EqualityPredicate)y);
        
        return new Predicate(x,y,Program.naturalJoin);
    }

    public static Predicate union( Predicate x, Predicate y )  {
        if( x instanceof Relation && y instanceof Relation )
            return Relation.union((Relation)x, (Relation)y);
        
        if( !(y instanceof IndexedPredicate) 
         && !(y instanceof InversePredicate)        
        ) {
            Predicate tmp = x;
            x = y;
            y = tmp;
        }
        
        if( x instanceof Relation && y instanceof InversePredicate ) 
            return InversePredicate.union((Relation)x,(InversePredicate)y);
        if( x instanceof Relation && y instanceof IndexedPredicate ) 
            return IndexedPredicate.union((Relation)x,(IndexedPredicate)y);
        
        return new Predicate(x,y,Program.innerUnion);
    }

    /*
     * Set intersection join
     */
    public static Predicate setIX( Predicate x, Predicate y )  {
        if( x instanceof EqualityPredicate && !(y instanceof EqualityPredicate) ) {
            Predicate tmp = x;
            x = y;
            y = tmp;
        }
        
        if( y instanceof EqualityPredicate ) 
            return EqualityPredicate.setIX(x,(EqualityPredicate)y);
        
        if( x instanceof Relation && y instanceof Relation ) {
            Relation hdr = new Relation(Util.symmDiff(x.colNames, y.colNames));
        	return Relation.union(hdr,join(x, y));
        }
        
        if( x instanceof Relation && y.lft != null ) {
        	//final String oper = Program.cyk.allSymbols[y.oper];
        	boolean notAll3Intersect = Util.intersect(Util.intersect(x.colNames, y.lft.colNames),y.rgt.colNames).length==0;
			if( y.oper == Program.setIX && notAll3Intersect ) {
                // conditional associativity: 
				// http://vadimtropashko.wordpress.com/relational-lattice/the-laws-of-renaming/
                try {
                	Predicate test = setIX(x,y.lft);
                	if( test instanceof Relation )
                		return setIX(test,y.rgt);
                	if( test.lft instanceof Relation &&
                	    Util.intersect(Util.intersect(y.rgt.colNames, test.lft.colNames),test.rgt.colNames).length==0
                	) {
                    	Predicate test2 = setIX(y.rgt,test.lft);
                    	if( test2 instanceof Relation )
                    		return setIX(test2,test.rgt);           		
                	}
                } catch( AssertionError e ) {}
                try {
                	Predicate test = setIX(x,y.rgt);
                	if( test instanceof Relation )
                		return setIX(test,y.lft);
                } catch( AssertionError e ) {}
			} else if( y.oper == Program.naturalJoin && notAll3Intersect ) {
            	String[] lftHdr = Util.symmDiff(x.colNames, Util.union(y.lft.colNames, y.rgt.colNames));
            	String[] rgtHdr = Util.union(Util.symmDiff(x.colNames, y.lft.colNames), Util.symmDiff(x.colNames, y.rgt.colNames));
            	if( Util.symmDiff(lftHdr,rgtHdr).length == 0 )
            		return join(setIX(x,y.lft),setIX(x,y.rgt));
            } else if( y.oper == Program.innerUnion ) {
                String[] xly = Util.intersect(x.colNames, y.lft.colNames);
                String[] xry = Util.intersect(x.colNames, y.rgt.colNames);
                Set<String> l = new HashSet<String>();
                for( String s : xly )
                    l.add(s);
                Set<String> r = new HashSet<String>();
                for( String s : xry )
                    r.add(s);
                if( l.equals(r) ) // SDC
                    return union(setIX(x,y.lft),setIX(x,y.rgt));
            } else if( x == Database.R00 ) {
                return new Relation(y.colNames);
            } else
                throw new AssertionError("Unknown operation");
        } else if( y instanceof Relation && x.lft != null ) 
            return setIX(y,x);
                        
        Predicate ret = null;
        try {
            Relation hdr = new Relation(Util.symmDiff(x.colNames, y.colNames));
        	ret = Relation.union(hdr,join(x, y));
        } catch( AssertionError e ) {        	
        }
        if( ret instanceof Relation )
        	return ret;
        
        return new Predicate(x,y,Program.setIX);

    }
    public static Predicate setEQ( Predicate x, Predicate y ) {
        if( x instanceof IndexedPredicate && !(y instanceof IndexedPredicate) ) {
            Predicate tmp = x;
            x = y;
            y = tmp;
        }
        
        if( x instanceof Relation && y instanceof IndexedPredicate ) 
            return IndexedPredicate.setEQ((Relation)x,(IndexedPredicate)y);
        
        throw new AssertionError("x.className="+x.getClass().getSimpleName()+",y.className="+y.getClass().getSimpleName());
    }
    
    @Override
	public boolean equals( Object obj ) {
        if( this == obj )
            return true;
        Predicate src = (Predicate) obj;
        if( colNames.length != src.colNames.length )
            return false;
        
        for( String colName : header.keySet() ) {
            Integer j = src.header.get(colName);
            if( j == null )
                return false;
        }
        
        if( src instanceof Predicate || this instanceof Predicate ) 
        	if( getClass() != src.getClass() )
        		throw new AssertionError("Can't compare predicate content"); 
        	
        return true;
	}
	/**
     * @param x
     * @param y
     * @return x < y (i.e. x ^ y = x)
     * @throws Exception 
     */
    public static boolean le( Predicate x, Predicate y )  {
        return x.equals(join(x,y));
    }
    public static boolean ge( Predicate x, Predicate y )  {
        return y.equals(join(x,y));
    }
    public int compareTo( Object o ) {
		if( this.equals(o) )
			return 0;
    	if( !(o instanceof Predicate) )
    		throw new AssertionError("! o instanceof Predicate");
		if( le(this,(Predicate)o) )
			return -1;
		return 1;
	}


    public String toString() {
        return toString(0);
    }
    public String toString( int ident ) {
        StringBuffer ret = new StringBuffer("");
        ret.append("[");
        for( int i = 0; i < colNames.length; i++ )
            ret.append((i>0?"  ":"")+colNames[i]);
        ret.append("]\n");
        ret.append(Util.identln(ident," "));
        ret.append("...\n");
        return ret.toString();
    }
    
    protected Predicate clone() {
        Predicate ret = new Predicate(colNames.clone());
        if( lft != null )
            ret.lft = lft.clone();
        if( rgt != null )
            ret.rgt = rgt.clone();
        ret.oper = oper;
        return ret;
    }


}