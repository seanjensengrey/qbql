package qbql.lang.parse;

import java.util.List;
import java.util.TreeMap;

import qbql.lattice.Database;
import qbql.lattice.Grammar;
import qbql.parser.CYK;
import qbql.parser.Lex;
import qbql.parser.LexerToken;
import qbql.parser.Matrix;
import qbql.parser.ParseNode;
import qbql.parser.RuleTuple;

public class ParseDb extends Database {
    List<LexerToken> src;
    ParseNode root;
    CYK cyk;
    
    public ParseDb( String pkg, List<LexerToken> src, ParseNode root, CYK cyk ) {
        super(pkg);
        this.src = src;
        this.root = root;
        this.cyk = cyk;
    }
    
    public static void main( String[] args ) throws Exception {
        CYK cyk = Grammar.cyk;          
        final String input =
            //"x ^ (e v (y ^ R00)) -> y ^ (e v (x ^ R00)).";
            "[source from] Hello 3 /0 [from=f];"
            //"( ( x v y ) ^ ( ( x ^ y ) v ( ( x v y ) ) ' ) )"
            //"cat ^ [source] Hello World ^ [from] 3;"
            //Util.readFile("c:/qbql_trunk/qbql/lattice/Partition.prg")
        ;
        List<LexerToken> src =  new Lex().parse(input);
        LexerToken.print(src);

        Matrix matrix = cyk.initArray1(src);
        int size = matrix.size();
        TreeMap<Integer,Integer> skipRanges = new TreeMap<Integer,Integer>();
        cyk.closure(matrix, 0, size+1, skipRanges, -1);
        ParseNode root = cyk.forest(size, matrix);
        root.printTree();
        
        StackTraceElement[] stack = new Throwable().getStackTrace();
        String createdInClass = stack[0].getClassName();
        String pkg = createdInClass.substring(0,createdInClass.lastIndexOf('.'));
        ParseDb db = new ParseDb(pkg, src, root, cyk);
        
        // program
        String prg = "[pos] \"[8,13)\" ^ Vars;";
        src =  new Lex().parse(prg);
        matrix = Grammar.cyk.initArray1(src);
        size = matrix.size();
        skipRanges = new TreeMap<Integer,Integer>();
        Grammar.cyk.closure(matrix, 0, size+1, skipRanges, -1);
        root = Grammar.cyk.forest(size, matrix);

        if( root.topLevel != null ) {
            System.out.println("*** Parse Error in assertions file ***");
            CYK.printErrors(prg, src, root);
            return;
        }

        Grammar program = new Grammar(src,db); 
        ParseNode exception = program.program(root);
    }

}
