package qbql.lattice;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import qbql.parser.LexerToken;
import qbql.parser.ParseNode;
import qbql.parser.RuleTuple;
import qbql.parser.Token;
import qbql.util.Permutations;
import qbql.util.Util;

public class Relation extends Predicate {

    public Set<Tuple> content = new HashSet<Tuple>(); // TreeSet<Tuple>

    public Relation( String[] columns ) {
        super(columns);		
    }
    
    public void renameInPlace( String from, String to ) {
        if( from.equals(to) )
            return;
        int colFrom = header.get(from);
        Integer colTo = header.get(to);
        if( colTo == null ) {
            colNames[colFrom] = to;
            header.remove(from);
            header.put(to, colFrom);            
        } else {
            HashMap<String,Integer> newHeader = new HashMap<String,Integer>();
            String[] newColNames = new String[colNames.length-1];
            Set<Tuple> newContent = new HashSet<Tuple>();
            for( int i = 0; i < colNames.length; i++ ) 
                if( i < colFrom ) {
                    newColNames[i] = colNames[i];
                    newHeader.put(colNames[i], i);
                } else if( colFrom < i ) {
                    newColNames[i-1] = colNames[i];
                    newHeader.put(colNames[i], i-1);
                }
            
            for( Tuple t : content ) {
                if( !t.data[colFrom].equals(t.data[colTo]) )
                    continue;
                Object[] newT = new String[newColNames.length];
                for( int i = 0; i < colNames.length; i++ ) 
                    if( i < colFrom ) {
                        newT[i] = t.data[i];
                    } else if( colFrom < i ) {
                        newT[i-1] = t.data[i];
                    }
                newContent.add(new Tuple(newT));
            }
            
            header = newHeader;
            colNames = newColNames;
            content = newContent;
        }
    }

    public void addTuple( Map<String,Object> content ) {
        Object[] newTuple = new Object[colNames.length];
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


    public String toString( int ident, boolean isSetNotation ) {
        if( colNames.length == 0 ) {
            if( content.size() == 0 )
                return "R00";
            else
                return "R01";
        }
        StringBuffer ret = new StringBuffer("");
        if( !isSetNotation ) {
            //ret.append(header.keySet()+"\n");
            // no commas
            ret.append("[");
            for( int i = 0; i < colNames.length; i++ )
                ret.append((i>0?"  ":"")+colNames[i]);
            ret.append("]\n");
            for( Tuple tuple : orderedContent() ) {
                boolean firstTuple = true;
                for( int i = 0; i < tuple.data.length; i++ ) {
                    ret.append((firstTuple?Util.identln(ident," "):"  ")+tuple.data[i]);
                    firstTuple = false;
                }
                ret.append("\n");
            }
        } else {
            ret.append("{");
            boolean firstTuple = true;
            String tupleSeparator = ",";
            if( ident > 0 )
                tupleSeparator = "\n"+Util.identln(ident,",");
            for( Tuple tuple : orderedContent() ) {
                ret.append((firstTuple?"":tupleSeparator)+"<");
                firstTuple = false;
                for( int i = 0; i < tuple.data.length; i++ )
                    ret.append((i==0?"":",")+colNames[i]+"="+tuple.data[i]);
                ret.append(">");
            }
            ret.append("}");
        }
        return ret.toString();
    }

    public static Relation join( Relation x, Relation y ) {
        Set<String> header = new TreeSet<String>();
        header.addAll(x.header.keySet());
        header.addAll(y.header.keySet());		
        Relation ret = new Relation(header.toArray(new String[0]));
        for( Tuple tupleX: x.content )
            for( Tuple tupleY: y.content ) {				
                Object[] retTuple = new Object[header.size()];
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
        Set<String> header = new TreeSet<String>();
        header.addAll(x.header.keySet());
        header.retainAll(y.header.keySet());		
        Relation ret = new Relation(header.toArray(new String[0]));
        for( Tuple tupleX: x.content ){
            Object[] retTuple = new Object[header.size()];
            for( String attr : ret.colNames ) {
                retTuple[ret.header.get(attr)] = tupleX.data[x.header.get(attr)];
            }
            ret.content.add(new Tuple(retTuple));
        }
        for( Tuple tupleY: y.content ){
            Object[] retTuple = new Object[header.size()];
            for( String attr : ret.colNames ) {
                retTuple[ret.header.get(attr)] = tupleY.data[y.header.get(attr)];
            }
            ret.content.add(new Tuple(retTuple));
        }
        return ret;
    }

    public static Relation innerJoin( Relation x, Relation y ) {
        Set<String> header = new TreeSet<String>();
        header.addAll(x.header.keySet());
        header.retainAll(y.header.keySet());		
        Relation ret = new Relation(header.toArray(new String[0]));
        for( Tuple tupleX: x.content ){
            Object[] retTuple = new Object[header.size()];
            for( String attr : ret.colNames ) {
                retTuple[ret.header.get(attr)] = tupleX.data[x.header.get(attr)];
            }
            ret.content.add(new Tuple(retTuple));
        }
        Set<Tuple> content = new HashSet<Tuple>(); // TreeSet<Tuple>
        for( Tuple tupleY: y.content ){
            Object[] retTuple = new Object[header.size()];
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
     * @return x < y (i.e. x ^ y = x)
     */
    public static boolean le( Relation x, Relation y ) {
        return x.equals(join(x,y));
    }
    public static boolean ge( Relation x, Relation y ) {
        return y.equals(join(x,y));
    }
    public boolean equals( Object o ) {
        if( this == o )
            return true;
        Relation src = (Relation) o;
        if( colNames.length != src.colNames.length )
            return false;
        if( content.size() != src.content.size() )
            return false;
        
        for( String colName : header.keySet() ) {
            Integer j = src.header.get(colName);
            if( j == null )
                return false;
        }

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
    
    // rename is in-place operation.
    // therefore auxiliary methods to restore relation header after each rename
    /*private static String[] cloneColumnNames( Relation src ) {
        String[] origColNames = new String[src.colNames.length];
        for( int i = 0; i < origColNames.length; i++ )
            origColNames[i] = src.colNames[i];
        return origColNames;
    }
    private static Map<String, Integer> cloneHeader( Relation src ) {
        Map<String,Integer> origHdr = new HashMap<String,Integer>();
        for( String key : src.header.keySet() )
            origHdr.put(key, src.header.get(key));
        return origHdr;
    }*/

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

    private static boolean map( Relation x, Relation y, int colX, int colY ) {
        boolean matchedAllRows = true;
        for ( Tuple tx: x.content ) { 
            boolean matchedX = false;
            for ( Tuple ty: y.content ) {
                if( !tx.data[colX].equals(ty.data[colY]) )
                    continue;
                matchedX = true;
                break;
            }
            if( !matchedX ) {
                matchedAllRows = false;
                break;
            }
        }
        if( matchedAllRows )
            return true;
        return false;
    }
    static boolean match( Relation x, Relation y, int colX, int colY ) {
        return map(x,y, colX,colY) && map(y,x, colY,colX);
    }

    static Relation renameCols( Relation x, Relation y, int[] indexes ) {
        Relation ret = join(y,Database.R01); // clone
        for( int i = 0; i < ret.colNames.length; i++) 
            if( indexes[i] < 0 ) 
                ret.renameInPlace(y.colNames[i], y.colNames[i]+"1");
        Map<String,String> rename = new HashMap<String,String>();
        for( int i = 0; i < ret.colNames.length; i++) 
            if( 0 <= indexes[i] ) 
                rename.put(y.colNames[i], x.colNames[indexes[i]]);     
        for( String from : rename.keySet() ) 
            ret.renameInPlace(from, rename.get(from));
        return ret;
    }

}
