package qbql.lattice;

import java.util.HashMap;
import java.util.HashSet;
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

public class Model {
	
	Map<String,Relation> lattice = new HashMap<String,Relation>();
	static Relation R00 = new Relation(new String[]{});
	Relation R11;
	static Relation R01 = new Relation(new String[]{});
	Relation R10;
	
	static {
		R01.addTuple(new TreeMap<String,String>());
	}
	
	public Model() {
		LexerToken.isPercentLineComment = true;
		{
			Relation tmp = new Relation(new String[]{"x"});
			Map<String,String> tuple = new TreeMap<String,String>(); 
			tuple.put("x", "1");
			tmp.addTuple(tuple);
			lattice.put("A",tmp);
		}
		{
			Relation tmp = new Relation(new String[]{"x"});
			Map<String,String> tuple = new TreeMap<String,String>(); 
			tuple.put("x", "1");
			tmp.addTuple(tuple);
			tuple.clear();
			tuple.put("x", "2");
			tmp.addTuple(tuple);
			lattice.put("B",tmp);
		}
		{
			Relation tmp = new Relation(new String[]{"y"});
			Map<String,String> tuple = new TreeMap<String,String>(); 
			tuple.put("y", "a");
			tmp.addTuple(tuple);
			lattice.put("C",tmp);
		}
		{
			Relation tmp = new Relation(new String[]{"y"});
			Map<String,String> tuple = new TreeMap<String,String>(); 
			tuple.put("y", "a");
			tmp.addTuple(tuple);
			tuple.clear();
			tuple.put("y", "b");
			tmp.addTuple(tuple);
			lattice.put("D",tmp);
		}
		lattice.put("R00",R00);		
		lattice.put("A ^ C",Relation.join(lattice.get("A"), lattice.get("C")));
		lattice.put("A ^ D",Relation.join(lattice.get("A"), lattice.get("D")));
		lattice.put("B ^ C",Relation.join(lattice.get("B"), lattice.get("C")));
		lattice.put("`x`",Relation.join(lattice.get("A"), lattice.get("R00")));
		lattice.put("`y`",Relation.join(lattice.get("C"), lattice.get("R00")));
		lattice.put("(A ^ D) v (B ^ C)",Relation.innerUnion(lattice.get("A ^ D"), lattice.get("B ^ C")));
		
		{
			Relation tmp = new Relation(new String[]{"x","z"});
			Map<String,String> tuple = new TreeMap<String,String>(); 
			tuple.put("x", "0");
			tuple.put("z", "@");
			tmp.addTuple(tuple);
			tuple.clear();
			tuple.put("x", "1");
			tuple.put("z", "@");
			tmp.addTuple(tuple);
			lattice.put("E",tmp);
		}
		{
			Relation tmp = new Relation(new String[]{"y","z"});
			Map<String,String> tuple = new TreeMap<String,String>(); 
			tuple.put("y", "c");
			tuple.put("z", "@");
			tmp.addTuple(tuple);
			tuple.clear();
			tuple.put("y", "a");
			tuple.put("z", "@");
			tmp.addTuple(tuple);
			tuple.clear();
			tuple.put("y", "a");
			tuple.put("z", "#");
			tmp.addTuple(tuple);
			lattice.put("F",tmp);
		}
				
		R10 = buildR10();
		R11 = buildR11();
		lattice.put("R00",R00);
		lattice.put("R01",R01);
		lattice.put("R10",R10);
		lattice.put("R11",R11);
		
		lattice.put("((A ^ D) v (B ^ C))'",complement(lattice.get("(A ^ D) v (B ^ C)")));

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
	
	public Relation eval( String input ) {
		List<LexerToken> src =  LexerToken.parse(input);
		Matrix matrix = cyk.initArray1(src);
		int size = matrix.size();
		TreeMap<Integer,Integer> skipRanges = null;//new TreeMap<Integer,Integer>();
		cyk.closure(matrix, 0, size+1, skipRanges, -1);
		ParseNode root = cyk.forest(size, matrix);
		return eval(root,src);
	}
	private Relation eval( ParseNode node, List<LexerToken> src ) {
		if( node.from + 1 == node.to )
			return lattice.get(src.get(node.from).content);
		
		Relation x = null;
		Relation y = null;
		int oper = -1;
		boolean parenGroup = false;
		for( ParseNode child : node.children() ) {
			if( parenGroup )
				return eval(child, src);
			else if( child.contains(openParen) )
				parenGroup = true;
			else if( child.contains(expr) ) {
				if( x == null )
					x = eval(child,src);
				else
					y = eval(child,src);
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
	
	public boolean assertion( ParseNode root, List<LexerToken> src ) {
		if( !root.contains(assertion) ) 
			throw new RuntimeException("Syntax Error in "+root.content(src));
		for( ParseNode child : root.children() ) {
			return equation(child,src);
		}
		return false;
	}
	public boolean equation( ParseNode root, List<LexerToken> src ) {
		Relation left = null;
		Relation right = null;
		for( ParseNode child : root.children() ) {
			if( left == null )
				left = eval(child,src);
			else if( child.contains(eq) )
				;
			else 				
				right = eval(child,src);
		}
		return left.equals(right);
	}
	public boolean axiomSystem( ParseNode root, List<LexerToken> src ) {
		for( ParseNode child : root.children() ) {
				
			String[] relVars = lattice.keySet().toArray(new String[0]);
			for( String xName : relVars )
				for( String yName : relVars )
					for( String zName : relVars ) {
						Relation x = lattice.get(xName);
						Relation y = lattice.get(yName);
						Relation z = lattice.get(zName);
						
						lattice.put("x", x);
						lattice.put("y", y);
						lattice.put("z", z);
						if( !assertion(child,src) ) {
							System.out.println("x = "+x);
							System.out.println("y = "+y);
							System.out.println("z = "+z);
							System.out.println(child.content(src));
							return false;
						}
						lattice.remove("x");
						lattice.remove("y");
						lattice.remove("z");
					}
		}
		return true;
	}
	
	static CYK cyk;
	static int join;
	static int innerJoin;
	static int innerUnion;
	static int outerUnion;
	static int complement;
	static int eq;
	static int expr;
	static int openParen;
	static int equation;
	static int assertion;
	static int axiomSystem;
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
			expr = cyk.symbolIndexes.get("expr");
			openParen = cyk.symbolIndexes.get("'('");
			equation = cyk.symbolIndexes.get("equation");
			assertion = cyk.symbolIndexes.get("assertion");
			axiomSystem = cyk.symbolIndexes.get("axiomSystem");
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}

	public static void main( String[] args ) throws Exception {
		/*Relation vx = eval("(A ^ D) v (B ^ C)");
		lattice.put("x", vx);
		
		System.out.println("x="+vx);
		System.out.println("R01 * x ="+eval("R01 * x"));
		System.out.println("x' ="+eval("x'"));
		System.out.println("(R01 * x) * x' ="+eval("(R01 * x) * x'"));
		System.out.println("x * x' ="+eval("x * x'"));
		System.out.println("R01 * (x * x') ="+eval("R01 * (x * x')"));
		
		System.out.println("----------------------------");		
		*/
		Model model = new Model();
		/*
		System.out.println("E ="+model.eval("E"));
		System.out.println("E' ="+model.eval("E'"));
		System.out.println("E' ^ E ="+model.eval("E' ^ E"));
		*/
		//if( true )
			//return;
		
		String axioms = Util.readFile(Model.class,"/qbql/lattice/bilattice.axioms");
		
		List<LexerToken> src =  LexerToken.parse(axioms);
		Matrix matrix = cyk.initArray1(src);
		int size = matrix.size();
		TreeMap<Integer,Integer> skipRanges = new TreeMap<Integer,Integer>();
		cyk.closure(matrix, 0, size+1, skipRanges, -1);
		ParseNode root = cyk.forest(size, matrix);
						
		if( !model.axiomSystem(root,src) )
			System.out.println("*** Inconsistent Axiom System ***");
		else
			System.out.println("Axiom System is OK");
	}
}
