package qbql.parser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import qbql.lattice.Relation;
import qbql.util.Util;


	/*
	 * Optimized version of CYK with ints insted of Strings
	 * 
	 * 	http://en.wikipedia.org/wiki/CYK_algorithm
	 *  
		Let the input string consist of n letters, a1 ... an.
		Let the grammar contain r terminal and nonterminal symbols R1 ... Rr. 
		This grammar contains the subset Rs which is the set of start symbols.
		
		------------------------ initPArray() ----------------------------
		Let P[n,n,r] be an array of booleans. Initialize all elements of P to false.
		For each i = 1 to n
		  For each unit production Rj : ai, set P[i,1,j] = true.
		  
		------------------------ parse() ----------------------------
		For each i = 2 to n -- Length of span
		  For each j = 1 to n-i+1 -- Start of span
		    For each k = 1 to i-1 -- Partition of span
		      For each production RA -> RB RC
		        If P[j,k,B] and P[j+k,i-k,C] then set P[j,i,A] = true
		        
		------------------------ final test --------------------------        
		If any of P[1,n,x] is true (x is iterated over the set s, where s are all the indices for Rs)
		  Then string is member of language
		  Else string is not member of language
		  
		  Amendments: 
		  1. Switched to using standard interval convention [i,j+1)
		  2. Allow single RHS productions
	 	  3. Boolean arrays are compressed into arrays of integer bitmaps
	         when larger than a certain threshold, see: BoolArray
	         Being sparse, they were subsequently reduced to sets. 

	 */
public class CYK {

	public ChomskiTuple[] rules;
	//static Set<RuleTuple> originalRules; 

	// indexes
	public Set<Integer>[] singleRhsRules;

	Proj[] doubleRhsRules;

	public String[] allSymbols;  
	public Map<String,Integer> symbolIndexes;

	Set<Integer> keywords = new TreeSet<Integer>(); // pure Keywords

	//static boolean debug = true;
	public static void main( String[] dummy ) throws Exception {
		CYK cyk = new CYK(Relation.getRules());
		cyk.printSelectedChomskiRules("nion");		
		final String input = 
			"R01 = R00 -> R01 = R00.";
		//Util.readFile("c:/raptor_trunk/db/src/oracle/dbtools/parser/test.sql")
		;
		long t1 = System.currentTimeMillis();
		List<LexerToken> src =  LexerToken.parse(input);
		long t2 = System.currentTimeMillis();
		System.out.println("Lexer time = "+(t2-t1)); // (authorized)
		//LexerToken.print(src);

		long h = Runtime.getRuntime().totalMemory();
		long hf = Runtime.getRuntime().freeMemory();
		System.out.println("mem="+(h-hf)); // (authorized)
		t1 = System.currentTimeMillis();
		Matrix matrix = cyk.initArray1(src);
		t2 = System.currentTimeMillis();
		System.out.println("Init array time = "+(t2-t1)); // (authorized)

		int size = matrix.size();
		System.out.println("size = "+size); // (authorized)
		TreeMap<Integer,Integer> skipRanges = new TreeMap<Integer,Integer>();
		t1 = System.currentTimeMillis();
		cyk.closure(matrix, 0, size+1, skipRanges, -1);
		t2 = System.currentTimeMillis();
		System.out.println("Parse time = "+(t2-t1)); // (authorized)
		System.out.println(skipRanges);// (authorized)
		cyk.print(matrix, 0, size);
		System.out.println("^^^^^^^^^^^^^"); // (authorized)
		int x = 19; int y = 23;
		//cyk.print(matrix, 0, 1);

		t1 = System.currentTimeMillis();
		ParseNode root = cyk.forest(size, matrix);
		t2 = System.currentTimeMillis();
		System.out.println("Reduction time = "+(t2-t1)); // (authorized)
		h = Runtime.getRuntime().totalMemory();
		hf = Runtime.getRuntime().freeMemory();
		System.out.println("mem="+(h-hf)); // (authorized)

		root.printTree();
		//root.printBinaryTree(0);				
	}

	public CYK( Set<RuleTuple> originalRules ) {
		rules = getChomskyRules(originalRules);
		singleRhsRules = filterSingleRhsRules();
		doubleRhsRules = filterDoubleRhsRules();
	}


	public void printSelectedChomskiRules( String name ) {
		System.out.println("-------------Chomsky Rules---------------"); // (authorized)		
		for( ChomskiTuple rule : rules ) 
			if( allSymbols[rule.head].contains(name) //>=0 
					||	allSymbols[rule.rhs0].contains(name)
					||	rule.rhs1>0 && allSymbols[rule.rhs1].contains(name)
			)
				System.out.println(rule.toString()); // (authorized)
		System.out.println("-------------------------------------"); // (authorized)
	}
	public  void printIds() {
		System.out.println("-------------Id Rules---------------"); // (authorized)		
		for( ChomskiTuple rule : rules ) 
			for( int i : singleRhsRules[symbolIndexes.get("digits")] )
				if( rule.head == i )
					System.out.println(rule.toString()); // (authorized)
		System.out.println("-------------------------------------"); // (authorized)
	}

	public Matrix initArray( List<LexerToken> input ) {
		Matrix ret = new Matrix(this);	

		int i = 0;
		for( LexerToken token : input ) {
			initArrayElement(ret, i, token, false);
			i++;
		}

		return ret;
	}
	public Matrix initArray1( List<LexerToken> input ) {
		Matrix ret = new Matrix(this);	

		int i = 0;
		for( LexerToken token : input ) {
			initArrayElement(ret, i, token, true);
			i++;
		}

		return ret;
	}

	public void initArrayElement(Matrix ret, int pos, LexerToken token, boolean identifiersOnly) {
		/*if( token.type == RegexprBasedLexer.WS ) // this is done as part of parsing
				continue;
			if( token.type == RegexprBasedLexer.COMMENT ) 
				continue;
			if( token.type == RegexprBasedLexer.LINE_COMMENT ) 
				continue;
		 */
		Integer suspect = symbolIndexes.get("'" + token.content + "'");
		Set<Integer> dependents = new TreeSet<Integer>();
		if( suspect != null ) {
			dependents.addAll(singleRhsRules[suspect]);
		} 
		if( token.type == Token.IDENTIFIER ) {
			if( !identifiersOnly || suspect == null || !keywords.contains(suspect) ) { 
				int symbol = symbolIndexes.get("identifier");
				dependents.addAll(singleRhsRules[symbol]);
			}
		} else if( token.type == Token.DQUOTED_STRING || token.type == Token.QUOTED_STRING ) {
			int symbol = symbolIndexes.get("string_literal");
			dependents.addAll(singleRhsRules[symbol]);
		} else if( token.type == Token.DIGITS ) {
			int symbol = symbolIndexes.get("digits");
			dependents.addAll(singleRhsRules[symbol]);
		}
		int[] tmp = new int[dependents.size()];
		int i = 0;
		for( int e : dependents )
			tmp[i++] = Util.pair(e, pos);
		ret.put(Util.pair(pos,pos+1), tmp);
	}
	public void initArrayElement(SortedMap<Integer, Set<Integer>> ret, int pos, int symbol) {
		Set<Integer> dependents = new TreeSet<Integer>();
		dependents.addAll(singleRhsRules[symbol]);
		ret.put(Util.pair(pos,pos+1), dependents);
	}

	private boolean containsSymbol( int[] cellContent, int symbol ) {
		for( int sb : cellContent )
			if( symbol==Util.X(sb) )
				return true;

		return false;
	}
	/*private final int maxKwdIdx;
	private boolean containsKwd( int[] cellContent ) {
		for( int sb : cellContent )
			if( Util.X(sb)<=maxKwdIdx )
				return true;

		return false;
	}*/

	/**
	 * If we derived some pretty "complex" grammar symbol (such as statement, or axiom),
	 * then there is no need to look inside the interval when applying the main loop of the CYK method.
	 */
	public int[] atomicSymbols() {
		return new int[0];
	}
	/**
	 * The main evaluation loop of the CYK method (see doc)
	 * P - main matrix which diagonal is filled in by the initArray() method 
	 * len - a redundant size of the matrix
	 * skipPoints and finalSymbols -- optimization parameters
	 * if we deduced that interval [x,y) is "(subquery)"
	 * then closure would work faster if we never look inside this interval
	 * when joining other intervals
	 */
	public void closure( Matrix matrix, int from, int to,
			Map<Integer,Integer> skipRanges,   // optimization
			int middle                         // == -1 for parsing in ordinary context
			// != -1 if have auxiliary symbols in the middle
	) {

		for( int y = 1; y < to; y++ ) {
			for( int x = y-2; x >= from; x-- ) {

				if( skipRanges != null ) {
					Integer nextX = skipRanges.get(x);
					if( nextX != null ) {

						/*if( Visual.skipped!=null )
							for(int i = x; i>nextX; i--)
								Visual.skipped[i][y] = true;*/

						x = nextX;
						continue;
					}
				}
				int start = Util.pair(x,y);
				int end = Util.pair(0,y+1);
				Set<Integer> tmp = new TreeSet<Integer>();
				SortedMap<Integer,int[]> range = matrix.subMap(start, end);
				for( int key : range.keySet() ) {
					int mid = Util.X(key);
					int[] prefixes = matrix.get(Util.pair(x,mid));
					if( prefixes==null )
						continue;
					int[] suffixes = matrix.get(Util.pair(mid,y));
					if( suffixes==null )
						continue;

					for( int II : prefixes )    // Not indexed Nested Loops
						for( int JJ : suffixes ) {
							int I = Util.X(II);
							int J = Util.X(JJ);
							Proj p = doubleRhsRules[I];
							if( p==null )
								continue;
							Set<Integer> A = p.values[J];
							if( A==null )
								continue;
							List<Integer> B = new LinkedList<Integer>();
							for( int a : A )
								B.add(Util.pair(a, mid));
							tmp.addAll(B);
						}										

				}
				if( tmp.size()>0 ) {
					int[] tmp1 = new int[tmp.size()];
					int i = 0;
					for( int e : tmp )
						tmp1[i++] = e;
					matrix.put(Util.pair(x,y),tmp1);
					int[] atomicSymbols = atomicSymbols();
					if( skipRanges != null && atomicSymbols().length > 0 
							&& y-1 != x+1 // actually even though y-1 == x+1 there would still be x skipped
							&& !(x <= middle && middle < y)
					)
			checkIfAtomic:
						for( int ss : tmp1 ) {
							int s = Util.X(ss);
							for( int skipRangeSymbol : atomicSymbols )
								if( s==skipRangeSymbol ) {
									skipRanges.put(y-1,x+1);
									break checkIfAtomic;
								}
						}

				} 
			} 
		}

	}

	/**
	 * @param matrix
	 * @param skipRanges
	 * @param [x,y) is the current interval
	 */
	private int splitInterval( Matrix matrix, int x, int y, int symbol, boolean leftDirection ) {
		for( int i = leftDirection?x+1:y-1; x<i && i<y; i=leftDirection?i+1:i-1 ) {
			int[] tmpIXsymbols = matrix.get(Util.pair(x,i));
			if( tmpIXsymbols == null )
				continue;
			int[] tmpIYsymbols = matrix.get(Util.pair(i,y));
			if( tmpIYsymbols == null )
				continue;
			for( int tmp : tmpIXsymbols ) {
				if( Util.X(tmp) == symbol ) {
					for( int tmp2 : tmpIYsymbols ) {
						if( Util.X(tmp2) == symbol ) {
							return i;
						}											
					}									
				}											
			}									
		}
		return -1;	
	}



	public void print( Matrix P ) {
		for( int xy : P.keySet() ) {
			int i = Util.X(xy);
			int j = Util.Y(xy);
			print(P, i, j);
		}
	}
	public void print( Matrix P, int i, int j ) {
		System.out.print("["+i+","+j+")"); // (authorized)
		int[] output = P.get(Util.pair(i, j));
		if( output ==  null ) {
			System.out.println("- syntactically invalid code fragment"); // (authorized)
			return;
		}
		for( int kk : output ) {
			int k = Util.X(kk);
			if( k == -1 )
				System.out.print("''"); // (authorized)
			else	
				System.out.print("  "+allSymbols[k]); // (authorized)
		}
		System.out.println(); // (authorized)
	}

	Set<Integer>[] filterSingleRhsRules() {
		Map<Integer,Set<Integer>> tmp = new TreeMap<Integer,Set<Integer>>();
		// identity
		for( int i = 0; i < allSymbols.length; i++ ) {
			Set ii = new TreeSet<Integer>();
			ii.add(i);
			tmp.put(i, ii);
		}
		for( ChomskiTuple rule : rules ) {
			if( rule.rhs1==-1 ) {
				Set<Integer> headers = tmp.get(rule.rhs0);
				headers.add(rule.head);
				tmp.put(rule.rhs0, headers);
			} 
		}

		boolean grown = true;
		Map<Integer,Set<Integer>> incrementedRet = new TreeMap<Integer,Set<Integer>>();;
		while( grown ) {
			grown = false;
			for( int i : tmp.keySet() ) {
				Set<Integer> set = new TreeSet<Integer>();
				set.addAll(tmp.get(i));
				for( int j : tmp.get(i) ) {
					Set<Integer> t1 = tmp.get(j);
					if( t1 != null )
						set.addAll(t1);
				}
				if( set.size() > tmp.get(i).size() ) {
					grown = true;
				}
				incrementedRet.put(i, set);
			}
			tmp = incrementedRet;
		} 

		Set<Integer>[] ret = new Set[allSymbols.length];
		for( int i : tmp.keySet() )
			ret[i] = tmp.get(i);

		return ret;
	}

	Proj[] filterDoubleRhsRules() {
		Proj[] ret = new Proj[allSymbols.length];
		for( ChomskiTuple rule : rules ) {
			if( rule.rhs1!=-1 ) {
				if( ret[rule.rhs0] == null )
					ret[rule.rhs0] = new Proj();
				Set<Integer> headers = ret[rule.rhs0].values[rule.rhs1];
				if( headers == null )
					headers = new TreeSet<Integer>();
				headers.addAll(singleRhsRules[rule.head]);
				ret[rule.rhs0].values[rule.rhs1]= headers;

			}
		}

		return ret;
	}


	protected ChomskiTuple[] getChomskyRules( Set<RuleTuple> input ) {
		Set<RuleTuple> nonEmptyRules = null;
		Set<RuleTuple> tmp = input;
		do {
			nonEmptyRules = tmp;
			tmp = eliminateEmptyProduction(nonEmptyRules);
		} while( tmp!=null );

		return convertToChomskyRules(nonEmptyRules);
	}


	/*
	 * This procedure eliminates an empty symbol, but produces some others!
	 */
	Set<RuleTuple> eliminateEmptyProduction( Set<RuleTuple> rules ) {
		Set<RuleTuple> ret = new TreeSet<RuleTuple>();
		String emptyHeader = null;
		for( RuleTuple rule : rules ) {
			if( rule.rhs.length==0 ) {
				emptyHeader = rule.head;
				break;
			}
		}
		if( emptyHeader==null )
			return null;
		for( RuleTuple rule : rules ) {
			if( rule.head.equals(emptyHeader) && rule.rhs.length==0 )
				continue;
			ret.add(rule);
			int countEmptySymbols = 0;
			for( int i = 0; i < rule.rhs.length; i++  ) {
				if( emptyHeader.equals(rule.rhs[i]) ) {
					countEmptySymbols++;
				}
			}
			if( countEmptySymbols ==1 ) {
				for( int i = 0; i < rule.rhs.length; i++  ) {
					if( emptyHeader.equals(rule.rhs[i]) ) {
						String[] rhs = new String[rule.rhs.length-1];
						for( int j = 0; j < rule.rhs.length-1; j++ )
							if( j < i )
								rhs[j]=rule.rhs[j];
							else if( j >= i )
								rhs[j]=rule.rhs[j+1];
						ret.add( new RuleTuple(rule.head,rhs) );
					}
				}
			} else if( countEmptySymbols == 2 ) {
				if( rule.rhs.length==2 )  // otherwise, infinite loop on rules like this: "var: var var;"
					continue;
				String[] rhs01 = new String[rule.rhs.length-1];
				String[] rhs10 = new String[rule.rhs.length-1];
				String[] rhs11 = new String[rule.rhs.length-2];
				int bit = 0;
				for( int i = 0; i < rule.rhs.length; i++  ) {
					if( emptyHeader.equals(rule.rhs[i]) ) {
						if( bit==0 )
							rhs10[i] = rule.rhs[i];
						else if( bit==1 )
							rhs01[i-1] = rule.rhs[i];
						bit++;
						continue;
					}
					rhs01[i-(bit>0?1:0)] = rule.rhs[i];
					rhs10[i-(bit>1?1:0)] = rule.rhs[i];
					rhs11[i-bit] = rule.rhs[i];
				}
				ret.add( new RuleTuple(rule.head,rhs01) );
				ret.add( new RuleTuple(rule.head,rhs10) );
				ret.add( new RuleTuple(rule.head,rhs11) );
			} else if( countEmptySymbols > 2 )
				throw new RuntimeException("countEmptySymbols > 2 "+rule.toString());
		}
		return ret;
	}


	private ChomskiTuple[] convertToChomskyRules( Set<RuleTuple> rules ) {
		Set<RuleTuple> tmp = extractBinaryRules(rules);

		ChomskiTuple[] ret = new ChomskiTuple[tmp.size()];

		Set<String> tmpSymbols = new TreeSet<String>();
		int i = 0;
		for( RuleTuple ct : tmp ) {
			if( ct.head==null || ct.rhs[0]==null || ct.rhs.length>1 && ct.rhs[1]==null )
				throw new RuntimeException("ct has null symbols");
			tmpSymbols.add(ct.head);
			tmpSymbols.add(ct.rhs[0]);
			if( ct.rhs.length > 1 ) 
				tmpSymbols.add(ct.rhs[1]);
			if( ct.rhs.length > 2 ) 
				throw new RuntimeException("ct.rhs.length > 2");
		}

		// add grammar symbols according to some heuristic order
		// generally want to see "more complete" parse trees 
		allSymbols = new String[tmpSymbols.size()+1];
		symbolIndexes = new TreeMap<String,Integer>();
		int k = 0;
		if( tmpSymbols.contains("exec") ) {
			symbolIndexes.put("exec", k);
			allSymbols[k]="exec";
			tmpSymbols.remove("exec");
			k++;
		}

		Set<String> added = new TreeSet<String>();
		for( String s : tmpSymbols ) {
			if( s.contains("+") || s.charAt(0)=='.' ) 
				continue;
			symbolIndexes.put(s, k);
			allSymbols[k]=s;
			added.add(s);
			k++;
		}
		tmpSymbols.removeAll(added);

		added = new TreeSet<String>();
		for( String s : tmpSymbols ) {
			if( s.contains("+") ) 
				continue;
			symbolIndexes.put(s, k);
			allSymbols[k]=s;
			added.add(s);
			k++;
		}
		tmpSymbols.removeAll(added);

		added = new TreeSet<String>();
		for( String s : tmpSymbols ) {
			symbolIndexes.put(s, k);
			allSymbols[k]=s;
			added.add(s);
			k++;
		}

		tmpSymbols.removeAll(added);
		symbolIndexes.put("identifier", k);
		allSymbols[k]="identifier";
		k++;

		i = 0;
		for( RuleTuple ct : tmp ) {
			ret[i++] = new ChomskiTuple(
					symbolIndexes.get(ct.head),
					symbolIndexes.get(ct.rhs[0]),
					ct.rhs.length>1 ? symbolIndexes.get(ct.rhs[1]) : -1
			);
		}


		return ret;
	}


	Set<RuleTuple> split( RuleTuple rule ) {
		Set<RuleTuple> tmp = new TreeSet<RuleTuple>();
		if( rule.rhs.length == 0 ) 
			throw new RuntimeException("Empty Rule!");
		else if( rule.rhs.length == 1 || rule.rhs.length == 2 ) {
			tmp.add(rule);
		} else {
			int cut = rule.rhs.length / 2;		

			List<String> rhs = new ArrayList<String>();
			StringBuffer ruleNameL = new StringBuffer();
			for( int i = 0; i < cut; i++ ) {
				rhs.add(rule.rhs[i]);
				if( i > 0)
					ruleNameL.append('+');
				ruleNameL.append(rule.rhs[i]);
			}
			if( cut > 1 )
				tmp.addAll(split(new RuleTuple(ruleNameL.toString(),rhs)));
			rhs = new ArrayList<String>();
			StringBuffer ruleNameR = new StringBuffer();
			for( int i = cut; i < rule.rhs.length; i++ ) {
				rhs.add(rule.rhs[i]);
				if( i > cut)
					ruleNameR.append('+');
				ruleNameR.append(rule.rhs[i]);
			}
			if( cut < rule.rhs.length )
				tmp.addAll(split(new RuleTuple(ruleNameR.toString(),rhs)));
			tmp.add(new RuleTuple(rule.head, new String[] {ruleNameL.toString(),ruleNameR.toString()}));
		}
		return tmp;
	}

	private Set<RuleTuple> extractBinaryRules( Set<RuleTuple> rules ) {
		Set<RuleTuple> tmp = new TreeSet<RuleTuple>();
		for( RuleTuple rule : rules ) {			
			tmp.addAll( split(rule) ); 			
		}
		return tmp;
	}

	void unitTest() {
		Set<RuleTuple> tmp = new TreeSet<RuleTuple>();
		/*tmp.add(new RuleTuple("e",new String[] {"e","'/'","e"}));
			tmp.add(new RuleTuple("e",new String[] {"e","'*'","e"}));
			tmp.add(new RuleTuple("e",new String[] {"identifier"}));
		 */
		tmp.add(new RuleTuple(".else.",new String[] {"'ELSE'"}));
		tmp.add(new RuleTuple(".else.",new String[] {}));
		tmp.add(new RuleTuple("e",new String[] {"'IF'","'THEN'",".else.","'END'"}));

		for( RuleTuple rule : tmp ) 
			System.out.println(rule.toString()); // (authorized)
		System.out.println("-------------------------------------"); // (authorized)		

		tmp = eliminateEmptyProduction(tmp);

		for( RuleTuple rule : tmp ) 
			System.out.println(rule.toString()); // (authorized)
		System.out.println("======================================"); // (authorized)

		rules = getChomskyRules(tmp);		
		singleRhsRules = filterSingleRhsRules();
		doubleRhsRules = filterDoubleRhsRules();

		for( String s : allSymbols ) 
			System.out.println(s); // (authorized)

		System.out.println("+++++++++++++++++++++++++++++++++++++"); // (authorized)

		for( ChomskiTuple rule : rules ) 
			System.out.println(rule.toString()); // (authorized)
		System.out.println("-------------------------------------"); // (authorized)		

	}

	public class ChomskiTuple implements Comparable {
		public int head;
		public int rhs0;   
		public int rhs1;   
		public ChomskiTuple( int h, int r0, int r1 ) {
			head = h;
			rhs0 = r0;
			rhs1 = r1;
		}
		public boolean equals(Object obj) {
			return compareTo(obj)==0;
		}
		public int hashCode() {
			throw new RuntimeException("hashCode inconssitent with equals"); 
		}		
		public int compareTo(Object obj) {
			ChomskiTuple src = (ChomskiTuple)obj;
			if( head==0 || src.head==0 )
				throw new RuntimeException("head==0 || src.head==0");
			int cmp = head-src.head;
			if( cmp!=0 )
				return cmp;
			cmp = rhs0-src.rhs0;
			if( cmp!=0 )
				return cmp;			
			return  rhs1-src.rhs1;
		}
		public String toString() {
			if( rhs1==-1 )
				return allSymbols[head]+": "+allSymbols[rhs0]+";";
			return allSymbols[head]+": "+allSymbols[rhs0]+"  "+allSymbols[rhs1]+";";
		}
	}

	// This class is artefact of large memory footprint (12.9M) of the 
	// [allSymbols.length][allSymbols.length] array which is sparse
	// Just converting uniform array into ragged array saves 40% of the space
	//
	// Can optimize it further (because values) is sparse array too
	// But have to be careful to keep the access to the elements fast
	// CYK performace is critical of it!
	class Proj {
		Set<Integer>[] values = new Set[allSymbols.length];
	}

	//////////////////////// Parse Tree ///////////////////////////
	/**
	 * @param begin
	 * @param end
	 * @param symbolSplit -- Util.pair(symbol, middle)
	 * @param backPtr
	 * @return
	 */
	public ParseNode parseInterval( int begin, int end, int symbolSplit,
			Matrix backPtr
	) {
		int symbol = Util.X(symbolSplit);
		if( begin+1 == end ) {
			return new ParseNode(begin, end, symbol, symbol, this);
		}
		int mid = Util.Y(symbolSplit);
		int[] pres = backPtr.get(Util.pair(begin,mid));
		if( pres == null )
			return null;
		int[] posts = backPtr.get(Util.pair(mid,end));
		if( posts == null )
			return null;
		for( int pre : pres ) {
			for( int post : posts ) {
				int s1 = Util.X(pre);
				int s2 = Util.X(post);
				Proj p = doubleRhsRules[s1];
				if( p==null )
					continue;
				Set<Integer> A = p.values[s2];
				if( A==null )
					continue;				
				if( A.contains(symbol) ) {
					ParseNode ret = null;
					for( ChomskiTuple t : rules )
						if( t.rhs0 == s1 && t.rhs1 == s2 && singleRhsRules[t.head].contains(symbol) ) {
							ret = new ParseNode(begin,end,t.head, -1, this);
							ret.lft = parseInterval(begin,mid, pre, backPtr);
							if( ret.lft == null )
								continue;
							ret.lft.payloadOut = t.rhs0;
							ret.rgt = parseInterval(mid,end, post, backPtr);
							if( ret.rgt == null )
								continue;
							ret.rgt.payloadOut = t.rhs1;
							return ret;
						}
				}
			}
		}
		return null;
	}



	/**
	 * How to process Parsing Errors:
	 * If the parser fails to derive the correct tree, let's
	 * return a forest   
	 */
	public ParseNode forest( int len, 
			Matrix backPtr
	) {
		if( backPtr.get(Util.pair(0, len)) != null ) { // special case
			ParseNode ret = null;
			int[] sms = backPtr.get(Util.pair(0, len));
			for( int sm : sms ) {
				ret = parseInterval(0,len, sm,backPtr);
				if( ret != null )
					return ret;					
			}
		}	

		List<Integer> cover = new ArrayList<Integer>();
		for( int key :backPtr.keySet() ) {
			List<Integer> nodes = new ArrayList<Integer>();
			boolean alreadyCovered = false;
			for( int n : cover ) {
				if( Util.X(key)<=Util.X(n) && Util.Y(key)>Util.Y(n)  
						|| Util.X(key)<Util.X(n) && Util.Y(key)>=Util.Y(n)) {
					nodes.add(n);
				}
				if( Util.X(key)>=Util.X(n) && Util.Y(key)<Util.Y(n)  
						|| Util.X(key)>Util.X(n) && Util.Y(key)<=Util.Y(n)) {
					alreadyCovered = true;
					break;
				}
			}
			for( Integer x : nodes )
				cover.remove(x);
			if( !alreadyCovered )
				cover.add(key);

		}
		Set<ParseNode> topLevelNodes = new TreeSet<ParseNode>();		
		for( Integer n : cover ) {
			ParseNode ret = null;
			int[] sms = backPtr.get(n);
			if( sms != null ) {
				for( int sm : sms ) {
					ret = parseInterval(Util.X(n),Util.Y(n), sm,backPtr);
					if( ret != null ) {
						topLevelNodes.add(ret);
						break;
					}
				}
			}
		}
		ParseNode pseudoRoot = new ParseNode(0,len, -1,-1, this);
		pseudoRoot.topLevel = topLevelNodes;
		return pseudoRoot;

	}

	public static int[] toArray( Set<Integer> s ) {
		int[] ret = new int[s.size()];
		int i = 0;
		for( int ii : s )
			ret[i++] = ii;
		return ret;
	}

}




