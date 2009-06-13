package qbql.lattice;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import qbql.parser.CYK;
import qbql.parser.LexerToken;
import qbql.parser.Matrix;
import qbql.parser.ParseNode;
import qbql.parser.RuleTuple;
import qbql.util.Util;

public class ExprGen {
    static ExprTree join = null;
    static ExprTree union = null;
    static ExprTree complementX = null;
    static ExprTree complementY = null;
    static ExprTree inverseX = null;
    static ExprTree inverseY = null;
    static ExprTree r00 = null;
    static ExprTree r11 = null;
    static {
        try {
            join = parse("(x ^ y)");
            union = parse("(x v y)");
            complementX = parse("(x)'");
            complementY = parse("(y)'");
            inverseX = parse("(x)`");
            inverseY = parse("(y)`");
            r00 = parse("R00");
            r11 = parse("R11");
        } catch (Exception e) {
            System.exit(1);
        }
    }

    public static void main( String[] args ) throws Exception {
        List<ExprTree> accumulated = new LinkedList<ExprTree>();
        accumulated.add(parse("x"));
        
        List<Substitution> grafts = new LinkedList<Substitution>();
        grafts.add(new Substitution("x",r00));
        grafts.add(new Substitution("y",r00));
        grafts.add(new Substitution("x",r11));
        grafts.add(new Substitution("y",r11));
        grafts.add(new Substitution("x",join));
        grafts.add(new Substitution("y",join));
        grafts.add(new Substitution("x",union));
        grafts.add(new Substitution("y",union));
        grafts.add(new Substitution("x",complementX));
        grafts.add(new Substitution("y",complementY));
        //grafts.add(new Substitution("x",inverseX));
        //grafts.add(new Substitution("y",inverseY));
     
        Database model = new Database();
        ExprTree XeqExpr = parse("((x)`)` = expr.");
        int exprPos = 8; //-------0123456-7-^^^^
        //ExprTree XeqExpr = parse("x /\\ y = expr.");
        //int exprPos = 5; //-----------------^^^^
        
        final long t1 = System.currentTimeMillis();
        for( long i = 0; i < 100000; i++) {            
            if( i%5000==0 )
                System.out.println("i="+i);
            else if( i%100==0 )
                System.out.print('.');
            
            ExprTree current = smallest(accumulated);    
            for( Integer varPos : current.getVariables() )
                for( Substitution s : grafts ) {
                    if( !s.var.equals(current.src.get(varPos).content) )
                        continue;
                    ExprTree graft = s.by;
                    if( isRedundant(graft,current,varPos) )
                        continue;
                    ExprTree grown = current.grow(varPos, graft);
                    accumulated.add(grown);
                    // debug
                    //if( isRedundant(graft,current,varPos) )
                        //LexerToken.print(grown.src, 0, grown.src.size());
                    String expr = LexerToken.toString(grown.src, 0, grown.src.size());
                    if( expr.contains("( x ^ R11 ) v ( x ^ R00 )") )
                        System.out.println("!!!!!!!!!   !!!!!!!!   !!!!!!!!!");                     
                    if( expr.contains("( x ^ ( y ) ' ) v ( x ^ ( y ) ` )") )
                        System.out.println("!!!!!!!!!  *******  !!!!!!!! ******* !!!!!!!!!");                     
                        
                    ExprTree identity = XeqExpr.grow(exprPos, grown);
                    if( model.assertion(identity.root, identity.src, false) != null )
                        continue;
                    //long t2 = System.currentTimeMillis();
                    //if( expr.contains("x") && expr.contains("y") && expr.contains("`") )
                    System.out.println("*** found *** "+expr);
                    //}
                }
            accumulated.remove(current);
        }
        long t2 = System.currentTimeMillis();
        System.out.println("Time = "+(t2-t1)); 
    }

    private static boolean ignore( ExprTree graft, ExprTree current, int pos ) {
        if( join == graft || union == graft ) {
            ParseNode parent = current.root.parent(pos, pos+1);
            if( parent == null )
                return false;
            if( (parent.contains(Database.join) || parent.contains(Database.innerUnion)) 
                    && parent.from+3==parent.to ) {
                if( parent.from==pos ) {
                    String var = current.src.get(parent.to-1).content;
                    if( "x".equals(var) || "y".equals(var) )
                        return true;
                }
                if( parent.to-1==pos ) {
                    String var = current.src.get(parent.from).content;
                    if( "x".equals(var) || "y".equals(var) )
                        return true;
                }
            }
        }
        return false;
    }
    private static boolean isRedundant( ExprTree graft, ExprTree current, int pos ) {
        if( complementX == graft || complementY == graft ) {
            if( current.src.get(pos).content.equals("R00") 
                || current.src.get(pos).content.equals("R11") )
                return true;
            ParseNode parent = current.root.parent(pos, pos+1);
            if( parent == null || parent.contains(Database.expr) )
                return false;
            ParseNode grandparent = current.root.parent(parent.from, parent.to);
            if( grandparent == null )
                return false;
            if( (grandparent.contains(Database.complement)) ) {
                if( parent.from+3!=parent.to )
                    throw new RuntimeException("parent.from+3!=parent.to");
                //String var = current.src.get(pos).content;
                //if( "x".equals(var) || "y".equals(var) )
                    return true;
            }
        }
        if( inverseX == graft || inverseY == graft ) {
            if( current.src.get(pos).content.equals("R00") 
                || current.src.get(pos).content.equals("R11") )
                return true;
        }
        if( r00 == graft || r11 == graft ) {
            ParseNode parent = current.root.parent(pos, pos+1);
            if( parent == null )
                return false;
            if( (parent.contains(Database.join) || parent.contains(Database.innerUnion)) 
              && parent.from+3==parent.to ) {
                if( parent.from==pos ) {
                    String var = current.src.get(parent.to-1).content;
                    if( "R00".equals(var) || "R11".equals(var) )
                        return true;
                }
                if( parent.to-1==pos ) {
                    String var = current.src.get(parent.from).content;
                    if( "R00".equals(var) || "R11".equals(var) )
                        return true;
                }
            }
            ParseNode grandparent = current.root.parent(parent.from, parent.to);
            if( grandparent == null )
                return false;
            if( (grandparent.contains(Database.complement)) || (grandparent.contains(Database.inverse)) ) {
                if( parent.from+3!=parent.to )
                    throw new RuntimeException("parent.from+3!=parent.to");
                //String var = current.src.get(pos).content;
                //if( "x".equals(var) || "y".equals(var) )
                    return true;
            }
        }
        return false;
    }

    private static ExprTree parse( String input ) throws Exception {
        List<LexerToken> src =  LexerToken.parse(input);
        Matrix matrix = Database.cyk.initArray1(src);
        int size = matrix.size();
        TreeMap<Integer,Integer> skipRanges = new TreeMap<Integer,Integer>();
        Database.cyk.closure(matrix, 0, size+1, skipRanges, -1);
        ParseNode root = Database.cyk.forest(size, matrix);
        if( root.topLevel != null )
            throw new Exception("root.topLevel!=null" );
        return new ExprTree(root,src);
    }
    
    private static ExprTree smallest( List<ExprTree> exprs ) {
        ExprTree ret = null;
        for( ExprTree et : exprs )
            if( ret == null || et.src.size() < ret.src.size() )
                ret = et;
        return ret;
    }

}

class ExprTree {
    ParseNode root;
    List<LexerToken> src;
    
    public ExprTree( ParseNode root, List<LexerToken> src ) {
        if( src.size() != root.to )
            throw new RuntimeException("src.size() != root.to");
        this.root = root;
        this.src = src;
    }
    
    private List<Integer> vars = null;
    List<Integer> getVariables() {
        if( vars == null ) {
            vars = new LinkedList<Integer>();
            addChildren(vars, root, src);
        }        
        return vars;
    }    
    private static void addChildren( List<Integer> leaves, ParseNode node, List<LexerToken> src ) {
        if( node.from+1 == node.to &&
                (node.content().contains(Database.identifier)
                ||node.content().contains(Database.expr)
                ||node.content().contains(Database.attribute)) 
            ) {
            if( ! node.content(src).startsWith("R") )
                leaves.add(node.from);
        } else if( node.from+2 < node.to )
            for( ParseNode child: node.children() )
                addChildren(leaves, child, src);
    }

    ExprTree grow( int pos, ExprTree scion ) {
        return new ExprTree(
              root.graft(pos, scion.root), 
              graft(src, pos, scion.src)
        );
    }
    private static List<LexerToken> graft( List<LexerToken> src, int pos, List<LexerToken> scion ) {
        List<LexerToken> newSrc = new LinkedList<LexerToken>();
        int i = 0;
        for( LexerToken t: src ) {
            if( i == pos )
                newSrc.addAll(scion);
            else
                newSrc.add(t);
            i++;
        }
        return newSrc;
    }
  
    public String toString() {
        return LexerToken.toString(src, 0, src.size());
    }
}

class Substitution {
    String var;
    ExprTree by;
    public Substitution( String var, ExprTree by ) {
        super();
        this.var = var;
        this.by = by;
    }
}