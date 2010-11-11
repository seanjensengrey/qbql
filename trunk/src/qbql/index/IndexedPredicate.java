package qbql.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import qbql.lattice.Database;
import qbql.lattice.EqualityPredicate;
import qbql.lattice.Program;
import qbql.lattice.Predicate;
import qbql.lattice.Relation;
import qbql.lattice.Tuple;
import qbql.parser.Lex;
import qbql.parser.LexerToken;
import qbql.parser.Token;
import qbql.util.Util;

public class IndexedPredicate extends Predicate {
    
    Class<?> implementation = null;
    
    private HashMap<String,String> renamed = new HashMap<String,String>(); //new_name->old_name
    private String oldName( String col ) {
        return renamed.get(col);
    }
    private Set<String> newNames( String col ) {
        Set<String> ret = new HashSet<String>();
        for( String newName : renamed.keySet() )
            if( col.equals(oldName(newName)) )
                ret.add(newName);
        return ret;
    }
    
    private Method method( Set<String> output ) {
        Method ret = null;
        for( Method m : methods() ) {
            List<String> args = arguments(m,ArgType.OUTPUT);
            boolean inputsCovered = true;
            for( String i : output )
                if( !args.contains(i) ) {
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
    
    public void renameInPlace( String from, String to ) {       
        super.renameInPlace(from, to);
        
        renamed.put(to, oldName(from));
        renamed.remove(oldName(from));
    }
    public void eqInPlace( String from, String to ) {     
        super.eqInPlace(from, to);
        
        renamed.put(to, oldName(from));
    }

    private Database db = null;
    public IndexedPredicate( Database db, String name ) throws Exception {
        this.db = db;
        if( !narrowPredicate(db, name) && !genericPredicate(db, name) )
        	throw new Exception("Failed to instantiate "+name);
        /*?*/db.addPredicate(name, this);
    }
	
    public IndexedPredicate( IndexedPredicate ip ) {
        super(Util.clone(ip.colNames)); 
        this.db = ip.db;
        implementation = ip.implementation;
        renamed = (HashMap<String, String>) ip.renamed.clone();
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

    
    public static Relation join( Relation x, IndexedPredicate y ) {
        Set<String> header = new TreeSet<String>();
        header.addAll(x.header.keySet());
        header.addAll(y.header.keySet());               
        Relation ret = new Relation(header.toArray(new String[0]));
        Set<String> required = new HashSet<String>();
        for( String s : y.header.keySet() )
            required.add(y.oldName(s));
        for( String s : x.header.keySet() )
            required.remove(y.oldName(s));
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
                for( String s : y.newNames(origName) ) {
                    Integer xHeaderPos = x.header.get(s);
                    if( xHeaderPos != null ) {
                        args[pos] = tupleX.data[xHeaderPos];
                        break;
                    }
                }
            }
            Object o = null;
            try {
                Class partypes[] = new Class[1];
                partypes[0] = Database.class;
                Constructor ct = y.implementation.getConstructor(partypes);
                Object arglist[] = new Object[1];
                arglist[0] = y.db;
                o = ct.newInstance(arglist);
            } catch( NoSuchMethodException e ) {            	
            } catch ( Exception e ) {
				throw new RuntimeException(e);
			}
            
            try {
                o = m.invoke(o, args);
            } catch( InvocationTargetException e ) {
                if( e.getCause() instanceof EmptySetException )
                    return ret;
                else if( e.getCause() instanceof AssertionError )
                	throw (AssertionError)e.getCause();
                else
                	throw new RuntimeException(e);
            } catch ( Exception e ) {
				throw new RuntimeException(e);
			}
            
            if( o instanceof NamedTuple ) {
                Object[] retTuple = new Object[header.size()];
                NamedTuple nt = (NamedTuple)o;
                for( String attr : ret.colNames ) {
                    Integer colRet = ret.header.get(attr);
                    Integer colX = x.header.get(attr);                    
                    //Integer colY = y.header.get(attr);
                    Object co = null;
                    String oldYattr = y.oldName(attr);
                    if( oldYattr != null )
                        co = nt.get(oldYattr);
                    if( co != null ) {
                        if( colX != null ) 
                            if( !co.equals(tupleX.data[colX]) ) {
                                retTuple = null;
                                break;
                            }
                        retTuple[colRet] = co;                        
                    } else if( colX != null ) {
                        retTuple[colRet] = tupleX.data[colX]; 
                    } else {
                        for( String eqCol : y.newNames(y.oldName(attr)) ) {
                            colX = x.header.get(eqCol);
                            if( colX != null ) {
                                retTuple[colRet] = tupleX.data[colX];
                            }
                        }
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
                        //Integer colY = y.header.get(attr);
                        Integer colR = r.header.get(y.oldName(attr));
                        if( colR != null ) {
                            Object co = t.data[colR];
                            if( colX != null ) 
                                if( !co.equals(tupleX.data[colX]) ) {
                                    retTuple = null;
                                    break;
                                }
                            retTuple[colRet] = co;                            
                        } else if( colX != null ) {
                            retTuple[colRet] = tupleX.data[colX]; 
                        } else {
                            for( String eqCol : y.newNames(y.oldName(attr)) ) {
                                colX = x.header.get(eqCol);
                                if( colX != null ) {
                                    retTuple[colRet] = tupleX.data[colX];
                                }
                            }
                        }                        
                    }
                    if( retTuple != null )
                        ret.content.add(new Tuple(retTuple));
                }
            } else
                throw new RuntimeException("Wrong return type");
        }
        return ret;
    }
    
    public static IndexedPredicate union( Relation x, IndexedPredicate y )  {
        if( 0 < x.content.size() )
            throw new AssertionError("Not a projection: TODO");
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
    
    public static Relation setEQ( Relation x, IndexedPredicate y ) {
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
                
        Relation X = Relation.union(x,hdrXmY);
        
        Set<String> headerXY = new TreeSet<String>();
        headerXY.addAll(x.header.keySet());
        headerXY.retainAll(y.header.keySet());
        Relation hdrYX = new Relation(headerXY.toArray(new String[0]));
        
        
        Set<String> required = new HashSet<String>();
        for( String s : y.header.keySet() )
            required.add(y.oldName(s));
        for( String s : x.header.keySet() )
            required.remove(y.oldName(s));
        Method m = y.method(required);
        for( Tuple xi : X.content ) {
            Relation singleX = new Relation(X.colNames);
            singleX.content.add(xi);
            Relation lft = Relation.union(Relation.join(singleX,x),hdrYX);
            
            Object o = null;
            try {
                Class partypes[] = new Class[1];
                partypes[0] = Database.class;
                Constructor ct = y.implementation.getConstructor(partypes);
                Object arglist[] = new Object[1];
                arglist[0] = y.db;
                o = ct.newInstance(arglist);
            } catch( NoSuchMethodException e ) {            	
            } catch ( Exception e ) {
				throw new RuntimeException(e);
			}
            
            try {
                Relation singleY = ((NamedTuple)m.invoke(o, new Object[] {lft})).toRelation();
                for( String newName : y.renamed.keySet() )
                	singleY.renameInPlace(y.renamed.get(newName), newName );

                ret = Relation.union(ret, Relation.join(singleX, singleY)); 
            } catch( InvocationTargetException e ) {
                /*if( e.getCause() instanceof EmptySetException )
                    return ret;
                else*/
                	throw new RuntimeException(e);
            } catch ( Exception e ) {
				throw new RuntimeException(e);
			}
        }        
        return ret;      
    }

    
    private boolean narrowPredicate( Database db, String name ) {
    	Set<String> tmp = new TreeSet<String>();
    	try {
			implementation = Class.forName(db.pkg+"."+name);
		} catch( ClassNotFoundException e ) {
			return false;
		}
    	for( Method m : methods() ) {
    		tmp.addAll(arguments(m,ArgType.BOTH));
    	}
    	colNames = new String[tmp.size()];
    	int pos = 0;
    	for( String s : tmp ) {
    		colNames[pos] = s;
    		header.put(s,pos);
    		renamed.put(s, s);
    		pos++;
    	}
    	return true;
    }
    
    String[] files = null;
    private boolean genericPredicate( Database db, String predicate ) {
    	// http://www.javaworld.com/javaworld/javatips/jw-javatip113.html
    	if( files == null ) {
            String name = db.pkg;
            if( !name.startsWith("/") ) {
            	name = "/" + name;
            }        
            name = name.replace('.','/');

            URL url = IndexedPredicate.class.getResource(name);
            File directory = new File(url.getFile());
            if( directory.exists() ) {
            	files = directory.list();
            } else {
            	String jarName = directory.getPath();
            	jarName = jarName.substring("file:\\".length());
            	jarName = jarName.substring(0,jarName.indexOf('!'));
             	try {
					files = getClassesFromJARFile(jarName,name.substring(1)).toArray(new String[0]);
				} catch( IOException e ) {
					System.err.println(e.getMessage());
				}
            }
    	}
        for( int i=0; i<files.length; i++ ) {
            if( files[i].endsWith(".class") ) {
                String classname = files[i].substring(0,files[i].length()-6);
                try {
                    Class c = Class.forName(db.pkg+"."+classname);
                    Method getSymbolicNameMethod = c.getDeclaredMethod("getSymbolicNames");
                    String[] candidates = (String[])getSymbolicNameMethod.invoke(null);
                    for( String candidate: candidates ) {
                    	Map<String,String> matched = matchNames(candidate, predicate);
                    	if( matched == null )
                    		continue;
                    	implementation = c;
                    	Set<String> tmp = new TreeSet<String>();
                    	for( Method m : methods() ) {
                    		tmp.addAll(arguments(m,ArgType.BOTH));
                    	}
                    	colNames = new String[tmp.size()];
                    	int pos = 0;
                    	for( String s : tmp ) {
                    		String t = matched.get(s);
                    		colNames[pos] = t;
                    		header.put(t,pos);
                    		renamed.put(t, s);
                    		pos++;
                    	}
                    	return true;
                    }
                } catch ( Exception e ) {
                }
            }
        }
        return false;
    }
    
    private static List<String> getClassesFromJARFile( String jar, String packageName ) throws IOException {
    	final List<String> classes = new LinkedList<String>();
    	JarInputStream jarFile = null;
    	try {
    		jarFile = new JarInputStream(new FileInputStream(jar));
    		for( JarEntry jarEntry = jarFile.getNextJarEntry(); jarEntry != null ;jarEntry = jarFile.getNextJarEntry() ) {
    			String className = jarEntry.getName();
    			if( className.endsWith(".class") ) {
    				if (className.startsWith(packageName))
    					classes.add(className.substring(packageName.length()+1));
    			}    			
    		}
    	} finally {
    		if( jarFile != null )
    			jarFile.close();
    	}
    	return classes;
    }
    
    protected IndexedPredicate clone() {
        return new IndexedPredicate(this);
    }

}
