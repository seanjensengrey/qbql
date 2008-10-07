package qbql.lattice;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import qbql.parser.CYK;
import qbql.parser.LexerToken;
import qbql.parser.Matrix;
import qbql.parser.ParseNode;
import qbql.util.Util;

public class Database {
	
	Map<String,Relation> lattice = new HashMap<String,Relation>();
	static Relation R00 = new Relation(new String[]{});
	Relation R11;
	static Relation R01 = new Relation(new String[]{});
	Relation R10;
	
	static {
		R01.addTuple(new TreeMap<String,String>());
		
		LexerToken.isPercentLineComment = true;
	}
	
	final static String databaseFile = "Figure1.db"; 
	final static String assertionsFile = "bilattice.assertions"; 
	//final static String databaseFile = "Wittgenstein.db"; 
	//final static String assertionsFile = "Wittgenstein.assertions"; 
	//final static String databaseFile = "Sims.db"; 
	//final static String assertionsFile = "Sims.assertions"; 
	public Database() throws Exception {				
		String database = Util.readFile(Database.class,"/qbql/lattice/"+databaseFile);
		
		List<LexerToken> src =  LexerToken.parse(database);
		Matrix matrix = cyk.initArray1(src);
		int size = matrix.size();
		TreeMap<Integer,Integer> skipRanges = new TreeMap<Integer,Integer>();
		cyk.closure(matrix, 0, size+1, skipRanges, -1);
		ParseNode root = cyk.forest(size, matrix);
		
		if( root.topLevel != null ) {
			System.out.println("*** Parse Error in database file ***");
			printErrors(database, src, root);
			throw new Exception("Parse Error");
		}
		
		lattice.put("R00",R00);	
		lattice.put("R01",R01);
		
		database(root,src);
		
		R10 = buildR10();
		R11 = buildR11();
		lattice.put("R10",R10);
		lattice.put("R11",R11);
		
		// relations that requre complement can be built only after R10 and R11 are defined
		try {
			lattice.put("((A ^ D) v (B ^ C))'",complement(lattice.get("AjDuBjC")));
		} catch( Exception e ) { // NPE if databaseFile is not Figure1.db
		}

	}
	
	Relation outerUnion( Relation x, Relation y ) {
		return Relation.innerUnion( Relation.join(x, Relation.innerUnion(y, R11))
				                  , Relation.join(y, Relation.innerUnion(x, R11)) 
		);
	}
	
	Relation complement( Relation x ) {
		Relation xvR11 = Relation.innerUnion(x, R11);
		Relation ret = Relation.join(x, R00);
		for( Tuple t : xvR11.content ) {
			boolean matched = false;
			for( Tuple tx : x.content )
				if( t.equals(tx, ret, x) ) {
					matched = true;
					break;
				}
			if( !matched )
				ret.content.add(t);
		}
		return ret;
	}
	
	Relation buildR10() {
		Relation ret = new Relation(new String[]{});
		for( Relation r : lattice.values() )
			ret = Relation.join(ret, r);
		return ret;	
	}

	Relation buildR11() {
		Map<String, Relation> domains = new HashMap<String, Relation>();
		for( String col : R10.colNames )
			domains.put(col, new Relation(new String[]{col}));
	
		for( Relation rel : lattice.values() ) 
			for( Tuple t : rel.content )
				for( int i = 0; i < t.data.length; i++ ) {
					Tuple newTuple = new Tuple(new String[]{t.data[i]});
					domains.get(rel.colNames[i]).content.add(newTuple);
				}
		
		Relation ret = R01;
		for( Relation domain : domains.values() )
			ret = Relation.join(ret, domain);
		return ret;	
	}
	
	public Relation eval( String input ) throws Exception {
		List<LexerToken> src =  LexerToken.parse(input);
		Matrix matrix = cyk.initArray1(src);
		int size = matrix.size();
		TreeMap<Integer,Integer> skipRanges = null;//new TreeMap<Integer,Integer>();
		cyk.closure(matrix, 0, size+1, skipRanges, -1);
		ParseNode root = cyk.forest(size, matrix);
		return eval(root,src);
	}
	private Relation eval( ParseNode node, List<LexerToken> src ) throws Exception {
		if( node.from + 1 == node.to ) {
			Relation ret = lattice.get(src.get(node.from).content);
			if( ret == null )
				throw new Exception("There is no relation "+src.get(node.from).content+" in the database");
			return ret;
		}
		
		Relation x = null;
		Relation y = null;
		int oper = -1;
		boolean parenGroup = false;
		for( ParseNode child : node.children() ) {
			if( parenGroup )
				return eval(child, src);
			else if( child.contains(openParen) )
				parenGroup = true;
			else if( child.contains(relation) || child.contains(expr) ) {
				if( x == null )
					x = compute(child,src);
				else
					y = compute(child,src);
			} else
				oper = child.content().toArray(new Integer[0])[0];
		}
		if( oper == join ) 
		    return Relation.join(x,y);
		if( oper == innerJoin ) 
		    return Relation.innerJoin(x,y);
		if( oper == outerUnion ) 
		    return outerUnion(x,y);
		if( oper == innerUnion ) 
		    return Relation.innerUnion(x,y);
		if( oper == complement ) 
		    return complement(x);
		return null;
	}
	
	public ParseNode assertion( ParseNode root, List<LexerToken> src ) throws Exception {
//System.out.println(root.content(src));
		for( ParseNode child : root.children() ) {
			if( child.contains(equation) ) {
				boolean OK = equationOrConstraint(child,src);
				if( !OK )
					return child;
				else
					return null;
			} else if( child.contains(implication) )
				return implication(child,src);
		}
		throw new Exception("Missed equation in the assertion");
	}
	
	public boolean equationOrConstraint( ParseNode root, List<LexerToken> src ) throws Exception {
		boolean isEquation = false;
		Relation left = null;
		Relation right = null;
		for( ParseNode child : root.children() ) {
			if( left == null )
				left = compute(child,src);
			else if( child.contains(eq) )
				isEquation = true;
			else if( child.contains(lt) )
				isEquation = false;
			else 				
				right = compute(child,src);
		}
		if( isEquation && !left.equals(right)
				|| !isEquation && !Relation.le(left,right)
		) 
			return false;

		return true;
	}
	public ParseNode implication( ParseNode root, List<LexerToken> src ) throws Exception {
		Boolean left = null;
		Boolean right = null;
		for( ParseNode child : root.children() ) {
			if( left == null )
				left = equationOrConstraint(child,src);
			else if( child.contains(gt)||child.contains(minus) )
				;
			else 				
				right = equationOrConstraint(child,src);
		}		
		if( !left || right )
			return null;
		else
			return root;
	}
	
	private boolean next( int[] state, int limit ) {
		for( int pos = 0; pos < state.length; pos++ ) {
			if( state[pos] < limit-1 ) {
				state[pos]++;
				return true;
			}
			state[pos] = 0;				
		}
		return false;
	}
	
	/**
	 * @param root
	 * @param src
	 * @return node that violates assertion
	 * @throws Exception
	 */
	public ParseNode assertions( ParseNode root, List<LexerToken> src ) throws Exception {
		if( root.contains(assertion) )
			return assertion(root,src);
		ParseNode ret = null;
		for( ParseNode child : root.children() ) {
			if( child.contains(assertion) )
				ret =  assertion(child,src);
			else 
				ret =  assertions(child,src);	
			if( ret != null )
				return ret;
		}
		return ret;
	}
	
	
	public void database( ParseNode root, List<LexerToken> src ) throws Exception {
		if( root.contains(assignment) )
			createRelation(root,src);
		else
			for( ParseNode child : root.children() ) {				
				if( child.contains(assignment) )
					createRelation(child,src);
				else 
					database(child,src);
			}
	}
	public void createRelation( ParseNode root, List<LexerToken> src ) throws Exception {
		String left = null;
		Relation right = null;
		for( ParseNode child : root.children() ) {
			if( left == null )
				left = child.content(src);
			else if( child.contains(eq) )
				;
			else { 				
				right = compute(child,src);
				break;
			}
		}
		lattice.put(left, right);
	}
	public Relation compute( ParseNode root, List<LexerToken> src ) throws Exception {
		if( root.contains(relation) ) {
			for( ParseNode child : root.children() ) {
				if( child.contains(tuples) )
					return tuples(child,src);
			}
		} else
			return eval(root,src);
		throw new Exception("Unknown case");
	}
	public Relation tuples( ParseNode root, List<LexerToken> src ) throws Exception {
		Set<String> attrs = new HashSet<String>();
		for( ParseNode descendant: root.descendants() )
			if( descendant.contains(attribute) )
				attrs.add(descendant.content(src));
		Relation ret = new Relation(attrs.toArray(new String[0]));
		
		addTuples(ret,root,src);		
		return ret;
	}
	public void addTuples( Relation ret, ParseNode root, List<LexerToken> src ) throws Exception {
		if( root.contains(tuple) ) 
			ret.addTuple(tuple(root,src));
		else for( ParseNode child : root.children() )
			if( child.contains(tuple) )
				ret.addTuple(tuple(child,src));
			else if( child.contains(tuples) )
				addTuples(ret,child,src);
	}
	public Map<String,String> tuple( ParseNode root, List<LexerToken> src ) throws Exception {
		for( ParseNode child : root.children() ) {
			if( child.contains(values) ) {
				Map<String,String> tuple = new TreeMap<String,String>(); 
				values(tuple, child,src);
				return tuple;
			}
		}
		throw new Exception("Unknown case");
	}
	public void values( Map<String,String> tuple, ParseNode root, List<LexerToken> src ) {
		if( root.contains(value) )
			value(tuple,root,src);
		else for( ParseNode child : root.children() )
			if( child.contains(value) )
				value(tuple,child,src);
			else if( child.contains(values) )
				values(tuple,child,src);
	}
	public void value( Map<String,String> tuple, ParseNode root, List<LexerToken> src ) {
		String left = null;
		String right = null;
		for( ParseNode child : root.children() ) {
			if( left == null )
				left = child.content(src);
			else if( child.contains(eq) )
				;
			else 				
				right = child.content(src);
		}
		tuple.put(left, right);
	}
		
	static CYK cyk;
	static int join;
	static int innerJoin;
	static int innerUnion;
	static int outerUnion;
	static int complement;
	static int eq;
	static int minus;
	static int expr;
	static int openParen;
	static int equation;
	static int implication;
	static int lt;
	static int gt;
	static int assertion;
	static int identifier;
	
	static int assignment;
	static int relation;
	static int tuples;
	static int tuple;
	static int attribute;
	static int values;
	static int value;
	static int comma;
	static {
		try {
			cyk = new CYK(Relation.getRules()) {
				public int[] atomicSymbols() {
					return new int[] {assertion};
				}
			};
			join = cyk.symbolIndexes.get("'^'");
			innerJoin = cyk.symbolIndexes.get("'*'");
			innerUnion = cyk.symbolIndexes.get("'v'");
			outerUnion = cyk.symbolIndexes.get("'+'");
			complement = cyk.symbolIndexes.get("'''");
			eq = cyk.symbolIndexes.get("'='");
			minus = cyk.symbolIndexes.get("'-'");
			lt = cyk.symbolIndexes.get("'<'");
			gt = cyk.symbolIndexes.get("'>'");
			expr = cyk.symbolIndexes.get("expr");
			openParen = cyk.symbolIndexes.get("'('");
			equation = cyk.symbolIndexes.get("equation");
			implication = cyk.symbolIndexes.get("implication");
			assertion = cyk.symbolIndexes.get("assertion");
			identifier = cyk.symbolIndexes.get("identifier");
			
			assignment = cyk.symbolIndexes.get("assignment");
			relation = cyk.symbolIndexes.get("relation");
			tuples = cyk.symbolIndexes.get("tuples");
			tuple = cyk.symbolIndexes.get("tuple");
			attribute = cyk.symbolIndexes.get("attribute");
			values = cyk.symbolIndexes.get("values");
			value = cyk.symbolIndexes.get("value");
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}

	/**
	 * Iterate through all the relations in the database
	 * @param root
	 * @param src
	 * @return
	 * @throws Exception
	 */
	public void iterate( ParseNode root, List<LexerToken> src ) throws Exception {
		String[] relVars = lattice.keySet().toArray(new String[0]);
		
		Set<String> variables = new HashSet<String>();
		for( ParseNode descendant : root.descendants() ) {
			String id = descendant.content(src);
			if( descendant.from+1 == descendant.to && descendant.contains(expr) && Character.isLowerCase(id.substring(0,1).toCharArray()[0]) ) 
				variables.add(id);
		}
		
		int[] indexes = new int[variables.size()];
		for(int i = 0; i < indexes.length; i++)
			indexes[i] = 0;
		do {
			int var = 0;
			for( String variable : variables ) {
				lattice.put(variable, lattice.get(relVars[indexes[var++]]));
			}
			
			ParseNode exception = assertions(root,src);
			if( exception != null ) {
				System.out.println("*** False Assertion ***");
				System.out.println(exception.content(src));
				for( String variable : variables )
					System.out.println(variable+" = "+lattice.get(variable));
				//System.out.println("left = "+left);
				//System.out.println("right = "+right);
				return;
			}
		} while( next(indexes,relVars.length) );
		
		System.out.println("All Assertions are OK");
		return;
	}
	
	
	public static void main( String[] args ) throws Exception {
		String axioms = Util.readFile(Database.class,"/qbql/lattice/"+assertionsFile);
		
		List<LexerToken> src =  LexerToken.parse(axioms);
		Matrix matrix = cyk.initArray1(src);
		int size = matrix.size();
		TreeMap<Integer,Integer> skipRanges = new TreeMap<Integer,Integer>();
		cyk.closure(matrix, 0, size+1, skipRanges, -1);
		ParseNode root = cyk.forest(size, matrix);
		
		if( root.topLevel != null ) {
			System.out.println("*** Parse Error in assertions file ***");
			printErrors(axioms, src, root);
		}
						
		Database model = new Database();
		model.iterate(root, src);
	}

	private static void printErrors( String axioms, List<LexerToken> src, ParseNode root ) {
		int begin = 0;
		int end = axioms.length();
		int iteration = 0;
		for( ParseNode node : root.children() ) {
			if( iteration == 0 ) {
				iteration++;
				continue;
			}
			if( begin < src.get(node.from).begin ) 
				begin = src.get(node.from).begin;
			if( src.get(node.to).end < end ) 
				end = src.get(node.to).end;
			if( 1 <= iteration++ )
				break;
		}
		System.out.println(axioms.substring(begin, end));
	}
}
