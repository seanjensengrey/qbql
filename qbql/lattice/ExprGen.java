package qbql.lattice;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    static ExprTree star = null;
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
            star = parse("(x * y)");
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
        Map<Long,List<ExprTree>> accumulated = new HashMap<Long,List<ExprTree>>();
        put(accumulated,parse("x"));
        
        List<Substitution> grafts = new LinkedList<Substitution>();
        //grafts.add(new Substitution("x",r00));
        //grafts.add(new Substitution("y",r00));
        //grafts.add(new Substitution("x",r11));
        //grafts.add(new Substitution("y",r11));
        grafts.add(new Substitution("x",join));
        grafts.add(new Substitution("y",join));
        grafts.add(new Substitution("x",union));
        grafts.add(new Substitution("y",union));
        grafts.add(new Substitution("x",complementX));
        grafts.add(new Substitution("y",complementY));
        grafts.add(new Substitution("x",inverseX));
        grafts.add(new Substitution("y",inverseY));
        grafts.add(new Substitution("x",star));
        grafts.add(new Substitution("y",star));
     
        
        //String goal = "((x)`)` = expr.";
        //String goal = "(x + (x * y))^(x)' = expr.";
        //String goal = "x*y = expr.";
        String goal = "x /\\ y = expr.";
        System.out.println("goal: "+goal);
        ExprTree XeqExpr = parse(goal);
        
        Set<String> found = new HashSet<String>();
        
        Database model = new Database();
        int exprPos = XeqExpr.src.size()-2;    
        final long startTime = System.currentTimeMillis();
        long evalTime = 0;
       
        for( long i = 0; i < 1000000; i++) {            
            if( i%5001==5000 ) {
                System.out.println("i="+i);
                System.out.println("Eval time = "+evalTime); 
                System.out.println("Other time = "+(System.currentTimeMillis()-startTime-evalTime)); 
            } else if( i%100==0 )
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
                    //final long t2 = System.currentTimeMillis();
                    if( !addNotExistent(accumulated, grown) )
                        continue;
                    //evalTime += System.currentTimeMillis()-t2;
                    
                    // debug
                    if( "^vyR00^xvxR00^y".equals(grown.root.signature(grown.src)) ) {
                        String expr = LexerToken.toString(grown.src, 0, grown.src.size());
                        System.out.println(">>>>>>>>> "+expr);  
                    }
                    //LexerToken.print(grown.src, 0, grown.src.size());
                    if( ignore(graft,current,varPos) ) {
                        continue;
                    }
                                            
                    ExprTree identity = XeqExpr.grow(exprPos, grown);
                    final long t2 = System.currentTimeMillis();
                    ParseNode eval = model.assertion(identity.root, identity.src, false);
                    evalTime += System.currentTimeMillis()-t2;
                    if( eval != null )
                        continue;

                    String sig = grown.root.signature(grown.src);
                    if( !found.contains(sig) ) {
                        System.out.println("*** found *** ");
                        LexerToken.print(grown.src, 0, grown.src.size());
                        found.add(sig);
                    }
                    return;
                }
            del(accumulated,current);
        }
    }

    private static void put( Map<Long, List<ExprTree>> accumulated, ExprTree tree ) {
        long hash = tree.root.hash(tree.src);
        List<ExprTree> l = accumulated.get(hash);
        if( l == null ) {
            l = new LinkedList<ExprTree>();
            accumulated.put(hash, l);
        }
        l.add(tree);
    }
    private static void del( Map<Long, List<ExprTree>> accumulated, ExprTree tree ) {
        long hash = tree.root.hash(tree.src);
        List<ExprTree> l = accumulated.get(hash);
        l.remove(tree);
        if( l.size() == 0 ) 
            accumulated.remove(hash);
    }

    private static boolean addNotExistent( Map<Long,List<ExprTree>> accumulated, ExprTree grown ) {
        long index = grown.root.hash(grown.src);
        List<ExprTree> l = accumulated.get(index);
        if( l == null )
            l = new LinkedList<ExprTree>();
        for( ExprTree t : l ) {
            if( t.root.signature(t.src).equals(grown.root.signature(grown.src)) )
                return false;
            if( t.root.signature(t.src).equals(grown.signature1()) )
                return false;
        }
        put(accumulated,grown);
        return true;
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
            ParseNode parent = current.root.parent(pos, pos+1);
            if( parent == null || parent.contains(Database.expr) )
                return false;
            ParseNode grandparent = current.root.parent(parent.from, parent.to);
            if( grandparent == null )
                return false;
            if( (grandparent.contains(Database.inverse)) ) {
                if( parent.from+3!=parent.to )
                    throw new RuntimeException("parent.from+3!=parent.to");
                //String var = current.src.get(pos).content;
                //if( "x".equals(var) || "y".equals(var) )
                return true;
            }
        }
        if( r00 == graft || r11 == graft ) {
            ParseNode parent = current.root.parent(pos, pos+1);
            if( parent == null )
                return false;
            if( (parent.contains(Database.join) || parent.contains(Database.innerUnion)|| parent.contains(Database.innerJoin)) 
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
    
    private static ExprTree smallest( Map<Long,List<ExprTree>> accumulated ) {
        long index = Long.MAX_VALUE;
        for( long candidate : accumulated.keySet() )
            if( candidate < index)
                index = candidate;
        return accumulated.get(index).get(0);
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
  
    private String signature1 = null;
    public String signature1() {
        if( signature1 == null ) {
            signature1 = root.signature(src);
            signature1 = signature1.replace('x', '$');
            signature1 = signature1.replace('y', 'x');
            signature1 = signature1.replace('$', 'y');
        }
        return signature1;
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