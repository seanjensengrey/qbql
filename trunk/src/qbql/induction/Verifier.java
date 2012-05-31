package qbql.induction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import qbql.lattice.Program;
import qbql.parser.Earley;
import qbql.parser.Lex;
import qbql.parser.LexerToken;
import qbql.parser.Matrix;
import qbql.parser.ParseNode;
import qbql.parser.SyntaxError;
import qbql.util.Util;

class Verifier {
    final Map<String,long[]> assertions;
    final Lex lex;
    final Program quick;
    final Program full;
    final Set<String> databaseOperations;
    final String[] variables;
    Earley earley;

    Thread thread = null;
    
    public Verifier( Map<String,long[]> assertions, Lex lex, Program quick,
            Program full, Set<String> databaseOperations, final String[] variables ) throws Exception {
    	this.assertions = assertions;
        this.lex = lex;
        this.quick = quick;
        this.full = full;
        this.databaseOperations = databaseOperations;
        this.variables = variables;
        earley = new Earley(Program.latticeRules());
    }

    public void execThread( final TreeNode node ) {
        thread = new Thread() {
            public void run() {
                exec(node); 
            }
        };
        thread.start();
    }

    public void exec( final TreeNode n ) {
        String node = n.toString();
    	StringBuilder print = new StringBuilder();
    	for( String goal : assertions.keySet() )
    		try {
    			String input = goal;
    			long[] intervals = assertions.get(goal);
    			for( int i = intervals.length-1; 0 <= i ; i-- ) {
    				String inductionFormula = input.substring(Util.lX(intervals[i]),Util.lY(intervals[i]));
    				List<String> formalArguments = parseInductionFormula(inductionFormula);
    				node = substituteFormalFariables(node, formalArguments);
    				input = input.substring(0,Util.lX(intervals[i]))+node+input.substring(Util.lY(intervals[i]));				
				}
    			print.append(input);
    			if( goal.equals(input) )
    				throw new AssertionError("goal didn't change");

    			List<LexerToken> src =  lex.parse(input);
    			Matrix matrix = new Matrix(earley);
    			earley.parse(src, matrix);
    			ParseNode root = earley.forest(src, matrix);
    			if( !root.contains(Program.assertion) ) {
    				boolean OK = ExprGen.nextOp(n);
    				return;
    			}

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

	private String substituteFormalFariables( String node, List<String> formalArguments ) {
		String uniquePrefix = "%^$";
		for( int j = 0; j < variables.length && j < formalArguments.size(); j++ ) {
			node = node.replace(variables[j], uniquePrefix+j);
		}
		for( int j = 0; j < variables.length && j < formalArguments.size(); j++ ) {
			node = node.replace(uniquePrefix+j, formalArguments.get(j));
		}
		return node;
	}

	private List<String> parseInductionFormula( String txt ) {
		List<String> ret = new ArrayList<String>();
		int i0 = txt.indexOf('(');
		if( i0 < 0 )
			return ret;
		int i1 = txt.indexOf(')');
		txt = txt.substring(i0+1,i1);
        StringTokenizer st = new StringTokenizer(txt,",",false);
        while( st.hasMoreTokens() ) {
        	String token = st.nextToken();
        	ret.add(token);
        }
		return ret;
	}


}
