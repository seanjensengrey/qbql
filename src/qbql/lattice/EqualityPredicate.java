package qbql.lattice;

import java.util.Set;

import qbql.index.IndexedPredicate;
import qbql.util.Util;

public class EqualityPredicate extends Predicate {
    String colX = null;
    String colY;
    public EqualityPredicate( String colX, String colY ) {
        super(new String[]{colX,colY});
        this.colX = colX;
        this.colY = colY;
    }
    
    static Predicate setIX( Predicate x, EqualityPredicate y ) throws Exception  {
        if( x instanceof Relation ) {
            Relation ret = (Relation)Relation.join(x, Database.R01); // clone
            if( x.header.containsKey(y.colX) && !x.header.containsKey(y.colY) )
                ret.renameInPlace(y.colX, y.colY);
            else if( x.header.containsKey(y.colY) && !x.header.containsKey(y.colX) )
                ret.renameInPlace(y.colY, y.colX);
            else
                throw new AssertionError("Renaming columns misaligned with target relation");
            return ret;                           
        } else if( x instanceof IndexedPredicate ) {
            IndexedPredicate ret = new IndexedPredicate((IndexedPredicate)x);
            if( x.header.containsKey(y.colX) && !x.header.containsKey(y.colY) )
                ret.renameInPlace(y.colX, y.colY);
            else if( x.header.containsKey(y.colY) && !x.header.containsKey(y.colX)  )
                ret.renameInPlace(y.colY, y.colX);
            else
                throw new AssertionError("Renaming columns misaligned with target relation");
            return ret;                                
        } else if( x.lft != null ) {
            Predicate ret = x.clone();
            if( ret.header.containsKey(y.colX) && !x.header.containsKey(y.colY) ) {
                ret.renameInPlace(y.colX, y.colY);
            } else if( ret.header.containsKey(y.colY) && !x.header.containsKey(y.colX) )
                ret.renameInPlace(y.colY, y.colX);
            else
                throw new AssertionError("Renaming columns misaligned with target relation");
            return ret;
        }
        throw new AssertionError("Unexpected case");
    }
    
    static Predicate join( Predicate x, EqualityPredicate y )  {
        Predicate ret = x.clone();
        if( ret.header.containsKey(y.colX) && !x.header.containsKey(y.colY) ) {
            ret.eqInPlace(y.colX, y.colY);
        } else if( ret.header.containsKey(y.colY) && !x.header.containsKey(y.colX) )
            ret.eqInPlace(y.colY, y.colX);
        else
            throw new AssertionError("Renaming columns are disjoint with target relation");
        return ret;       
    }
    static Predicate join( Relation x, EqualityPredicate y )  {
        String colX = null;
        String colY = null;
        Set<String> hdrX = x.header.keySet();
        if( hdrX.contains(y.colX) && !hdrX.contains(y.colY) ) {
            colX = y.colX;
            colY = y.colY;
        } else if( hdrX.contains(y.colY) && !hdrX.contains(y.colX) ) {
            colX = y.colY;
            colY = y.colX;
        }
        if( colX == null || colY == null ) {
            if( hdrX.contains(y.colX) && hdrX.contains(y.colY) ) {
                colX = y.colX;
                colY = y.colY;
                String[] header = new String[x.colNames.length];
                System.arraycopy(x.colNames, 0, header, 0, x.colNames.length);
                Relation ret = new Relation(header);
                for( Tuple t : x.content ) 
                    if( t.data[x.header.get(colX)].equals(t.data[x.header.get(colY)]) ) {
                        Object[] o = new Object[x.colNames.length];
                        System.arraycopy(t.data, 0, o, 0, x.colNames.length);
                        ret.content.add(new Tuple(o));
                    }
                return ret;
            } 
            throw new AssertionError("Equality column doesn't match relation");
        }
        String[] header = new String[x.colNames.length+1];
        System.arraycopy(x.colNames, 0, header, 0, x.colNames.length);
        header[x.colNames.length] = colY;
        Relation ret = new Relation(header);
        for( Tuple t : x.content ) {
            Object[] o = new Object[x.colNames.length+1];
            System.arraycopy(t.data, 0, o, 0, x.colNames.length);
            o[x.colNames.length] = t.data[x.header.get(colX)];
            ret.content.add(new Tuple(o));
        }
        return ret;
    }

}
