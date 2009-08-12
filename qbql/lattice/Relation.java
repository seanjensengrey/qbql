package qbql.lattice;

import java.util.HashMap;
import java.util.HashSet;
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
    
    private void rename( String from, String to ) {
        header.remove(from);
        for( int i = 0; i < colNames.length; i++ )
            if( colNames[i].equals(from) ) {
                colNames[i] = to;
                header.put(to, i);
                return;
            }
    }
    /*public void rename( String[] from, String[] to ) {
        for( int i = 0; i < from.length; i++ )
            rename(from[i],to[i]);
    }*/
    public void rename( Map<String,String> m ) {
        // clone header and colNames
        Map<String,Integer> hdr = new HashMap<String,Integer>();
        String[] cols = new String[colNames.length];
        for( int i = 0; i < colNames.length; i++ ) {
            cols[i]=colNames[i];
            hdr.put(colNames[i],i);
        }
        header = hdr;
        colNames = cols;
        
        for( String from : m.keySet() )
            rename(from,m.get(from));
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
        return toString(0, false);
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
        Set<String> header = new TreeSet<String>();
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
        Set<String> header = new TreeSet<String>();
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

    public static Relation unison( Relation x, Relation y ) throws Exception {
        Set<String> header = new TreeSet<String>();
        header.addAll(x.header.keySet());
        header.removeAll(y.header.keySet());            
        Set<String> header1 = new TreeSet<String>();
        header1.addAll(y.header.keySet());
        header1.removeAll(x.header.keySet());
        header.addAll(header1);
              
        // fix x@R01 = x.
        /*if( x.colNames.length == 0 && x.content.size() > 0 ) 
            return y;
        if( y.colNames.length == 0 && y.content.size() > 0 ) 
            return x;*/
        
        Relation ret = new Relation(header.toArray(new String[0]));
        if( x.colNames.length != y.colNames.length )
            return ret;
        
        // fix x@x = R01 
        if( ret.colNames.length == 0 && x.content.size() == 0 && y.content.size() == 0 ) {
            ret.content.add(new Tuple(new String[]{}));
            return ret;
        }
        
        Map<String, Integer> origHdr = cloneHeader(x);
        String[] origColNames = cloneColumnNames(x);
        
        Set<Tuple> tmp = new HashSet<Tuple>(); 
        List<Map<String,String>> permutations = Permutations.permute(x.colNames,y.colNames);
        for( Map<String,String> m : permutations ) {
            x.header = origHdr;
            x.colNames = origColNames;
            x.rename(m);
            for( Tuple tupleX: x.content )
                for( Tuple tupleY: y.content )
                    if( tupleX.equals(tupleY,x,y) ) {
                        String[] data = new String[header.size()];
                        for( String attr: ret.colNames ) {
                            Integer posX = origHdr.get(attr);
                            if( posX != null ) {
                                data[ret.header.get(attr)] = tupleX.data[posX];
                                continue;
                            }
                            Integer posY = y.header.get(attr);
                            if( posY != null ) {
                                data[ret.header.get(attr)] = tupleY.data[posY];
                                continue;
                            }
                            throw new Exception("unexpected case");
                        }
                        tmp.add(new Tuple(data));
                    }
            if( tmp.size() > ret.content.size() )
                ret.content = tmp;
        }
        x.header = origHdr;
        x.colNames = origColNames;

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
    public static boolean equivalent( Relation x, Relation y ) {
        if( x == y )
            return true;
        if( x.colNames.length != y.colNames.length )
            return false;
        if( x.content.size() != y.content.size() )
            return false;
        Map<String, Integer> origHdr = cloneHeader(x);
        String[] origColNames = cloneColumnNames(x);
        
        List<Map<String,String>> permutations = Permutations.permute(x.colNames,y.colNames);
        for( Map<String,String> m : permutations ) {
            x.rename(m);
            if( x.equals(y) ) {
                x.header = origHdr;
                x.colNames = origColNames;
                return true;
            }
            x.header = origHdr;
            x.colNames = origColNames;
        }
        return false;
    }
    
    // rename is in-place operation.
    // therefore auxiliary methods to restore relation header after each rename
    private static String[] cloneColumnNames( Relation src ) {
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


}