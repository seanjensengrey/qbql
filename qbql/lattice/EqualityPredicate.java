package qbql.lattice;

public class EqualityPredicate extends Predicate {
    String colX = null;
    String colY;
    public EqualityPredicate( String colX, String colY ) {
        this.colX = colX;
        this.colY = colY;
    }
    
    EqualityPredicate lft;
    EqualityPredicate rgt;
    public EqualityPredicate( EqualityPredicate lft, EqualityPredicate rgt ) {
        super();
        this.lft = lft;
        this.rgt = rgt;
    }

    static Predicate setIX( Predicate x, EqualityPredicate y ) throws Exception {
        if( x instanceof Relation ) {
            if( y.colX != null ) {
                if( x.header.containsKey(y.colX) ) {
                    Relation ret = Relation.join(x, Database.R01); // clone
                    ret.renameInPlace(y.colX, y.colY);
                    return ret;
                }
                if( x.header.containsKey(y.colY) ) {
                    Relation ret = Relation.join(x, Database.R01); // clone
                    ret.renameInPlace(y.colY, y.colX);
                    return ret;
                }
                throw new Exception("Renaming columns are disjoint with target relation");
            } else {
                return setIX(setIX(x,y.lft),y.rgt);
            }
        } else if( x instanceof EqualityPredicate ) {
            return new EqualityPredicate((EqualityPredicate)x,y);
        }
        throw new Exception("Unexpected case");
    }  
}
