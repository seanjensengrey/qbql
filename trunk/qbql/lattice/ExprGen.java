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
    static ExprTree r00 = null;
    static ExprTree r11 = null;
    static {
        try {
            join = parse("(x ^ y)");
            union = parse("(x v y)");
            complementX = parse("(x)'");
            complementY = parse("(y)'");
            r00 = parse("R00");
            r11 = parse("R11");
        } catch (Exception e) {
            System.exit(1);
        }
    }

    public static void main( String[] args ) throws Exception {
        List<ExprTree> accumulated = new LinkedList<ExprTree>();
        accumulated.add(parse("x"));
        
        List<ExprTree> grafts = new LinkedList<ExprTree>();
        grafts.add(r00);
        grafts.add(r11);
        //grafts.add(complementX);
        //grafts.add(complementY);
        grafts.add(join);
        grafts.add(union);
     
        Database model = new Database();
        ExprTree XeqExpr = parse("x = expr.");
        
        
        final long t1 = System.currentTimeMillis();
        for( long iter = 0; iter < 1000; iter++) {            
            //if( iter%100==0 )
                //System.out.print('.');
            
            ExprTree current = smallest(accumulated);    
            for( Integer varPos : current.getVariables() )
                for( ExprTree graft : grafts ) {
                    if( isRedundant(graft,current,varPos) )
                        continue;
                    ExprTree grown = current.grow(varPos, graft);
                    accumulated.add(grown);
                    // debug
                    //if( isRedundant(graft,current,varPos) )
                        //LexerToken.print(grown.src, 0, grown.src.size());
                    String expr = LexerToken.toString(grown.src, 0, grown.src.size());
                    //if( expr.contains("( x ^ R11 ) v ( x ^ R00 )") )
                        //LexerToken.print(grown.src, 0, grown.src.size());                        
                        
                    ExprTree identity = XeqExpr.grow(2, grown);
                    if( model.assertion(identity.root, identity.src, false) != null )
                        continue;
                    //if( expr.contains("( x ^ R11 ) v ( x ^ R00 )") ) {
                    if( !ignore(graft,current,varPos) ) {
                        long t2 = System.currentTimeMillis();
                        //System.out.println("*************************** = "+(t2-t1)); 
                        LexerToken.print(grown.src, 0, grown.src.size());
                    }
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
            ParseNode parent = current.root.parent(pos, pos+1);
            if( parent == null || parent.contains(Database.expr) )
                return false;
            ParseNode grandparent = current.root.parent(parent.from, parent.to);
            if( grandparent == null )
                return false;
            if( (grandparent.contains(Database.complement)) 
              //&& parent.from+3==parent.to 
            ) {
                //String var = current.src.get(pos).content;
                //if( "x".equals(var) || "y".equals(var) )
                    return true;
            }
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