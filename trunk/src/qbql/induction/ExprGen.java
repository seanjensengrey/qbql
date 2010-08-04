package qbql.induction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import qbql.lattice.Database;
import qbql.lattice.Program;
import qbql.parser.Lex;
import qbql.parser.LexerToken;
import qbql.parser.Matrix;
import qbql.parser.ParseNode;
import qbql.program.Run;
import qbql.util.Util;

public class ExprGen {
    
    static String[] zilliaryOps;
    final static String[] unaryOps = new String[] {
            "'",
            "`",
    };
    static String[] binaryRelsOps;
    public static void main( String[] args ) throws Exception {
        //final String goal = Util.readFile(ExprGen.class,"induction.prg");
        //String goal = "(x ^ y) v (x ^ (y`)') = expr.";
        //String goal = "(x ^ (y v z)) /< ((x ^ y) v (x ^ z)) = expr.";
        //String goal = "[] < x v y v z -> x /^ (y /^ z) = expr.";
        //String goal = "y + z = y <-> implication."; // Found: y * z = y <-> (((R11 ^ z) v (R00 ^ y)) = (z v y)).

        //String goal = "(x @^ y) @v (x @^ z) = expr.";
        String goal = "x /^ y = expr.";
        //String goal = "r#x < r#y <-> implication.";
        //String goal = "x = y <-> R00 = expr.";
        System.out.println("goal: "+goal);
        
        final String[] constants = new String[] {
            "R00",
            "R11",             
        };
        
        final String[] binaryOps = new String[] {
            "^",
            "v", 
            //"@*",
            "@^",
            //"/>",
            //"/<",
            //"/=",
            //"/^",
            //"/0",
            //"/1",
            //"/!",
        };
        final String[] binaryRels = new String[] {
        		"<",
        		//"=",
        		//"!=",
        		//"&",
        		//"|"
        };
        
        //String quickFile = "FD.db";
        String quickFile = "Figure1.db";
		String quickDb = Util.readFile(ExprGen.class,quickFile);
        //String fullDb = quickDb;
        String fullDb = Util.readFile(Run.class,"Figure1.db");
        
        final int threads = 4;//Runtime.getRuntime().availableProcessors();
        System.out.println("Using " + threads + " threads");
        
        final Lex lex = new Lex();
        List<LexerToken> src =  lex.parse(goal);
        Matrix matrix = Program.cyk.initMatrixSubdiagonal(src,true);
        int size = matrix.size();
        TreeMap<Integer,Integer> skipRanges = new TreeMap<Integer,Integer>();
        Program.cyk.closure(matrix, 0, size+1, skipRanges, -1);
        ParseNode root = Program.cyk.forest(size, matrix);
        if( !root.contains(Program.cyk.symbolIndexes.get("program") ) )
            throw new Exception("!root.contains(program)" );     
        
        final int subgoal = subgoal(src);
        binaryRelsOps = new String[binaryOps.length];
        if( subgoal == Program.implication )
        	binaryRelsOps = new String[binaryOps.length+binaryRels.length];
        for( int i = 0; i < binaryOps.length; i++ ) {
        	binaryRelsOps[i] = binaryOps[i];
        }
        if( subgoal == Program.implication )
        	for( int i = 0; i < binaryRels.length; i++ ) {
        		binaryRelsOps[i+binaryOps.length] = binaryRels[i];
        	}              
        
        Verifier[] verifiers= new Verifier[threads];
        for( int i = 0; i < threads; i++ ) {
			final Program quick = new Program(Database.init(quickDb));       
			final Program full = new Program(Database.init(fullDb));
            final Set<String> databaseOperations = new HashSet<String>();
            databaseOperations.addAll(full.database.operationNames());
            for( String op : databaseOperations )
            	quick.database.addOperation(op, full.database.getOperation(op));
        	verifiers[i] = new Verifier(goal, lex, subgoal, quick, full, databaseOperations);
		}
        
        final Set<String> variables = extractVariables(root, src, verifiers[0].full, subgoal);
        variables.remove(Program.cyk.allSymbols[subgoal]);
        
        zilliaryOps = new String[variables.size()+constants.length];
        for( int i = 0; i < variables.size(); i++ ) {
            zilliaryOps[i] = variables.toArray(new String[0])[i];
        }
        for( int i = 0; i < constants.length; i++ ) {
            zilliaryOps[i+variables.size()] = constants[i];
        }
        
        ArrayList<TreeNode> l = new ArrayList<TreeNode>();
        l.add(Polish.leaf());
        l.add(TreeNode.one);
        
        //boolean skip = true;
        boolean skip = false;
        for( Polish num = new Polish(l); ; num.next() ) {
            if( !num.wellBuilt() )
                continue;
            final TreeNode n = num.decode(); 
            if( n != null ) {
                //if( n.isRightSkewed() ) // !!!conflicts with other variable assignments!!!
                    //continue;
            	
                //System.out.println();
                try {
                    init(n);
                } catch( ArrayIndexOutOfBoundsException e ) { // no unary operations
                    continue;
                }
                n.print();
                if( skip && "(((((z ^ z) ^ (z)`) ^ ((z ^ z))`) ^ ((z ^ z))`))`".equals(n.toString()) )
                    skip = false;
                if( skip )
                    continue;
                do {  
            		if( n.isRightSkewed() )
            		    continue;
            		if( n.isAbsorpIdemp() )
            			continue;
            		if( n.isDoubleComplement() )
            			continue;
            		//if( n.toString().contains("(y * x) v y") )
            		    //n.print();
            		final String node = n.toString();
            		boolean launched = false;
            		do {
            			if( verifiers.length == 1 ) {
            				verifiers[0].exec(node);
            				break;
            			}
            			for( Verifier verifier : verifiers ) {
            				if( verifier.thread!=null && verifier.thread.isAlive() ) {
                    			Thread.yield();
            					continue;
            				}
            				verifier.execThread(node);
            				launched = true;
            				break;
            			}
            		} while( !launched );
                } while( ExprGen.next(n) );
            } else {
                //System.out.print('.');
            }
        }
    }
    
	static long startTime = System.currentTimeMillis();
	static long evalTime = 0;
    

    private static class Verifier {
    	final String goal;
    	final Lex lex;
    	final int subgoal;
    	final Program quick;
    	final Program full;
    	final Set<String> databaseOperations;
		
		Thread thread = null;
		
		public Verifier( String goal, Lex lex, int subgoal, Program quick,
				Program full, Set<String> databaseOperations ) {
			this.goal = goal;
			this.lex = lex;
			this.subgoal = subgoal;
			this.quick = quick;
			this.full = full;
			this.databaseOperations = databaseOperations;
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
			try {
				String input = goal.replace(Program.cyk.allSymbols[subgoal], node);

				List<LexerToken> src =  lex.parse(input);
				Matrix matrix = Program.cyk.initMatrixSubdiagonal(src);
				int size = matrix.size();
				TreeMap<Integer, Integer> skipRanges = new TreeMap<Integer,Integer>();
				Program.cyk.closure(matrix, 0, size+1, skipRanges, -1);
				ParseNode root = Program.cyk.forest(size, matrix);
				if( !root.contains(Program.cyk.symbolIndexes.get("program") ) )
					return;

				long t2 = System.currentTimeMillis();                   
				ParseNode eval = quick.program(root, src);
				evalTime += System.currentTimeMillis()-t2;
				quick.database.restoreOperations(databaseOperations);
				if( eval != null )
					return;
				t2 = System.currentTimeMillis();                   
				eval = full.program(root, src);
				evalTime += System.currentTimeMillis()-t2;
				full.database.restoreOperations(databaseOperations);
				if( eval != null )
					return;
				System.out.println("*** found *** ");
				System.out.println(input);
				System.out.println("Elapsed="+(System.currentTimeMillis()-startTime));
				System.out.println("evalTime="+evalTime);
				System.exit(0);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
		
		
    }
    

	private static Set<String> extractVariables( ParseNode root, List<LexerToken> src, Program p, int subgoal ) {
        if( root.contains(Program.assertion) ) {
        	for( ParseNode d : root.descendants() )
        		if( d.contains(subgoal) )
        			return p.variables(root,src);
        } 
        for( ParseNode child : root.children() ) {
        	Set<String> ret = extractVariables(child, src, p, subgoal);       
            if( ret != null )
                return ret;
        }
        return null;
	}

    private static int subgoal( List<LexerToken> src ) {
    	for( LexerToken t : src )
    		if( t.content.equals(Program.cyk.allSymbols[Program.implication]) )
    			return Program.implication;
    		else if( t.content.equals(Program.cyk.allSymbols[Program.expr]) )
    			return Program.expr;
        throw new AssertionError("no subgoal?");
    }
    
    static void init( TreeNode node ) {
        if( node.lft == null ) 
            node.label = zilliaryOps[0];
        else {
            init(node.lft);
            if( node.rgt == null ) 
                node.label = unaryOps[0];
            else {
                init(node.rgt);
                node.label = binaryRelsOps[0];
            }
        }
    }
    static boolean next( TreeNode node ) {
        Boolean ok = false;
        if( node.lft != null ) {
            ok = next(node.lft);
            if( ok )
                return true;
        }
        if( node.rgt != null ) {
            init(node.lft);
            ok = next(node.rgt);
            if( ok )
                return true;
        }
            
        if( node.lft == null ) {
            int index = index(node.label,zilliaryOps)+1;
            if( index == zilliaryOps.length )
                return false;
            else {
                init(node);
                node.label = zilliaryOps[index];
                return true;
            }
        } else {
            if( node.rgt == null ) {
                int index = index(node.label,unaryOps)+1;
                if( index == unaryOps.length )
                    return false;
                else {
                    init(node);
                    node.label = unaryOps[index];
                    return true;
                }
            } else {
                int index = index(node.label,binaryRelsOps)+1;
                if( index == binaryRelsOps.length )
                    return false;
                else {
                    init(node);
                    node.label = binaryRelsOps[index];
                    return true;
                }
            }
        }
    }
    private static int index( String s, String[] src ) {
        int ret = -1;
        for (int i = 0; i < src.length; i++) {
            if( s.equals(src[i]) )
                return i;
        }
        return ret;
    }
    

}
