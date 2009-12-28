package qbql.lang.parse;

import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import qbql.lattice.Database;
import qbql.lattice.Program;
import qbql.lattice.Relation;
import qbql.parser.BNFGrammar;
import qbql.parser.CYK;
import qbql.parser.Lex;
import qbql.parser.LexerToken;
import qbql.parser.Matrix;
import qbql.parser.ParseNode;
import qbql.parser.RuleTuple;
import qbql.util.Util;

public class ParseDb extends Database {
    List<LexerToken> src;
    ParseNode root;
    CYK cyk;    
    int node;
    
    public ParseDb( String pkg, String db ) {
        super(pkg);
        try {
            cyk = new CYK(myGrammarRules()) {
                public int[] atomicSymbols() {
                    return new int[] {node};
                }
            };  
            node = cyk.symbolIndexes.get("node");
            
            src =  new Lex().parse(db);
            LexerToken.print(src);

            Matrix matrix = cyk.initMatrixSubdiagonal(src);
            int size = matrix.size();
            TreeMap<Integer,Integer> skipRanges = new TreeMap<Integer,Integer>();
            cyk.closure(matrix, 0, size+1, skipRanges, -1);
            root = cyk.forest(size, matrix);
            root.printTree();

        } catch( Exception e ) {
            e.printStackTrace(); 
        }
    }
        
    public static void main( String[] args ) throws Exception {
        final String db = Util.readFile(ParseDb.class,"test.db");
        final String prg = Util.readFile(ParseDb.class,"test.prg");
        run(db,prg);
    }
    
    public static ParseDb run( String database, String prg ) throws Exception {
        
        StackTraceElement[] stack = new Throwable().getStackTrace();
        String createdInClass = stack[0].getClassName();
        String pkg = createdInClass.substring(0,createdInClass.lastIndexOf('.'));
        ParseDb db = new ParseDb(pkg,database);
        
        // program
        List<LexerToken> src =  new Lex().parse(prg);
        Matrix matrix = Program.cyk.initMatrixSubdiagonal(src);
        int size = matrix.size();
        TreeMap<Integer,Integer> skipRanges = new TreeMap<Integer,Integer>();
        Program.cyk.closure(matrix, 0, size+1, skipRanges, -1);
        ParseNode root = Program.cyk.forest(size, matrix);

        if( root.topLevel != null ) {
            System.out.println("*** Parse Error in assertions file ***");
            CYK.printErrors(prg, src, root);
            return null;
        }
        System.out.println("-------------------------------------");

        Program program = new Program(src,db); 
        ParseNode exception = program.program(root);
        if( exception != null ) {
            System.out.println("*** False Assertion ***");
            System.out.println(prg.substring(src.get(exception.from).begin, src.get(exception.to-1).end));
        }
        
        //Relation result = (Relation)db.predicate("Result");
        return db;
    }

    private static Set<RuleTuple> myGrammarRules() throws Exception {
        String input = Util.readFile(ParseDb.class, "test.grammar"); //$NON-NLS-1$
        Lex lex = new Lex();
        lex.isQuotedString = true;
        List<LexerToken> src = lex.parse(input);
        //LexerToken.print(src);
        ParseNode root = BNFGrammar.parseGrammarFile(src, input);
        return BNFGrammar.grammar(root, src);
    }
}
