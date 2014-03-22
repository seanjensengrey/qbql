package qbql.lang.parse;

import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import qbql.gui.Gui;
import qbql.lattice.Database;
import qbql.lattice.Program;
import qbql.lattice.Relation;
import qbql.parser.Earley;
import qbql.parser.Grammar;
import qbql.parser.CYK;
import qbql.parser.Lex;
import qbql.parser.LexerToken;
import qbql.parser.Matrix;
import qbql.parser.ParseNode;
import qbql.parser.RuleTuple;
import qbql.parser.SyntaxError;
import qbql.util.Util;

public class ParseDb extends Database {
    List<LexerToken> src;
    ParseNode root;
    int node;
    
    public ParseDb( String pkg, String db ) {
        super(pkg);
        try {
            src =  new Lex().parse(db);
            LexerToken.print(src);

            Earley earley = new Earley(myGrammarRules());
            node = earley.symbolIndexes.get("node");
            Matrix matrix = new Matrix(earley);
            earley.parse(src, matrix); 
            SyntaxError err = SyntaxError.checkSyntax(db, new String[]{"program"}, src, earley, matrix);      
            if( err != null ) {
                System.out.println(err.toString());
                throw new AssertionError(PARSE_ERROR_IN_ASSERTIONS_FILE);
            }
            ParseNode root = earley.forest(src, matrix);
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
    
	static final String PARSE_ERROR_IN_ASSERTIONS_FILE = "*** Parse Error in assertions file ***";
    public static ParseDb run( String database, String prg ) throws Exception {
        
        StackTraceElement[] stack = new Throwable().getStackTrace();
        String createdInClass = stack[0].getClassName();
        String pkg = createdInClass.substring(0,createdInClass.lastIndexOf('.'));
        ParseDb db = new ParseDb(pkg,database);
        
        // program
        List<LexerToken> src =  new Lex().parse(prg);
        Earley earley = new Earley(myGrammarRules());
        Matrix matrix = new Matrix(earley);
        earley.parse(src, matrix); 
        SyntaxError err = SyntaxError.checkSyntax(prg, new String[]{"program"}, src, earley, matrix);      
        if( err != null ) {
            System.out.println(err.toString());
            throw new AssertionError(PARSE_ERROR_IN_ASSERTIONS_FILE);
        }
        ParseNode root = Program.earley.forest(src, matrix);
        
        if( root.topLevel != null ) {
            System.out.println("*** Parse Error in assertions file ***");
            CYK.printErrors(prg, src, root);
            return null;
        }
        System.out.println("-------------------------------------");

        Program program = new Program(db); 
        ParseNode exception = program.program(root,src);
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
        ParseNode root = Grammar.parseGrammarFile(src, input);
        return Grammar.grammar(root, src);
    }
}
