package qbql.index;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import qbql.lattice.EqualityPredicate;
import qbql.lattice.Grammar;
import qbql.lattice.Predicate;
import qbql.lattice.Relation;
import qbql.lattice.Tuple;
import qbql.util.Util;

public class IndexedPredicate extends Predicate {
    
    private HashMap<String,Method> indexes = new HashMap<String,Method>();
    private HashMap<String,String> renamed = new HashMap<String,String>(); //new_name->old_name
    private String oldName( String col ) {
        String ret = renamed.get(col);
        return (ret == null) ? col : ret;
    }
    private String newName( String col ) {
        String ret = null;
        for( String newName : renamed.keySet() )
            if( col.equals(renamed.get(newName)) )
                ret = newName;
        return (ret == null) ? col : ret;
    }
    private Method method( String col ) {
        return indexes.get(oldName(col));
    }
    
    public void renameInPlace( String from, String to ) throws Exception {
        if( from.equals(to) )
            return;
        int colFrom = header.get(from);
        Integer colTo = header.get(to);
        if( colTo == null ) {
            colNames[colFrom] = to;
            header.remove(from);
            header.put(to, colFrom);
            renamed.put(to, from);
        } else
            throw new Exception("Column collision");
    }

    public IndexedPredicate( String name ) throws Exception {
        Set<String> tmp = new TreeSet<String>();
        Class<?> c = Class.forName("qbql.index."+name);
        for( Method m : c.getDeclaredMethods() ) {
            String fullName = m.getName();
            if( "main".equals(fullName) )
                continue;
            StringTokenizer st = new StringTokenizer(fullName, "_", false);
            int pos = 0;
            while( st.hasMoreTokens() ) {
                String token = st.nextToken();
                if( pos == 0 )
                    indexes.put(token, m);
                tmp.add(token);
                pos++;
            }
            colNames = new String[tmp.size()];
            pos = 0;
            for( String s : tmp ) {
                colNames[pos] = s;
                header.put(colNames[pos],pos);
                pos++;
            }          
        }
    }
    public IndexedPredicate( IndexedPredicate ip ) throws Exception {
        super(ip.colNames);       
        indexes = (HashMap<String, Method>) ip.indexes.clone();
        renamed = (HashMap<String, String>) ip.renamed.clone();
    }

    IndexedPredicate lft = null;
    IndexedPredicate rgt;
    int oper;
    public IndexedPredicate( IndexedPredicate lft, IndexedPredicate rgt, int oper ) {
        super(
              oper==Grammar.naturalJoin ?
              Util.union(lft.colNames,rgt.colNames) :
              Util.symmDiff(lft.colNames,rgt.colNames)
        );       
        Set<String> header = new TreeSet<String>();
        header.addAll(lft.header.keySet());
        header.addAll(rgt.header.keySet());               
        this.lft = lft;
        this.rgt = rgt;
        this.oper = oper;
    }
    public static IndexedPredicate join( IndexedPredicate x, IndexedPredicate y ) throws Exception {
        return new IndexedPredicate(x,y,Grammar.naturalJoin);
    }
    public static IndexedPredicate joinIX( IndexedPredicate x, IndexedPredicate y ) throws Exception {
        return new IndexedPredicate(x,y,Grammar.setIX);
    }
    public static Relation join( Relation x, IndexedPredicate y ) throws Exception {
        if( y.lft != null )
            return join(join(x,y.lft),y.rgt);
        Set<String> header = new TreeSet<String>();
        header.addAll(x.header.keySet());
        header.addAll(y.header.keySet());               
        Relation ret = new Relation(header.toArray(new String[0]));
        for( Tuple tupleX: x.content ) {
            Object[] retTuple = new Object[header.size()];
            for( String attr : ret.colNames ) {
                Integer colRet = ret.header.get(attr);
                Integer colX = x.header.get(attr);
                Integer colY = y.header.get(attr);
                if( colY != null ) {
                    Method m = y.method(attr);
                    if( m == null ) {
                        if( colX == null )
                            throw new Exception("Missing index");
                        retTuple[colRet] = tupleX.data[colX];
                        continue;
                    }
                    String fullName = m.getName();
                    Object[] args = new Object[m.getParameterTypes().length];
                    StringTokenizer st = new StringTokenizer(fullName, "_", false);
                    int pos = -1;
                    while( st.hasMoreTokens() ) {
                        pos++;
                        String origName = st.nextToken();
                        if( pos == 0 )
                            continue;
                        args[pos-1] = tupleX.data[x.header.get(y.newName(origName))];
                    }
                    Object o = m.invoke(null, args);
                    if( colX != null ) 
                        if( !o.equals(tupleX.data[colX]) ) {
                            retTuple = null;
                            break;
                        }
                    retTuple[colRet] = o;
                } else 
                    retTuple[colRet] = tupleX.data[colX];
            }
            if( retTuple != null )
            ret.content.add(new Tuple(retTuple));
        }
        return ret;
    }
    public static Relation joinIX( Relation x, IndexedPredicate y ) throws Exception {
        throw new Exception("Not implemented");
    }

}
