package qbql.parser;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import qbql.util.Util;

public class ParseNode implements Comparable {
	public int from;
	public int to;
	
	public int compareTo( Object obj ) {
		ParseNode src = (ParseNode)obj;
		if( from != src.from )
			return from - src.from;
		else
			return to - src.to;
	}
	public String toString() { return toString(0); }
	
	public List<ParseNode> descendants() {
		List<ParseNode> ret = new ArrayList<ParseNode>();
		ret.add(this);
		for( ParseNode n : children() )
			ret.addAll(n.descendants());
		return ret;
	}
	
	/**
	 * Ancestor chain from the leaf at position pos to the root "this" 
	 * @param pos
	 * @return
	 */
	public List<ParseNode> ancestors( int pos ) {
		return intermediates(pos,pos+1);
	}
	
	/**
	 * All the descendants of "this" containing the given interval [head,tail).
	 * That is, all the nodes on the ancestor chain from [head,tail) to "this".
	 * In other words, these are all the nodes between ParseNode[head,tail) and "this".
	 */ 	
	public  List<ParseNode> intermediates( int head, int tail ) {
		List<ParseNode> ret = new ArrayList<ParseNode>();
		if( from <= head && tail <= to ) 
			ret.add(this);
		for( ParseNode n : children() )
			if( n.from <= head && tail <= n.to ) 
				ret.addAll(n.intermediates(head, tail));
		return ret;
	}
	
	/**
	 * Parent of the ParseNode[head,tail), not "this" (which assumed to be the root)
	 * @param head
	 * @param tail
	 * @return
	 */
	public ParseNode parent( int head, int tail ) {
		for( ParseNode descendant : intermediates(head, tail) )
			for( ParseNode child : descendant.children() )
				if( child.from == head && child.to == tail )
					return descendant;
		return null;
	}
	
	void print( int depth ) {
		System.out.println(toString(depth)); // (authorized)
	}
	public ParseNode leafAtPos( int pos ) {
		if( children().size() == 0 && pos == from )
			return this;
		for( ParseNode child : children() ) {
			if( child.from <= pos && pos < child.to ) 
				return child.leafAtPos(pos);
		}
		return null;
	}
	private void calculateDepth( Map<Integer,Integer> depthMap, int depth ) {
		depthMap.put(Util.pair(from,to), depth);
		for( ParseNode child : children() ) {
			child.calculateDepth(depthMap, depth+1);
		}
	}
	Map<Integer,Integer> calculateDepth() {
		Map<Integer,Integer> depthMap = new TreeMap<Integer,Integer>();
		calculateDepth(depthMap, 0);
		return depthMap;
	}
	public void printTree() {
		Map<Integer,Integer> depthMap = calculateDepth();

		int i = 0;
		for( ParseNode n : descendants() ) {
			if(i++>500)
				return;
			int depth = depthMap.get(Util.pair(n.from, n.to));
			n.print(depth);
		}
	}
	public void printBinaryTree( int depth ) {
		print(depth);
		if( lft!=null )
			lft.printBinaryTree(depth+1);
		if( rgt!=null )
			rgt.printBinaryTree(depth+1);
		if( topLevel!=null )
			for( ParseNode n : topLevel )
				n.printBinaryTree(depth+1);
	}
	
	/**
	 * @param src -- scanner output
	 * @return -- scanner content corresponding to the parse node
	 */
	public String content( List<LexerToken> src ) {
		StringBuffer sb = new StringBuffer();
		for( int i = from; i < to; i++ )
			sb.append(src.get(i).content);
		return sb.toString();
	}
	
	//////////////////// Implementation (Former subclass) ///////////////////
	ParseNode lft = null;
	ParseNode rgt = null;

	private int payloadIn;   // ----> ParseNode
	int payloadOut;          // ParseNode ---->
	public Set<Integer> content() {
		if( payloadIn == -1 && payloadOut == -1 )
			return new TreeSet<Integer>();
		if( payloadIn == -1 && payloadOut != -1 ) {
			TreeSet<Integer> ret = new TreeSet<Integer>();
			ret.add(payloadOut);
			return ret;
		}
		if( payloadIn != -1 && payloadOut == -1 ) {
			TreeSet<Integer> ret = new TreeSet<Integer>();
			ret.addAll(cyk.singleRhsRules[payloadIn]);
			return ret;
		}
		TreeSet<Integer> ret = new TreeSet<Integer>();
		for( int candidate : cyk.singleRhsRules[payloadIn] )
			if( cyk.singleRhsRules[candidate].contains(payloadOut) )
				ret.add(candidate);
		return ret;
	}
	/**
	 * Check if the node contains "symbol"
	 * If "reducedContent" is not null then refer to this map
	 */
	public boolean contains( int symbol ) {
		return content().contains(symbol);
	}

	// If fail to parse complete text, then accumulate all children here
	// if topLevel != null then lft and rgt == null, and content is empty. 
	public Set<ParseNode> topLevel = null;

	private CYK cyk;
	public ParseNode( int begin, int end, int sIn, int sOut, CYK c ) {
		from = begin;
		to = end;
		payloadIn = sIn;
		payloadOut = sOut;
		cyk = c;
	}

	String toString( int depth ) {	
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < depth ;i++)
			sb.append("  "); 
		sb.append("["+from+","+to+") "); 
		for( Integer i : content() )
			if( !cyk.allSymbols[i].endsWith(")") )
				sb.append("  "+ cyk.allSymbols[i]);
		return sb.toString();
	}

	// careful changing this method: parse tree traversal depends on it
	public boolean isAuxiliary() {
		boolean noneAux = true;
		for( Integer symbol : content() ) {
			if( cyk.allSymbols[symbol].indexOf("+") < 0 
			 || content().size()==1 && "'+'".equals(cyk.allSymbols[symbol]) ) {
				noneAux = false;
				break;
			}
		}
		return noneAux;   
	}

	// navigates through children skipping all the auxiliary nodes
	public Set<ParseNode> children() {
		Set<ParseNode> ret = new TreeSet<ParseNode>();
		if( topLevel != null ) {
			for( ParseNode child: topLevel ) {
				if( child.isAuxiliary() )
					ret.addAll(child.children());
				else
					ret.add(child);
			}
			return ret;
		}
		if( lft == null )
			return ret;
		if( lft.isAuxiliary() ) {
			ret.addAll( lft.children() );
		} else
			ret.add(lft);
		if( rgt == null )
			return ret;
		if( rgt.isAuxiliary() ) {
			ret.addAll( rgt.children() );
		} else
			ret.add(rgt);
		return ret;
	}

	public void moveInterval( int offset ) {
		from += offset;
		to += offset;
		if( topLevel != null )
			for( ParseNode p :  topLevel ) 
				p.moveInterval(offset);
		else {
			if( lft != null )
				lft.moveInterval(offset);
			if( rgt != null )
				rgt.moveInterval(offset);
		}
	}
	
	/*
	public BigInteger weight() {
		Set<ParseNode> tmp = new TreeSet<ParseNode>();
		return weight(tmp);
	}
	private BigInteger weight( Set<ParseNode> tmp ) {
		if( tmp.contains(this) )
			return new BigInteger("0");
		tmp.add(this);
		if( !isAuxiliary() )
			return (new BigInteger(""+(to-from))).pow(2);			
		BigInteger ret = new BigInteger("0");
		for( ParseNode child : children() ) {
			ret = ret.add(child.weight(tmp));
		}
		return ret;   
	}*/
	/*
	 * debug
	public BigInteger weight1( int depth, Set<ParseNode> tmp ) {
		System.out.println(toString(depth)); // (authorized)
		if( tmp.contains(this) )
			return new BigInteger("0");
		tmp.add(this);
		if( !isAuxiliary() )
			return (new BigInteger(""+(to-from))).pow(2);			
		BigInteger ret = new BigInteger("0");
		for( ParseNode child : children() ) {
			ret = ret.add(child.weight1(depth+1, tmp));
		}
		return ret;   
	}*/


    public static void main(String[] args) throws Exception {
    }
}

