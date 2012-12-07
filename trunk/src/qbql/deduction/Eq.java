package qbql.deduction;

import java.util.LinkedList;
import java.util.List;

import qbql.symbolic.Expr;


public class Eq {
    
    List<Expr> expressions = new LinkedList<Expr>();

    public Eq( List<Expr> expressions ) {
        this.expressions = expressions;
    }

    public Eq( Expr lft, Expr rgt ) {
        expressions.add(lft); 
        expressions.add(rgt); 
    }

    public Eq substitute( Eq src ) {
        Eq ret = new Eq(new LinkedList<Expr>());
        ret.expressions.addAll(expressions);
        for( Expr node : expressions ) {
            for( Expr tmp : node.substitute(src.expressions) )
                if( !contains(tmp) )
                    ret.expressions.add(tmp);
        }
        return ret;
    }

    private boolean contains( Expr src ) {
        for( Expr e : expressions )
            if( e.equals(src) )
                return true;
        return false;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        for( Expr node : expressions ) {
            if( 0 < ret.length() )
                ret.append(" = ");
            String tmp = node.toString();
            if( tmp.startsWith("(") )
                tmp = tmp.substring(1,tmp.length()-1);
            ret.append(tmp);
        }
        return ret.toString();
    }
    
    @Override
    public boolean equals( Object obj ) {
        Eq cmp = (Eq) obj;
        for( Expr t1 : expressions ) {            
            for( Expr t2 : expressions )
                if( t1.equals(t2) )
                    return true;
            return false;
        }
        throw new AssertionError("Empty equivalence class");
    }

    public static Eq merge( Eq src1, Eq src2 ) {
        List<Expr> tmp = new LinkedList<Expr>();
        tmp.addAll(src1.expressions);
        for( Expr t2 : src2.expressions ) {
            boolean matches = false;
            for( Expr t1 : src1.expressions )
                if( t1.equals(t2) ) {
                    matches = true;
                    break;
                }
            if( !matches )
                tmp.add(t2);
        }
        return new Eq(tmp);
    }
}
