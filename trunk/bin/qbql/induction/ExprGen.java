package qbql.induction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import qbql.lattice.Grammar;
import qbql.parser.Lex;
import qbql.parser.LexerToken;
import qbql.parser.Matrix;
import qbql.parser.ParseNode;
import qbql.program.Run;
import qbql.util.Util;

public class ExprGen {
    
    static String[] zilliaryOps;
    final static String[] unaryOps = new String[] {
            "'",
            //"`",
    };
    final static String[] binaryOps = new String[] {
            "^",
            "v",             
            "*",
            "+",
            //"/>",
            //"/<",
            //"/=",
            //"/0",
            //"/1",
            //"/!",
    };
    public static void main( String[] args ) throws Exception {
        //String goal = "(x + (y * z)) ^ (x ^ (y v z))' = expr.";
        //String goal = "(x ^ (y v z)) /< ((x ^ y) v (x ^ z)) = expr.";
        //String goal = "(x + y) ^ (x + (y * z)) = expr.";
        String goal = "y v ((x ^ y) v (x ^ z)) = expr.";
        System.out.println("goal: "+goal);
        final String subgoal = goal.substring(0,goal.indexOf("expr"));
        
        final String[] constants = new String[] {
            "R00",
            "R11",             
        };
        
        final Lex lex = new Lex();
        List<LexerToken> src =  lex.parse(goal);
        Matrix matrix = Grammar.cyk.initArray1(src);
        int size = matrix.size();
        TreeMap<Integer,Integer> skipRanges = new TreeMap<Integer,Integer>();
        Grammar.cyk.closure(matrix, 0, size+1, skipRanges, -1);
        ParseNode root = Grammar.cyk.forest(size, matrix);
        if( root.topLevel != null )
            throw new Exception("root.topLevel!=null" );     
        
        Grammar g = new Grammar(lex.parse(goal),Util.readFile(Run.class,"Figure1.db"));
        Set<String> variables = g.variables(root);
        variables.remove("expr");
        zilliaryOps = new String[variables.size()+constants.length];
        for( int i = 0; i < variables.size(); i++ ) {
            zilliaryOps[i] = variables.toArray(new String[0])[i];
        }
        for( int i = 0; i < constants.length; i++ ) {
            zilliaryOps[i+variables.size()] = constants[i];
        }
        
        ArrayList<TreeNode> l = new ArrayList<TreeNode>();
        l.add(Polish.leaf());
        l.add(TreeNode.one);
        
        //int cnt = 0; 
        final long startTime = System.currentTimeMillis();
        long evalTime = 0;
        for( Polish num = new Polish(l); ; num.next() ) {
            if( !num.wellBuilt() )
                continue;
            TreeNode n = num.decode(); 
            if( n != null ) {
                if( n.isRightSkewed() )
                    continue;
                //System.out.println();
                ExprGen.init(n);
                n.print();
                do {
                    if( n.isRightSkewed() )
                        continue;
                    if( n.isAbsorpIdemp() )
                        continue;
                    if( n.isDoubleComplement() )
                        continue;
                    //if( n.toString().contains("(y * x) v y") )
                        //n.print();
                                        
                    String input = subgoal + n.toString() +".";
                    src =  lex.parse(input);
                    matrix = Grammar.cyk.initArray1(src);
                    size = matrix.size();
                    skipRanges = new TreeMap<Integer,Integer>();
                    Grammar.cyk.closure(matrix, 0, size+1, skipRanges, -1);
                    root = Grammar.cyk.forest(size, matrix);
                    if( root.topLevel != null )
                        throw new Exception("root.topLevel!=null" );     
                    
                    final long t2 = System.currentTimeMillis();
                    
                    g.src = src;
                    ParseNode eval = g.assertion(root, false);
                    evalTime += System.currentTimeMillis()-t2;
                    if( eval != null )
                        continue;
                    System.out.println("*** found *** ");
                    System.out.println(input);
                    //System.out.println("Elapsed="+(System.currentTimeMillis()-startTime));
                    //System.out.println("evalTime="+evalTime);
                    return;
                } while( ExprGen.next(n) );
                //cnt++;
            } else {
                //System.out.print('.');
            }
        }
        //System.out.println(cnt);
        
    }
    
    static void init( TreeNode node ) {
        if( node.lft == null ) 
            node.label = zilliaryOps[0];
        else {
            init(node.lft);
            if( node.rgt == null )
                node.label = unaryOps[0];
            else {
                init(node.rgt);
                node.label = binaryOps[0];
            }
        }
    }
    static boolean next( TreeNode node ) {
        Boolean ok = false;
        if( node.lft != null ) {
            ok = next(node.lft);
            if( ok )
                return true;
        }
        if( node.rgt != null ) {
            init(node.lft);
            ok = next(node.rgt);
            if( ok )
                return true;
        }
            
        if( node.lft == null ) {
            int index = index(node.label,zilliaryOps)+1;
            if( index == zilliaryOps.length )
                return false;
            else {
                init(node);
                node.label = zilliaryOps[index];
                return true;
            }
        } else {
            if( node.rgt == null ) {
                int index = index(node.label,unaryOps)+1;
                if( index == unaryOps.length )
                    return false;
                else {
                    init(node);
                    node.label = unaryOps[index];
                    return true;
                }
            } else {
                int index = index(node.label,binaryOps)+1;
                if( index == binaryOps.length )
                    return false;
                else {
                    init(node);
                    node.label = binaryOps[index];
                    return true;
                }
            }
        }
    }
    private static int index( String s, String[] src ) {
        int ret = -1;
        for (int i = 0; i < src.length; i++) {
            if( s.equals(src[i]) )
                return i;
        }
        return ret;
    }

}
