package qbql.bool;

import java.util.List;

import qbql.parser.Lex;
import qbql.parser.LexerToken;
import qbql.parser.ParseNode;
import qbql.util.Util;

/**
 * Mace4-inspired tool optimized and specialized for Boolean Algebra embeddings (e.g. BA with closure)
 * There is only one model of size 2^n (up to isomorphism) which cuts down model checking time significantly
 * One has to shuffle elements in the additional operators only 
 * @author Dim
 */
public class Model {
    
    public static void main( String[] args ) throws Exception {
        String input = null;
        //Profile.sample(10000, 30);
        if( args.length == 1 ) {
            input = Util.readFile(args[0]);
        } else {
            input = Util.readFile(Model.class,"lattice.embeding");     
        }
                
        List<LexerToken> src =  new Lex().parse(input);
        ParseNode root = Program.parse(input, src);
        Program.variables = Program.variables(root, src);
        
        for( int dim = 1; dim < 10; dim++ ) {
            long t1 = System.currentTimeMillis();
            long size = 1L << dim;
            UnaryOperator oper = new UnaryOperator(dim);
            int count = 0;
            do {
                Program prg = new Program(oper);
                int[] model = prg.program(root, src);
                if( model != null ) {
                    System.out.println(oper.toString());
                    for( String var : prg.assignments.keySet() ) {
                        System.out.print(var+"=");
                        long vector = prg.assignments.get(var);
                        System.out.println(Oper.toString(vector, dim));
                    }
                    return;
                }
                count++;
                oper = oper.next();
            } while( oper != null );
            System.out.print("models("+size+")="+count);
            System.out.println("      ("+(System.currentTimeMillis()-t1)+" ms)");
        }
    }
    
}
