package qbql.index;

import java.util.List;
import java.util.TreeMap;

import qbql.lattice.Grammar;
import qbql.parser.CYK;
import qbql.parser.LexerToken;
import qbql.parser.Matrix;
import qbql.parser.ParseNode;
import qbql.util.Util;

public class Cat {
    public static String prefix_source_from( String source, int from ) {
        return source.substring(0,from);
    }
    public static String postfix_source_from( String source, int from ) {
        return source.substring(from);
    }
    
    public static void main( String[] args ) throws Exception {
        String prg = Util.readFile(Cat.class,"strings.prg");

        List<LexerToken> src =  LexerToken.parse(prg);
        Matrix matrix = Grammar.cyk.initArray1(src);
        int size = matrix.size();
        TreeMap<Integer,Integer> skipRanges = new TreeMap<Integer,Integer>();
        Grammar.cyk.closure(matrix, 0, size+1, skipRanges, -1);
        ParseNode root = Grammar.cyk.forest(size, matrix);

        if( root.topLevel != null ) {
            System.out.println("*** Parse Error in assertions file ***");
            CYK.printErrors(prg, src, root);
            return;
        }

        Grammar program = new Grammar(src,"");
        long t1 = System.currentTimeMillis();
        ParseNode exception = program.program(root);
        long t2 = System.currentTimeMillis();
        System.out.println("Time = "+(t2-t1)); 
        if( exception != null ) {
            System.out.println("*** False Assertion ***");
            System.out.println(prg.substring(src.get(exception.from).begin, src.get(exception.to-1).end));
            return;
        }
    }

}
