package qbql.induction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import qbql.lattice.Database;
import qbql.lattice.Program;
import qbql.parser.Earley;
import qbql.parser.Lex;
import qbql.parser.LexerToken;
import qbql.parser.Matrix;
import qbql.parser.ParseNode;
import qbql.parser.SyntaxError;
import qbql.program.Run;
import qbql.util.Util;

public class ExprGen {
    final static boolean singleSolution = true;

    static String[] zilliaryOps;
    final static String[] unaryOps = new String[] {
        "<NOT>",
        "<INV>",
        "<EQ_CLOSE>",
        "<CP_CLOSE>",
    };
    static String[] binaryRelsOps;
    private static final String PARSE_ERROR_IN_ASSERTIONS_FILE = "*** Parse Error in assertions file ***";
    public static void main( String[] args ) throws Exception {
        final String goal = Util.readFile(ExprGen.class,"induction.prg");
        System.out.println("goal: "+Util.removeComments(goal));

        final String[] constants = new String[] {
                "R00",
                "R11",             
                //"Id",             
        };

        final String[] binaryOps = new String[] {
                "^",
                "v", 
                //"<and>",
                //"<\"and\">",
                //"/^",
                //"/>",
                //"/<",
                /*"/=",
            "/0",
            "/1",
            "/!",*/
        };
        final String[] binaryRels = new String[] {
                //"<",
                "=",
                //"!=",
                //"&",
                //"|"
        };

        //String quickFile = "FD.db";
        String quickFile = "Figure1.db";
        String quickDbsrc = Util.readFile(ExprGen.class,quickFile);
        quickDbsrc += "\n include udf.def;\n";
        //String fullDb = quickDb;
        String fullDbsrc = Util.readFile(Run.class,"Figure1.db");
        fullDbsrc += "\n include udf.def;\n";

        final int threads = Runtime.getRuntime().availableProcessors()-1;
        System.out.println("Using " + threads + " threads");

        final Lex lex = new Lex();
        List<LexerToken> src =  lex.parse(goal);
        Earley earley = Program.earley;
        Matrix matrix = new Matrix(earley);
        earley.parse(src, matrix); 
        SyntaxError err = SyntaxError.checkSyntax(goal, new String[]{"program"}, src, earley, matrix);      
        if( err != null ) {
            System.out.println(err.toString());
            throw new AssertionError(PARSE_ERROR_IN_ASSERTIONS_FILE);
        }
        ParseNode root = earley.forest(src, matrix);
        
        Map<String,Long> assertions = new HashMap<String,Long>();
        listAssertions(root,src,goal,assertions);

        final ParseNode subgoal = subgoal(root, src);
        binaryRelsOps = new String[binaryOps.length];
        if( subgoal.contains(Program.implication) )
            binaryRelsOps = new String[binaryOps.length+binaryRels.length];
        for( int i = 0; i < binaryOps.length; i++ ) {
            binaryRelsOps[i] = binaryOps[i];
        }
        if( subgoal.contains(Program.implication) )
            for( int i = 0; i < binaryRels.length; i++ ) {
                binaryRelsOps[i+binaryOps.length] = binaryRels[i];
            }              

        Verifier[] verifiers= new Verifier[threads];
        for( int i = 0; i < threads; i++ ) {
            Database quickDb = new Database("qbql.lang");
            final Program quick = new Program(quickDb);
            quick.outputVariables = false;
            quick.run(quickDbsrc);       
            Database fullDb = new Database("qbql.lang");
            final Program full = new Program(fullDb);
            full.outputVariables = false;
            full.run(fullDbsrc);       


            final Set<String> databaseOperations = new HashSet<String>();
            databaseOperations.addAll(full.database.operationNames());
            for( String op : databaseOperations )
                quick.database.addOperation(op, full.database.getOperation(op));
            verifiers[i] = new Verifier(assertions, lex, subgoal, quick, full, databaseOperations);
        }

        final Set<String> variables = extractVariables(root, src, verifiers[0].full);
        variables.remove("expr");

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

        boolean skip = true;
        skip = false;
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
                if( singleSolution )
                    n.print();
                if( skip && "((z ^ z) ^ z)".equals(n.toString()) )
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
                    //if( n.toString().contains("(((R00 ^ s) v t) ^ s)") )
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




    private static Set<String> extractVariables( ParseNode root, List<LexerToken> src, Program p/*, int subgoal*/ ) {
        if( root.contains(Program.assertion) ) {
            for( ParseNode d : root.descendants() )
                //if( d.contains(subgoal) )
                    return p.variables(root,src);
        } 
        for( ParseNode child : root.children() ) {
            Set<String> ret = extractVariables(child, src, p/*, subgoal*/);       
            if( ret != null )
                return ret;
        }
        return null;
    }

    private static ParseNode subgoal( ParseNode root, List<LexerToken> src ) {
    	for( ParseNode p : root.descendants() )
    		if( p.from+1==p.to && p.contains(Program.expr) && p.content(src).equals(Program.earley.allSymbols[Program.expr]) )
                return p;
    		else if( p.contains(Program.inductionFormula) )
                return p;
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

    private static void listAssertions( ParseNode root, List<LexerToken> src, String input, Map<String,Long> ret ) {
		if( root.contains(Program.assertion) ) {
			ParseNode goal = subgoal(root, src);
			int offset = src.get(root.from).begin;
			ret.put(input.substring(offset,src.get(root.to-1).end),
					                Util.lPair(src.get(goal.from).begin-offset, src.get(goal.to-1).end-offset ));
			return;
		}
		for( ParseNode p : root.children() )
			listAssertions(p, src, input, ret);
	}

}
