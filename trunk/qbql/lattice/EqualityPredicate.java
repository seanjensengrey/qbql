package qbql.lattice;

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
}
