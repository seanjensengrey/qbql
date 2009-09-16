package qbql.induction;

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
    
    public static void main( String[] args ) throws Exception {
        String goal = "x*y = expr.";
        //String goal = "x` ^ x' = expr.";
        //String goal = "x = expr.";
        //String goal = "x < y -> x /> y = expr.";
        //String goal = "(x ^ (y v z)) ^ ((x ^ y) v (x ^ z))' = expr.";
        System.out.println("goal: "+goal);
        
        final String[] constants = new String[] {
            "R00",
            "R11",             
        };
        final String[] unaryOps = new String[] {
            "'",
            "`",
        };
        final String[] binaryOps = new String[] {
            "^",
            "v",             
            "*",
            "+",             
        };
        
        List<LexerToken> src =  new Lex().parse(goal);
        Matrix matrix = Grammar.cyk.initArray1(src);
        int size = matrix.size();
        TreeMap<Integer,Integer> skipRanges = new TreeMap<Integer,Integer>();
        Grammar.cyk.closure(matrix, 0, size+1, skipRanges, -1);
        ParseNode root = Grammar.cyk.forest(size, matrix);
        if( root.topLevel != null )
            throw new Exception("root.topLevel!=null" );     
        
        Grammar g = new Grammar(null,Util.readFile(Run.class,"Figure1.db"));
        Set<String> variables = g.variables(root);
        variables.remove("expr");
        final String[] zilliaryOps = new String[variables.size()+constants.length];
        for( int i = 0; i < variables.size(); i++ ) {
            zilliaryOps[i] = variables.toArray(new String[0])[i];
        }
        for( int i = 0; i < constants.length; i++ ) {
            zilliaryOps[i+variables.size()] = constants[i];
        }
        
        
    }
}
