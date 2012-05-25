package qbql.induction;

import java.util.List;
import java.util.Map;
import java.util.Set;

import qbql.lattice.Program;
import qbql.parser.Earley;
import qbql.parser.Lex;
import qbql.parser.LexerToken;
import qbql.parser.Matrix;
import qbql.parser.ParseNode;
import qbql.parser.SyntaxError;
import qbql.util.Util;

class Verifier {
    final Map<String, Long> assertions;
    final Lex lex;
    final Program quick;
    final Program full;
    final Set<String> databaseOperations;
    Earley earley;

    Thread thread = null;
    
    public Verifier( Map<String, Long> assertions, Lex lex, ParseNode subgoal2, Program quick,
            Program full, Set<String> databaseOperations ) throws Exception {
    	this.assertions = assertions;
        this.lex = lex;
        this.quick = quick;
        this.full = full;
        this.databaseOperations = databaseOperations;
        earley = new Earley(Program.latticeRules());
    }

    public void execThread( final String node ) {
        thread = new Thread() {
            public void run() {
                exec(node); 
            }
        };
        thread.start();
    }

    public void exec( final String node ) {
    	StringBuilder print = new StringBuilder();
    	for( String goal : assertions.keySet() )
    		try {
    			Long interval = assertions.get(goal);
    			String input = goal.substring(0,Util.lX(interval))+node+goal.substring(Util.lY(interval));
    			print.append(input);
    			if( goal.equals(input) )
    				throw new AssertionError("goal didn't change");

    			List<LexerToken> src =  lex.parse(input);
    			Matrix matrix = new Matrix(earley);
    			earley.parse(src, matrix);
    			/*SyntaxError err = SyntaxError.checkSyntax(input, new String[]{"assertion"}, src, earley, matrix);      
    			if( err != null ) {
    				System.out.println(err.toString());
    				throw new AssertionError(ExprGen.PARSE_ERROR_IN_ASSERTIONS_FILE);
    			}*/
    			ParseNode root = earley.forest(src, matrix);
    			if( !root.contains(Program.assertion) )
    				return;

    			long t2 = System.currentTimeMillis();                   
    			ParseNode eval = quick.program(root, src);
    			ExprGen.evalTime += System.currentTimeMillis()-t2;
    			quick.database.restoreOperations(databaseOperations);
    			if( eval != null )
    				return;
    			t2 = System.currentTimeMillis();                   
    			eval = full.program(root, src);
    			ExprGen.evalTime += System.currentTimeMillis()-t2;
    			full.database.restoreOperations(databaseOperations);
    			if( eval != null )
    				return;
    		} catch( Throwable e ) {
    			e.printStackTrace();
    			System.exit(0);
    		}
		System.out.println("*** found *** ");
   
		System.out.println(print.toString());
		System.out.println("Elapsed="+(System.currentTimeMillis()-ExprGen.startTime));
		System.out.println("evalTime="+ExprGen.evalTime);
		if( ExprGen.singleSolution )
			System.exit(0);
    }


}
