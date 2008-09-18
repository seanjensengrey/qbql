package qbql.lattice;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import qbql.parser.RuleTuple;
import qbql.parser.CYK.ChomskiTuple;

public class Relation {
	Map<String,Integer> header = new HashMap<String,Integer>();
	String[] colNames;
	
	Set<Tuple> content = new HashSet<Tuple>(); // TreeSet<Tuple>
	
	public Relation( String[] columns ) {
		colNames = columns;
		for( int i = 0; i < columns.length; i++ ) {
			header.put(colNames[i],i);
		}		
	}
	
	public void addTuple( Map<String,String> content ) {
		String[] newTuple = new String[colNames.length];
		Set<String> columns = content.keySet();
		for( String colName : columns )
			newTuple[header.get(colName)] = content.get(colName);
		this.content.add(new Tuple(newTuple));
	}
	
	TreeSet<Tuple> orderedContent() {
		TreeSet<Tuple> ret = new TreeSet<Tuple>();
		for( Tuple tuple : content ) 
			ret.add(tuple);
		return ret;		
	}

	
	public String toString() {
		StringBuffer ret = new StringBuffer("{");
		for( Tuple tuple : orderedContent() ) {
			ret.append("<");
			for( int i = 0; i < tuple.data.length; i++ )
				ret.append(colNames[i]+"="+tuple.data[i]+",");
			ret.append(">,");
		}
		ret.append("}");
		return ret.toString();
	}
	
	public static Relation join( Relation x, Relation y ) {
		Set<String> header = new HashSet<String>();
		header.addAll(x.header.keySet());
		header.addAll(y.header.keySet());		
		Relation ret = new Relation(header.toArray(new String[0]));
		for( Tuple tupleX: x.content )
			for( Tuple tupleY: y.content ) {				
				String[] retTuple = new String[header.size()];
				for( String attr : ret.colNames ) {
					Integer xAttr = x.header.get(attr);
					Integer yAttr = y.header.get(attr);
					if( xAttr == null )
						retTuple[ret.header.get(attr)] = tupleY.data[y.header.get(attr)];
					else if( yAttr == null )
						retTuple[ret.header.get(attr)] = tupleX.data[x.header.get(attr)];
					else {
						if( !tupleY.data[y.header.get(attr)].equals(tupleX.data[x.header.get(attr)]) ) {
							retTuple = null;
							break;
						} else
							retTuple[ret.header.get(attr)] = tupleX.data[x.header.get(attr)];
					}
				}
				if( retTuple != null )
					ret.content.add(new Tuple(retTuple));
			}
		return ret;
	}

	public static Relation innerUnion( Relation x, Relation y ) {
		Set<String> header = new HashSet<String>();
		header.addAll(x.header.keySet());
		header.retainAll(y.header.keySet());		
		Relation ret = new Relation(header.toArray(new String[0]));
		for( Tuple tupleX: x.content ){
			String[] retTuple = new String[header.size()];
			for( String attr : ret.colNames ) {
				retTuple[ret.header.get(attr)] = tupleX.data[x.header.get(attr)];
			}
			ret.content.add(new Tuple(retTuple));
		}
		for( Tuple tupleY: y.content ){
			String[] retTuple = new String[header.size()];
			for( String attr : ret.colNames ) {
				retTuple[ret.header.get(attr)] = tupleY.data[y.header.get(attr)];
			}
			ret.content.add(new Tuple(retTuple));
		}
		return ret;
	}
	
	public static Relation innerJoin( Relation x, Relation y ) {
		Set<String> header = new HashSet<String>();
		header.addAll(x.header.keySet());
		header.retainAll(y.header.keySet());		
		Relation ret = new Relation(header.toArray(new String[0]));
		for( Tuple tupleX: x.content ){
			String[] retTuple = new String[header.size()];
			for( String attr : ret.colNames ) {
				retTuple[ret.header.get(attr)] = tupleX.data[x.header.get(attr)];
			}
			ret.content.add(new Tuple(retTuple));
		}
		Set<Tuple> content = new HashSet<Tuple>(); // TreeSet<Tuple>
		for( Tuple tupleY: y.content ){
			String[] retTuple = new String[header.size()];
			for( String attr : ret.colNames ) {
				retTuple[ret.header.get(attr)] = tupleY.data[y.header.get(attr)];
			}
			content.add(new Tuple(retTuple));
		}
		ret.content.retainAll(content);
		return ret;
	}
	
	/**
	 * @param x
	 * @param y
	 * @return x < y (i.e. x ^ y = y)
	 */
	public static boolean le( Relation x, Relation y ) {
		return y.equals(join(x,y));
	}
	public boolean equals( Object o ) {
		Relation src = (Relation) o;
		if( colNames.length != src.colNames.length )
			return false;
		if( content.size() != src.content.size() )
			return false;
		String[] hdr = header.keySet().toArray(new String[0]);
		String[] srcHdr = src.header.keySet().toArray(new String[0]);
		for( int i = 0; i < hdr.length; i++ )
			if( !hdr[i].equals(srcHdr[i]) )
				return false;
		LinkedList<Tuple> list = new LinkedList<Tuple>();
		for( Tuple t : content )
			list.add(t);
		while( list.size() > 0 &&  matchAndDelete(list, src) )
			;
		return list.size()==0;
	}
	private boolean matchAndDelete( LinkedList<Tuple> list, Relation y ) {
		Tuple tx = list.getFirst();
		for( Tuple ty : y.content ) {
			if( tx.equals(ty,this,y) ) {
				list.removeFirst();
				return true;
			}
		}
		return false;
	}
	
	public int hashCode() {
		StringBuffer ret = new StringBuffer();
		ret.append(colNames.length);
		ret.append(content.size());
		if( colNames.length > 0 && content.size() > 0 ) {
			for( Tuple t : content ) {
				ret.append(t.data[0]);
				break;
			}
		}
		return ret.toString().hashCode();
	}

	/////////////////////////PARSER//////////////////////////
	private static final String fname = "grammar.serializedBNF";
	private static final String path = "/qbql/lattice/";
	public static void memorizeRules( Set<RuleTuple> rules ) throws Exception {
		FileOutputStream fos = new FileOutputStream("c:/qbql_trunk"+path+fname);
		ObjectOutputStream out = new ObjectOutputStream(fos);
		out.writeObject(rules);
		out.close();
	}
	public static Set<RuleTuple> getRules() throws Exception {
        URL u = Relation.class.getResource( path+fname );
        InputStream is = u.openStream();
		ObjectInputStream in = new ObjectInputStream(is);
		Set<RuleTuple> rules = (Set<RuleTuple>) in.readObject();
		in.close();
		return rules;
	}
	private static Set<RuleTuple> extractRules() {
		Set<RuleTuple> ret = new TreeSet<RuleTuple>();
		// LATTICE part
		ret.add(new RuleTuple("expr", new String[] {"identifier"}));
		ret.add(new RuleTuple("expr", new String[] {"'('","expr","')'"}));
		ret.add(new RuleTuple("join", new String[] {"expr","'^'","expr"}));
		ret.add(new RuleTuple("innerJoin", new String[] {"expr","'*'","expr"}));
		ret.add(new RuleTuple("innerUnion", new String[] {"expr","'v'","expr"}));
		ret.add(new RuleTuple("outerUnion", new String[] {"expr","'+'","expr"}));
		ret.add(new RuleTuple("complement", new String[] {"expr","'''"}));
		ret.add(new RuleTuple("expr", new String[] {"join"}));
		ret.add(new RuleTuple("expr", new String[] {"innerJoin"}));
		ret.add(new RuleTuple("expr", new String[] {"innerUnion"}));
		ret.add(new RuleTuple("expr", new String[] {"outerUnion"}));
		ret.add(new RuleTuple("expr", new String[] {"complement"}));
		ret.add(new RuleTuple("equation", new String[] {"expr","'='","expr"}));
		ret.add(new RuleTuple("inequality", new String[] {"expr","'<'","expr"}));
		ret.add(new RuleTuple("assertion", new String[] {"equation","'.'"}));
		ret.add(new RuleTuple("assertion", new String[] {"inequality","'.'"}));
		ret.add(new RuleTuple("assertions", new String[] {"assertion"}));
		ret.add(new RuleTuple("assertions", new String[] {"assertions","assertion"}));

		// Set Theoretic part
		ret.add(new RuleTuple("attribute", new String[] {"identifier"}));
		ret.add(new RuleTuple("value", new String[] {"attribute","'='","digits"}));
		ret.add(new RuleTuple("value", new String[] {"attribute","'='","identifier"}));
		ret.add(new RuleTuple("values", new String[] {"value"}));
		ret.add(new RuleTuple("values", new String[] {"values","','","value"}));
		ret.add(new RuleTuple("tuple", new String[] {"'<'","values","'>'"}));
		ret.add(new RuleTuple("tuples", new String[] {"tuple"}));
		ret.add(new RuleTuple("tuples", new String[] {"tuples","','","tuple"}));
		ret.add(new RuleTuple("relation", new String[] {"'{'","tuples","'}'"}));
		ret.add(new RuleTuple("expr", new String[] {"relation"}));
		ret.add(new RuleTuple("assignment", new String[] {"identifier","'='","expr","';'"})); // if defined in terms of lattice operations
		ret.add(new RuleTuple("database", new String[] {"assignment"}));
		ret.add(new RuleTuple("database", new String[] {"database","assignment"}));
		return ret;
	}
	
	public static void printRules( Set<RuleTuple> rules ) {
		RuleTuple predecessor = null;
		for( RuleTuple rule : rules ) {
			System.out.println(rule.toHTML(predecessor)); 
			predecessor = rule;
		}
	}

	
	public static void main( String[] args ) throws Exception {
		Set<RuleTuple> rules = extractRules();
		memorizeRules(rules);
		printRules(rules);
	}

}
