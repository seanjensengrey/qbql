package qbql.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import qbql.lattice.Program;
import qbql.program.Run;
import qbql.util.Array;
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
public class CYK extends Parser {


    //public Proj[] doubleRhsRules;
    public HashMap<Integer,int[]> doubleRhsRules;

    Set<Integer> keywords = new TreeSet<Integer>(); // pure Keywords

    public CYK( Set<RuleTuple> originalRules ) {
        super(extractBinaryRules(originalRules));
        rules = getChomskyRules(originalRules);
        singleRhsRules = filterSingleRhsRules();
        doubleRhsRules = filterDoubleRhsRules();
    }

    public ChomskiTuple[] rules;

    public void printSelectedChomskiRules( String name ) {
        System.out.println("-------------Chomsky Rules---------------"); // (authorized)                 //$NON-NLS-1$
        for( ChomskiTuple rule : rules )
            if( allSymbols[rule.head].contains(name) //>=0
                    ||  allSymbols[rule.rhs0].contains(name)
                    ||  rule.rhs1>0 && allSymbols[rule.rhs1].contains(name)
            )
                System.out.println(rule.toString()); // (authorized)
        System.out.println("-------------------------------------"); // (authorized) //$NON-NLS-1$
    }
    public  void printIds() {
        System.out.println("-------------Id Rules---------------"); // (authorized)             //$NON-NLS-1$
        for( ChomskiTuple rule : rules )
            for( int i : singleRhsRules[symbolIndexes.get("digits")] ) //$NON-NLS-1$
                if( rule.head == i )
                    System.out.println(rule.toString()); // (authorized)
        System.out.println("-------------------------------------"); // (authorized) //$NON-NLS-1$
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

        initArray(input, ret);

        return ret;
    }
    public void initArray( List<LexerToken> input, Matrix ret ) {
        int i = 0;
        for( LexerToken token : input ) {
            initArrayElement(ret, i, token, true);
            i++;
        }
    }

    public void initArrayElement( Matrix ret, int pos, LexerToken token, boolean identifiersOnly ) {
        Integer suspect = symbolIndexes.get("'" + token.content + "'"); //$NON-NLS-1$ //$NON-NLS-2$
        Set<Integer> dependents = new TreeSet<Integer>();
        if( suspect != null ) {
            dependents.addAll(singleRhsRules[suspect]);
        }
        if( token.type == Token.IDENTIFIER ) {
            if( !identifiersOnly || suspect == null || !keywords.contains(suspect) ) {
                int symbol = symbolIndexes.get("identifier"); //$NON-NLS-1$
                dependents.addAll(singleRhsRules[symbol]);
            }
        } else if( token.type == Token.DQUOTED_STRING || token.type == Token.QUOTED_STRING ) {
            int symbol = symbolIndexes.get("string_literal"); //$NON-NLS-1$
            dependents.addAll(singleRhsRules[symbol]);
        } else if( token.type == Token.DIGITS ) {
            int symbol = symbolIndexes.get("digits"); //$NON-NLS-1$
            dependents.addAll(singleRhsRules[symbol]);
        }
        int[] tmp = new int[dependents.size()];
        int i = 0;
        for( int e : dependents )
            tmp[i++] = e;
        ret.put(Util.pair(pos,pos+1), new CykCell(tmp));
    }
    /*public void initArrayElement(SortedMap<Integer, Set<Integer>> ret, int pos, int symbol) {
        Set<Integer> dependents = new TreeSet<Integer>();
        dependents.addAll(singleRhsRules[symbol]);
        ret.put(Util.pair(pos,pos+1), dependents);
    }*/


    /**
     * If we derived some pretty "complex" grammar symbol (such as statement, or axiom),
     * then there is no need to look inside the interval when applying the main loop of the CYK method.
     */
    public int[] atomicSymbols() {
        return new int[0];
    }
    /**
     * Little more conservative optimization [x1,y1),[x2,y2),[x3,y3) -> don't look inside [x2,y2)
     */
    public Map<Integer,Integer> delimitedSymbols() {
        return new HashMap<Integer,Integer>();
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
        final int[] atomicSymbols = atomicSymbols();
        final Map<Integer, Integer> delimitedSymbols = delimitedSymbols();

        for( int y = 1; y < to; y++ ) {
            for( int x = y-2; x >= from; x-- ) {

                if( skipRanges != null ) {
                    Integer nextX = skipRanges.get(x);
                    if( nextX != null ) {

                        if( Visual.skipped!=null )  // skipped semi-open intervals oriented the other way!
                            for( int i = x; i > nextX ; i-- )
                            	Visual.skipped[i][y] = Visual.causes.get(Util.pair(x, nextX));

                        x = nextX+1;   // it would be decremented at the end of the loop
                        continue;
                    }
                }
                int start = Util.pair(x,y);
                int end = Util.pair(0,y+1);
                int[] tmp = null;
                SortedMap<Integer,Cell> range = matrix.subMap(start, end);
//System.out.println("["+x+","+y+")  "+range.keySet());
                for( int key : range.keySet() ) {
                    int mid = Util.X(key);
                    Cell prefixes = matrix.get(Util.pair(x,mid));
                    if( prefixes==null )
                        continue;
                    Cell suffixes = matrix.get(Util.pair(mid,y));
                    if( suffixes==null )
                        continue;

                    for( int I : prefixes.getContent() )    // Not indexed Nested Loops
                        for( int J : suffixes.getContent() ) {
                            int[] A = doubleRhsRules.get(Util.pair(I, J));

                            if( A==null )
                                continue;
                            
                            tmp = Array.merge(tmp, A);
                        }                                                                              

                }
                if( tmp != null ) {
                    matrix.put(Util.pair(x,y),new CykCell(tmp));
                    if( skipRanges != null && (atomicSymbols.length > 0 || delimitedSymbols.size() > 0 )
                            && y-1 != x+1 // actually even though y-1 == x+1 there would still be x skipped
                            && !(x <= middle && middle < y)
                    )
                        checkIfAtomic:
                            for( int s : tmp ) {
                                for( int skipRangeSymbol : atomicSymbols )
                                    if( s==skipRangeSymbol ) {
                                    	int X = x;
                                    	int Y = y-1;
                                        skipRanges.put(Y,X);
                                        if( Visual.skipped!=null ) {
                                        	if( Visual.causes.get(Util.pair(Y,X))!=null )
                                        		throw new AssertionError("Visual.causes.get(Util.pair(Y,X))!=null");
                                        	Visual.causes.put(Util.pair(Y,X), s);
                                        }
                                        break checkIfAtomic;
                                    }
                                
                               for( int delimitedSymbol : delimitedSymbols.keySet() )
                                    if( s==delimitedSymbol) {
                                        int X = splitInterval(matrix, x, y, delimitedSymbol, delimitedSymbols.get(s), true);
                                        if( X == -1 )
                                            continue;
                                        int Y = splitInterval(matrix, X, y, delimitedSymbol, delimitedSymbols.get(s), false);
                                        if( Y == -1 )
                                            continue;
                                        int iY = Y-1;
                                        int iX = X;
                                        if( iY <= iX ) 
                                            continue;
                                        skipRanges.put(iY,iX);
                                        if( Visual.skipped!=null ) {
                                        	//if( Visual.causes.get(Util.pair(Y,X))!=null )
                                        		//throw new AssertionError("Visual.causes.get(Util.pair(Y,X))!=null");
                                        	Visual.causes.put(Util.pair(iY,iX), s);
                                        }
                                        break;
                                    }

                            }

                }
            }
        }

    }

	protected int next( int i, Map<Integer, Integer> skipRanges ) {
		Integer ret = skipRanges.get(i-1);
		if( ret == null )
			return i;
		return next(ret, skipRanges);
	}


	/**
     * Recalculates matrix when removing a segment of tokens [posX,posY)
     * @param matrix
     * @param len -- length of the text
     */
    public void recalculateRectangle(
            Matrix matrix,
            Map<Integer,Integer> skipRanges   // optimization
            , int len, int posX, int posY   // if a single point, then posY = posX+1
    ) {
        if( skipRanges != null ) {
            Set<Integer> keys = skipRanges.keySet();
            Integer[] dummy = new Integer[keys.size()];
            for( Integer key : keys.toArray(dummy) )
                if( posY < key ) {
                    Integer value = skipRanges.get(key); 
                    skipRanges.remove(key);
                    skipRanges.put(key-posY+posX, value-posY+posX);
                }
        }
        for( int y = posY; y < len; y++ )
            for( int x = posX; x >= 0; x-- ) {


                if( y == x+1 )
                    continue;
                matrix.remove(Util.pair(x,y));
                if( skipRanges != null ) {
                    Integer nextX = skipRanges.get(x);
                    if( nextX != null ) {
                        x = nextX+1;   // it would be decremented as the end of the loop
                        continue;
                    }
                }
                int start = Util.pair(x,y);
                int end = Util.pair(0,y+1);
                int[] tmp = null;
                SortedMap<Integer,Cell> range = matrix.subMap(start, end);
                for( int key : range.keySet() ) {
                    int mid = Util.X(key);
                    Cell prefixes = matrix.get(Util.pair(x,mid));
                    if( prefixes==null )
                        continue;
                    Cell suffixes = matrix.get(Util.pair(mid,y));
                    if( suffixes==null )
                        continue;

                    for( int I : prefixes.getContent() )    // Not indexed Nested Loops
                        for( int J : suffixes.getContent() ) {
                            int[] A = doubleRhsRules.get(Util.pair(I, J));
                            if( A==null )
                                continue;
                            tmp = Array.merge(tmp, A);
                        }                                                                                       

                }
                if( tmp != null )
                    matrix.put(Util.pair(x,y),new CykCell(tmp));
            }
    }

    public void print( Matrix P ) {
        for( int xy : P.keySet() ) {
            int i = Util.X(xy);
            int j = Util.Y(xy);
            print(P, i, j);
        }
    }
    public void print( Matrix P, int i, int j ) {
        System.out.print("["+i+","+j+")"); // (authorized) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        Cell output = P.get(Util.pair(i, j));
        if( output ==  null ) {
            System.out.println("- syntactically invalid code fragment"); // (authorized) //$NON-NLS-1$
            return;
        }
        for( int k : output.getContent() ) {
            if( k == -1 )
                System.out.print("-1"); // (authorized) //$NON-NLS-1$
            else        
                System.out.print("  "+allSymbols[k]); // (authorized) //$NON-NLS-1$
        }
        System.out.println(); // (authorized)
    }

    Set<Integer>[] filterSingleRhsRules() {
        Map<Integer,Set<Integer>> tmp = new TreeMap<Integer,Set<Integer>>();
        // identity
        for( int i = 0; i < allSymbols.length; i++ ) {
            Set<Integer> ii = new TreeSet<Integer>();
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


    HashMap<Integer,int[]> filterDoubleRhsRules() {
        HashMap<Integer,int[]> ret = new HashMap<Integer,int[]>();
        for( ChomskiTuple rule : rules ) {
            if( rule.rhs1!=-1 ) {
            	int[] headers = ret.get(Util.pair(rule.rhs0, rule.rhs1));
                for( int r : singleRhsRules[rule.head] )
                	headers = Array.insert(headers,r);
                ret.put(Util.pair(rule.rhs0, rule.rhs1), headers);
            }
        }

        return ret;
    }


    // This class is artifact of large memory footprint (12.9M) of the
    // [allSymbols.length][allSymbols.length] array which is sparse
    // Just converting uniform array into ragged array saves 40% of the space
    //
    // Can optimize it further (because values) is sparse array too
    // But have to be careful to keep the access to the elements fast
    // CYK performace is critical of it!
    /*public class Proj {
        public Set<Integer>[] values = new Set[allSymbols.length];
    }*/



    public static int[] toArray( Set<Integer> s ) {
        int[] ret = new int[s.size()];
        int i = 0;
        for( int ii : s )
            ret[i++] = ii;
        return ret;
    }

    /**
     * @param matrix
     * @param skipRanges
     * @param [x,y) is the current interval
     */
    protected static int splitInterval( Matrix matrix, int x, int y, int symbol, boolean leftDirection ) {
        for( int i = leftDirection?x+1:y-1; x<i && i<y; i=leftDirection?i+1:i-1 ) {
            Cell tmpIXsymbols = matrix.get(Util.pair(x,i));
            if( tmpIXsymbols == null )
                continue;
            Cell tmpIYsymbols = matrix.get(Util.pair(i,y));
            if( tmpIYsymbols == null )
                continue;
            for( int tmp : tmpIXsymbols.getContent() ) {
                if( tmp == symbol ) {
                    for( int tmp2 : tmpIYsymbols.getContent() ) {
                        if( tmp2 == symbol ) {
                            return i;
                        }                                                                                       
                    }                                                                   
                }                                                                                       
            }                                                                   
        }
        return -1;      
    }
    /**
     * @param matrix
     * @param skipRanges
     * @param [x,y) is the current interval
     */
    protected static int splitInterval( Matrix matrix, int x, int y, int symbol,int delimiter, boolean leftDirection ) {
        for( int i = leftDirection?x+1:y-1; x<i && i<y; i=leftDirection?i+1:i-1 ) {
            Cell tmp1 = matrix.get(Util.pair(x,i));
            if( tmp1 != null)
                for( int s1 : tmp1.getContent() ) 
                    if( s1 == symbol ) {
                        for( int delta = 1; delta <= y-i; delta++ ) {
                            Cell tmp2 = matrix.get(Util.pair(i+delta,y));
                            if( tmp2 == null)
                                continue;
                            boolean cont = true;
                            for( int s2 : tmp2.getContent() ) 
                                if( s2 == symbol ) 
                                    cont = false;
                            if( cont )
                                continue;
                             
                            Cell tmp = matrix.get(Util.pair(i,i+delta));
                            if( tmp != null )
                                for( int s : tmp.getContent() ) 
                                    if( s == delimiter ) 
                                        return leftDirection ? i+delta : i;
                        }
                    }            
            
        }
        return -1;      
    }

    protected static boolean containsSymbol( Cell cellContent, int symbol ) {
        if( cellContent == null )
            return false;
        for( int sb : cellContent.getContent() )
            if( symbol==sb )
                return true;

        return false;
    }
    protected static boolean containsEither( Cell cellContent, int[] symbols ) {
    	for( int s : symbols )
    		if( containsSymbol(cellContent, s) )
    			return true;

        return false;
    }
    
    public static void printErrors( String text, List<LexerToken> src, ParseNode root ) {
        int begin = 0;
        int end = text.length();
        for( ParseNode node : root.children() ) {
            if( begin < src.get(node.from).begin )
                begin = src.get(node.from).begin;
            if( src.get(node.to).end < end )
                end = src.get(node.to-1).end;
        }
        String fragment = text.substring(begin, end);
        if( fragment.length() > 200 )
            fragment = fragment.substring(0,40)+ " ... "+fragment.substring(fragment.length()-40);
        System.out.println(text.substring(0, begin)+"<<<*****\n"+fragment+"\n*****>>>"+text.substring(end));
    }
    
    
    @Override
	public
    ParseNode treeForACell( List<LexerToken> src, Matrix m, Cell cell, int x, int y ) {
        for( int i = 0; i < cell.size(); i++ ) {
            int symbol = cell.getSymbol(i);
            if( symbol == -1 )
                continue;
            return tree(m, x, y, symbol);
        }
        return null;
    }

    
    ////////////////////////////////////////////////////////
    

    protected ChomskiTuple[] getChomskyRules( Set<RuleTuple> input ) {
        //RuleTransforms.eliminateEmptyProductions(input);
        return convertToChomskyRules(input);
    }


    private ChomskiTuple[] convertToChomskyRules( Set<RuleTuple> rules ) {
        Set<RuleTuple> tmp = extractBinaryRules(rules);

        ChomskiTuple[] ret = new ChomskiTuple[tmp.size()];

        int i = 0;
        for( RuleTuple ct : tmp ) {
            ret[i++] = new ChomskiTuple(
                                        symbolIndexes.get(ct.head),
                                        symbolIndexes.get(ct.rhs[0]),
                                        ct.rhs.length>1 ? symbolIndexes.get(ct.rhs[1]) : -1
            );
        }

        return ret;
    }


    static Set<RuleTuple> split( RuleTuple rule ) {
        Set<RuleTuple> tmp = new TreeSet<RuleTuple>();
        if( rule.rhs.length == 0 )
            throw new RuntimeException("Empty Rule!"); //$NON-NLS-1$
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


    private static Set<RuleTuple> extractBinaryRules( Set<RuleTuple> rules ) {
        Set<RuleTuple> tmp = new TreeSet<RuleTuple>();
        for( RuleTuple rule : rules ) {                
            tmp.addAll( split(rule) );                  
        }
        return tmp;
    }
    
    
    public class ChomskiTuple implements Comparable<ChomskiTuple> {
        public int head;
        public int rhs0;  
        public int rhs1;  
        public ChomskiTuple( int h, int r0, int r1 ) {
            head = h;
            rhs0 = r0;
            rhs1 = r1;
        }
        int size() {
            return rhs1 == -1 ? 1 : 2; 
        }
        int content( int i ) {
            if( i == 0 )
                return rhs0;
            else if( i == 1 )
                return rhs1;
            else
                throw new IndexOutOfBoundsException("ChomskiTuple.content("+i+")");
        }
        
        public boolean equals(Object obj) {
            return (this == obj ) || ( obj instanceof ChomskiTuple &&  compareTo((ChomskiTuple)obj)==0);
        }
        public int hashCode() {
            throw new RuntimeException("hashCode inconssitent with equals"); //$NON-NLS-1$
        }              
        public int compareTo(ChomskiTuple src) {
            if( head==0 || src.head==0 )
                throw new RuntimeException("head==0 || src.head==0"); //$NON-NLS-1$
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
                return allSymbols[head]+": "+allSymbols[rhs0]+";"; //$NON-NLS-1$ //$NON-NLS-2$
            return allSymbols[head]+": "+allSymbols[rhs0]+"  "+allSymbols[rhs1]+";"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }


    private ParseNode tree( Matrix m, int x, int y, int out ) {
//if( x==1 && y==3 )
    //System.out.println("[1,3) out="+allSymbols[out]);
        
        //Cell cell = m.get(Util.pair(x,y));
        if( x+1 == y ) {
        	int in = out;
        	for( int i = 0; i < singleRhsRules.length; i++ ) {
        		Set<Integer> dependents = singleRhsRules[i];
        		if( dependents.contains(out) ) {
        			in = i;
        			break;
        		}
			}
            return new ParseNode(x,y, in,out, this);            
        }
        //for( int mid = x+1; mid < y; mid++ ) {
        for( int mid = y-1; x < mid; mid-- ) {
            Cell pre = m.get(Util.pair(x,mid));
            if( pre == null )
                continue;
            Cell post = m.get(Util.pair(mid,y));
            if( post == null )
                continue;
            for( int I : pre.getContent() )    // Not indexed Nested Loops
                for( int J : post.getContent() ) {
                    int[] A = doubleRhsRules.get(Util.pair(I, J));
                    if( A==null )
                        continue;
                    
                    int in = head(I, J, out);
                    if( in == -1 )
                        continue;
                    
                    ParseNode ret = new ParseNode(x,y,in,out, this);
                    ret.lft = tree(m, x,mid,I);
                    ret.rgt = tree(m, mid,y,J);
                    return ret;
                }                                                                              
        }
        throw new AssertionError("failed to extract the tree at ["+x+","+y+")");
    }
    
 
    private int head( int pre, int post, int closure ) {
        for( ChomskiTuple t : rules )
            if( t.rhs0 == pre && t.rhs1 == post && singleRhsRules[t.head].contains(closure) )
                return t.head;
        return -1;
    }
    
    public static void main( String[] args ) throws Exception {
        long h = Runtime.getRuntime().totalMemory();
        long hf = Runtime.getRuntime().freeMemory();
        System.out.println("mem="+(h-hf)); // (authorized) //$NON-NLS-1$
        
        Set<RuleTuple> rules = Program.latticeRules();
        CYK cyk = new CYK(rules);

        String input = Util.readFile(Run.class,"Test.prg");
        List<LexerToken> src =  (new Lex()).parse(input);
        
        Visual visual = null;
        if( src.size() < 1000 )
        	visual = new Visual(src, cyk);

        long t1 = System.currentTimeMillis();
        Matrix matrix = cyk.initArray(src);
        long t2 = System.currentTimeMillis();
        System.out.println("Init array time = "+(t2-t1)); // (authorized) //$NON-NLS-1$

        int size = matrix.size();
        TreeMap<Integer,Integer> skipRanges = new TreeMap<Integer,Integer>();
        t1 = System.currentTimeMillis();
        cyk.closure(matrix, 0, size+1, skipRanges, -1);
        t2 = System.currentTimeMillis();
        System.out.println("Parse time = "+(t2-t1)); // (authorized) //$NON-NLS-1$
        System.out.println(skipRanges);// (authorized)
        //instance.print(matrix);
        cyk.print(matrix, 0, size);
        //instance.print(ret, 0, 3);
        //instance.print(ret, 3, 6);
        if( visual != null )
            visual.draw(matrix);
        
        t1 = System.currentTimeMillis();
        ParseNode out = cyk.forest(src, matrix);
        t2 = System.currentTimeMillis();
        System.out.println("Reduction time = "+(t2-t1)); // (authorized) //$NON-NLS-1$
        
        h = Runtime.getRuntime().totalMemory();
        hf = Runtime.getRuntime().freeMemory();
        System.out.println("mem="+(h-hf)); // (authorized) //$NON-NLS-1$
        
        if( src.size() < 1000 )
        	out.printTree();

    }

}
