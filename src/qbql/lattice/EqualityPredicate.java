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
    
    EqualityPredicate lft;
    EqualityPredicate rgt;
    public EqualityPredicate( EqualityPredicate lft, EqualityPredicate rgt ) {
        super(Util.union(lft.colNames,rgt.colNames));       
        this.lft = lft;
        this.rgt = rgt;
    }

    static Predicate setIX( Predicate x, EqualityPredicate y ) throws Exception {
        if( x instanceof Relation || x instanceof IndexedPredicate ) {
            if( y.colX != null ) {
                if( x instanceof Relation ) {
                    Relation ret = (Relation)Relation.join(x, Database.R01); // clone
                    if( x.header.containsKey(y.colX) )
                        ret.renameInPlace(y.colX, y.colY);
                    else if( x.header.containsKey(y.colY) )
                        ret.renameInPlace(y.colY, y.colX);
                    else
                        throw new Exception("Renaming columns are disjoint with target relation");
                    return ret;                    
                } else {
                    IndexedPredicate ret = new IndexedPredicate((IndexedPredicate)x);
                    if( x.header.containsKey(y.colX) )
                        ret.renameInPlace(y.colX, y.colY);
                    else if( x.header.containsKey(y.colY) )
                        ret.renameInPlace(y.colY, y.colX);
                    else
                        throw new Exception("Renaming columns are disjoint with target relation");
                    return ret;                    
                }
            } else {
                return setIX(setIX(x,y.lft),y.rgt);
            }
        } else if( x instanceof EqualityPredicate ) {
            return new EqualityPredicate((EqualityPredicate)x,y);
        }
        throw new Exception("Unexpected case");
    }
    
    static Predicate join( Relation x, EqualityPredicate y ) throws Exception {
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
