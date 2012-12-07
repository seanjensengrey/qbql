package qbql.symbolic;

import java.util.List;

public interface Expr {
    Expr left();
    Expr right();
    String operation();
    //Expr substitute( String x, Expr treeNode );
    List<Expr> substitute( List<Expr> src );
}
