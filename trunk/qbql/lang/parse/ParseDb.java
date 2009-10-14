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
import qbql.program.Run;
import qbql.util.Util;

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
        final String input = Util.readFile(ParseDb.class,"text.db");
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
        String prg = Util.readFile(ParseDb.class,"test.prg");;
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
