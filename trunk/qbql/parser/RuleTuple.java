package qbql.parser;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * class RuleTuple, example:
 * 
 * expr : expr '+' expr
 * expr : expr '*' expr
 * 
 * head rhs
 * ---- -----------------------
 * expr {"expr", "'+'", "expr"}   
 * expr {"expr", "'*'", "expr"}   
 */
public class RuleTuple implements Comparable, Serializable {
	String head;
	String[] rhs;
	public RuleTuple( String h, List<String> r ) {
		head = h;
		rhs = new String[r.size()];
		int i = 0;
		for(String t: r)
			rhs[i++]=t;
	}
	public RuleTuple( String h, String[] r ) {
		head = h;
		rhs = r;
	}
	public boolean equals(Object obj) {
		return compareTo(obj)==0;
	}
	public int hashCode() {
		throw new RuntimeException("hashCode inconsistent with equals"); 
	}		
	public int compareTo(Object obj) {
		RuleTuple src = (RuleTuple)obj;
		int cmp = (head == null) ? 0 : head.compareTo(src.head);
		if( cmp!=0 )
			return cmp;
		if( rhs.length != src.rhs.length )
			return rhs.length - src.rhs.length;
		for(int i=0; i<rhs.length; i++)
			if( rhs[i].compareTo(src.rhs[i]) != 0 )
				return rhs[i].compareTo(src.rhs[i]);
		return 0;
	}
	/**
	 * Is keyword is inside brackets? Then ignore the rule.
	 */
	public boolean ignore( String keyword, String bra, String ket ) {
		boolean enteredBrackets = false; 
		for(String token: rhs) {
			if( token.equals(bra) ) {
				enteredBrackets = true;
			}
			if( enteredBrackets ) {
				if(token.equals(keyword))
					return true;
			}
			if( token.equals(ket) ) {
				return false;
			}
		}
		return false;
	}
	public String toString() {
		StringBuffer b = new StringBuffer();
		if( head!=null )
			b.append(head+":");
		for(String t: rhs) 
			b.append(" "+t);
		return b.toString();
	}
	
}
