package qbql.index;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import qbql.lattice.Database;
import qbql.lattice.EqualityPredicate;
import qbql.lattice.Program;
import qbql.lattice.Predicate;
import qbql.lattice.Relation;
import qbql.lattice.Tuple;
import qbql.util.Util;

public class IndexedPredicate extends Predicate {
    
    Class<?> implementation = null;
    
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
    
    private Method method( Set<String> output ) {
        Method ret = null;
        for( Method m : methods() ) {
            List<String> args = arguments(m,ArgType.OUTPUT);
            boolean inputsCovered = true;
            for( String i : output )
                if( !args.contains(oldName(i)) ) {
                    inputsCovered = false;
                    break;
                }
            if( !inputsCovered )
                continue;
            ret = m;
            if( m.getReturnType() == NamedTuple.class )
                return ret;
        }
        return ret;
    }
    
    public void renameInPlace( String from, String to ) throws Exception {
        if( lft != null && lft.header.containsKey(from) )
            lft.renameInPlace(from, to);
        if( rgt != null && rgt.header.containsKey(from) )
            rgt.renameInPlace(from, to);
            
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

    private Database db = null;
    public IndexedPredicate( Database db, String name ) throws Exception {
        this.db = db;
        Set<String> tmp = new TreeSet<String>();
        implementation = Class.forName(db.pkg+"."+name);
        for( Method m : methods() ) {
            tmp.addAll(arguments(m,ArgType.BOTH));
        }
        colNames = new String[tmp.size()];
        int pos = 0;
        for( String s : tmp ) {
            colNames[pos] = s;
            header.put(colNames[pos],pos);
            pos++;
        }          
    }
    public IndexedPredicate( IndexedPredicate ip ) throws Exception {
        super(Util.clone(ip.colNames)); 
        this.db = ip.db;
        implementation = ip.implementation;
        if( ip.lft != null )
            lft = new IndexedPredicate(ip.lft);
        if( ip.rgt != null )
            rgt = new IndexedPredicate(ip.rgt);
        renamed = (HashMap<String, String>) ip.renamed.clone();
    }

    IndexedPredicate lft = null;
    IndexedPredicate rgt;
    int oper;
    public IndexedPredicate( IndexedPredicate lft, IndexedPredicate rgt, int oper ) {
        super(
              oper==Program.naturalJoin ?
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
    
    private Set<Method> methods() {
        Set<Method> ret = new HashSet<Method>();
        for( Method m : implementation.getDeclaredMethods() ) {
            if( !Modifier.isPublic(m.getModifiers()) ) 
                continue;
            Class<?> returnType = m.getReturnType();
            if( returnType!=Relation.class && returnType!=NamedTuple.class )
                continue;
            ret.add(m);
        }
        return ret;
    }
    static enum ArgType {INPUT,OUTPUT,BOTH}
    private static List<String> arguments( Method m, ArgType at ) {
        List<String> ret = new LinkedList<String>();
        StringTokenizer st = new StringTokenizer(m.getName(), "_", false);
        int pos = -1;
        while( st.hasMoreTokens() ) {
            pos++;
            String token = st.nextToken();
            if( at == ArgType.OUTPUT && pos < m.getParameterTypes().length )
                continue;
            if( at == ArgType.INPUT && m.getParameterTypes().length <= pos )
                break;
            ret.add(token);
        }
        return ret;
    }

    
    public static IndexedPredicate join( IndexedPredicate x, IndexedPredicate y ) throws Exception {
        return new IndexedPredicate(x,y,Program.naturalJoin);
    }
    public static IndexedPredicate setIX( IndexedPredicate x, IndexedPredicate y ) throws Exception {
        return new IndexedPredicate(x,y,Program.setIX);
    }
    public static Relation join( Relation x, IndexedPredicate y ) throws Exception {
        if( y.lft != null ) {
            Relation ret = null;
            try {
                ret = join(join(x,y.lft),y.rgt);
            } catch( AssertionError e ) {
                ret = join(join(x,y.rgt),y.lft);
            }
            if( ret.header.size() != y.header.size() )
                return Relation.innerUnion(ret, new Relation(y.colNames));
            return ret;
        }
        Set<String> header = new TreeSet<String>();
        header.addAll(x.header.keySet());
        header.addAll(y.header.keySet());               
        Relation ret = new Relation(header.toArray(new String[0]));
        Set<String> required = new HashSet<String>();
        required.addAll(y.header.keySet());
        required.removeAll(x.header.keySet());
        Method m = y.method(required);
        for( Tuple tupleX: x.content ) {
            if( m == null ) 
                throw new AssertionError("didn't find a method");
            // postponed to facilitate joining with []
            List<String> inputs = arguments(m, ArgType.INPUT);
            
            Object[] args = new Object[m.getParameterTypes().length];
            int pos = -1;
            for( String origName : inputs ) {
                pos++;
                args[pos] = tupleX.data[x.header.get(y.newName(origName))];
            }
            Object o = null;
            try {
                Class partypes[] = new Class[1];
                partypes[0] = Database.class;
                Constructor ct = y.implementation.getConstructor(partypes);
                Object arglist[] = new Object[1];
                arglist[0] = y.db;
                o = ct.newInstance(arglist);
            } catch( NoSuchMethodException e ) {}
            
            try {
                o = m.invoke(o, args);
            } catch( InvocationTargetException e ) {
                if( e.getCause() instanceof EmptySetException )
                    return ret;
                else
                    throw e;
            }
            
            if( o instanceof NamedTuple ) {
                Object[] retTuple = new Object[header.size()];
                NamedTuple nt = (NamedTuple)o;
                for( String attr : ret.colNames ) {
                    Integer colRet = ret.header.get(attr);
                    Integer colX = x.header.get(attr);                    
                    Integer colY = y.header.get(attr);
                    if( colX != null && colY == null || inputs.contains(y.oldName(attr)) ) 
                        retTuple[colRet] = tupleX.data[colX];
                    else {
                        Object co = nt.get(y.oldName(attr));
                        if( colX != null ) 
                            if( !co.equals(tupleX.data[colX]) ) {
                                retTuple = null;
                                break;
                            }
                        retTuple[colRet] = co;
                        
                    }
                }
                if( retTuple != null )
                    ret.content.add(new Tuple(retTuple));
            } else if( o instanceof Relation ) {
                Relation r = (Relation)o;
                for( Tuple t : r.content ) {
                    Object[] retTuple = new Object[header.size()];
                    for( String attr : ret.colNames ) {
                        Integer colRet = ret.header.get(attr);
                        Integer colX = x.header.get(attr);                    
                        Integer colY = y.header.get(attr);
                        if( colX != null && colY == null || inputs.contains(y.oldName(attr)) ) 
                            retTuple[colRet] = tupleX.data[colX];
                        else {
                            Object co = t.data[r.header.get(y.oldName(attr))];
                            if( colX != null ) 
                                if( !co.equals(tupleX.data[colX]) ) {
                                    retTuple = null;
                                    break;
                                }
                            retTuple[colRet] = co;

                        }
                    }
                    if( retTuple != null )
                        ret.content.add(new Tuple(retTuple));
                }
            } else
                throw new Exception("Wrong return type");
        }
        return ret;
    }
    public static IndexedPredicate innerUnion( Relation x, IndexedPredicate y ) throws Exception {
        if( 0 < x.content.size() )
            throw new Exception("Not a projection: TODO");
        IndexedPredicate ret = new IndexedPredicate(y);
        Set<String> columns = new HashSet<String>();
        columns.addAll(x.header.keySet());
        columns.retainAll(y.header.keySet());
        HashMap<String,Integer> newHdr = new HashMap<String,Integer>();
        String[] newColNames = new String[columns.size()];
        int pos = -1;
        for( String col : columns ) {
            pos++;
            newColNames[pos] = col;
            newHdr.put(col, pos);
        }
        ret.header = newHdr;
        ret.colNames = newColNames;
        return ret; 
    }
    public static Relation setEQ( Relation x, IndexedPredicate y ) throws Exception {
        Set<String> headerXmY = new TreeSet<String>();
        headerXmY.addAll(x.header.keySet());
        headerXmY.removeAll(y.header.keySet());            
        Set<String> headerYmX = new TreeSet<String>();
        headerYmX.addAll(y.header.keySet());
        headerYmX.removeAll(x.header.keySet());
        Set<String> headerSymDiff = new TreeSet<String>();
        headerSymDiff.addAll(headerXmY);
        headerSymDiff.addAll(headerYmX);
        Relation hdrXmY = new Relation(headerXmY.toArray(new String[0]));
        //Relation hdrYmX = new Relation(headerYmX.toArray(new String[0]));
        
        Relation ret = new Relation(headerSymDiff.toArray(new String[0]));
                
        Relation X = Relation.innerUnion(x,hdrXmY);
        
        Set<String> headerXY = new TreeSet<String>();
        headerXY.addAll(x.header.keySet());
        headerXY.retainAll(y.header.keySet());
        Relation hdrYX = new Relation(headerXY.toArray(new String[0]));
        Method m = y.method(headerYmX);
        for( Tuple xi : X.content ) {
            Relation singleX = new Relation(X.colNames);
            singleX.content.add(xi);
            Relation lft = Relation.innerUnion(Relation.join(singleX,x),hdrYX);
            
            Object o = null;
            try {
                Class partypes[] = new Class[1];
                partypes[0] = Database.class;
                Constructor ct = y.implementation.getConstructor(partypes);
                Object arglist[] = new Object[1];
                arglist[0] = y.db;
                o = ct.newInstance(arglist);
            } catch( NoSuchMethodException e ) {}
            
            Relation singleY = ((NamedTuple)m.invoke(o, new Object[] {lft})).toRelation();
            for( String newName : y.renamed.keySet() )
                singleY.renameInPlace(y.renamed.get(newName), newName );
            
            ret = Relation.innerUnion(ret, Relation.join(singleX, singleY)); 
        }        
        return ret;      
    }

}
