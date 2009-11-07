package qbql.lang;

import java.util.HashMap;
import java.util.Map;

import qbql.lattice.Database;
import qbql.lattice.Relation;

public class LE {

    public static Relation lft_rgt( int lft, int rgt ) {
        if( lft <= rgt )
            return Database.R01;
        else
            return Database.R00;
    }  
}
