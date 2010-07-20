package qbql.gui;

import java.util.List;
import java.util.TreeMap;

import javax.swing.JFrame;

import qbql.lattice.Database;
import qbql.lattice.Program;
import qbql.parser.CYK;
import qbql.parser.Lex;
import qbql.parser.LexerToken;
import qbql.parser.Matrix;
import qbql.parser.ParseNode;
import qbql.util.Util;


public class Graphics2D extends Database {
    public static void main( String[] args ) throws Exception {
        final String db = Util.readFile(Graphics2D.class,"graphics.db");
        final String prg = Util.readFile(Graphics2D.class,"graphics.prg");
        run(db,prg);
        
        JFrame f = new JFrame("Graphics Database");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);

    }
    
    public Graphics2D( String pkg, String db ) {
        super(pkg);
    }
    
    public static Graphics2D run( String database, String prg ) throws Exception {
        
        StackTraceElement[] stack = new Throwable().getStackTrace();
        String createdInClass = stack[0].getClassName();
        String pkg = createdInClass.substring(0,createdInClass.lastIndexOf('.'));
        Graphics2D db = new Graphics2D(pkg,database);
        
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

        Program program = new Program(db); 
        ParseNode exception = program.program(root,src);
        if( exception != null ) {
            System.out.println("*** False Assertion ***");
            System.out.println(prg.substring(src.get(exception.from).begin, src.get(exception.to-1).end));
        }
        
        //Relation result = (Relation)db.predicate("Result");
        return db;
    }

}
