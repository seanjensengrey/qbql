package qbql.index;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import qbql.lattice.Predicate;
import qbql.lattice.Relation;
import qbql.lattice.Tuple;

public class IndexedPredicate extends Predicate {
    
    private Map<String,Method> indexes = new HashMap<String,Method>();

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

    public static Relation join( Relation x, IndexedPredicate y ) throws Exception {
        Set<String> header = new TreeSet<String>();
        header.addAll(x.header.keySet());
        header.addAll(y.header.keySet());               
        Relation ret = new Relation(header.toArray(new String[0]));
        for( Tuple tupleX: x.content ) {
            Object[] retTuple = new Object[header.size()];
            for( String attr : ret.colNames ) {
                Integer colRet = ret.header.get(attr);
                Integer colX = x.header.get(attr);
                if( colX == null ) {
                    Method m = y.indexes.get(attr);
                    if( m == null )
                        throw new Exception("Missing index");
                    String fullName = m.getName();
                    Object[] args = new Object[m.getParameterTypes().length];
                    StringTokenizer st = new StringTokenizer(fullName, "_", false);
                    int pos = -1;
                    while( st.hasMoreTokens() ) {
                        pos++;
                        String token = st.nextToken();
                        if( pos == 0 )
                            continue;
                        args[pos-1] = tupleX.data[x.header.get(token)];
                    }
                    retTuple[colRet] = m.invoke(null, args);
                } else 
                    retTuple[colRet] = tupleX.data[colX];
            }
            ret.content.add(new Tuple(retTuple));
        }
        return ret;
    }

}
