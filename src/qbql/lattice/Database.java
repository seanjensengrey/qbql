package qbql.lattice;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import qbql.parser.CYK;
import qbql.parser.Lex;
import qbql.parser.LexerToken;
import qbql.parser.Matrix;
import qbql.parser.ParseNode;
import qbql.util.Util;

public class Database {

    private Map<String,Predicate> lattice = new TreeMap<String,Predicate>();
    public Predicate predicate( String name ) {
        return lattice.get(name);
    }
    public void addPredicate( String name, Predicate relvar ) {
        lattice.put(name, relvar);
    }       
    public void removePredicate( String name ) {
        lattice.remove(name);
    }       
    public String[] relNames() {
        Set<String> ret = new TreeSet<String>();
        for( String pred : lattice.keySet() )
            if( lattice.get(pred) instanceof Relation )
                ret.add(pred);
        return ret.toArray(new String[0]);
    }       
    
    static Relation R00 = new Relation(new String[]{});
    Relation R11;
    static Relation R01 = new Relation(new String[]{});
    Predicate R10;

    static {
        R01.addTuple(new TreeMap<String,Object>());
    }

    public String pkg = null;    
    /*public Database() {                         
        addPredicate("R00",Database.R00); 
        addPredicate("R01",Database.R01);
        StackTraceElement[] stack = new Throwable().getStackTrace();
        String createdInClass = stack[1].getClassName();
        pkg = createdInClass.substring(0,createdInClass.lastIndexOf('.'));
    }*/
    public Database( String pkg ) {                         
        addPredicate("R00",Database.R00); 
        addPredicate("R01",Database.R01);
        this.pkg = pkg;
    }
    

    Relation outerUnion( Relation x, Relation y ) {
        return Relation.innerUnion( Relation.join(x, Relation.innerUnion(y, R11))
                                    , Relation.join(y, Relation.innerUnion(x, R11)) 
        );
    }

    /**
     * Generalized set intersection and set union
     */
    Relation quantifier( Relation x, Relation y, int type ) throws Exception {
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
        Relation hdrYmX = new Relation(headerYmX.toArray(new String[0]));
        
        Relation ret = new Relation(headerSymDiff.toArray(new String[0]));
                
        Relation X = Relation.innerUnion(R11,hdrXmY);
        Relation Y = Relation.innerUnion(R11,hdrYmX);
        
        Relation hdrYX = Relation.innerUnion(Relation.join(R00,x),Relation.join(R00,y));
        for( Tuple xi : X.content ) {
            Relation singleX = new Relation(X.colNames);
            singleX.content.add(xi);
            Relation lft = Relation.innerUnion(Relation.join(singleX,x),hdrYX); 
            for( Tuple yi : Y.content ) {
                Relation singleY = new Relation(Y.colNames);
                singleY.content.add(yi);
                Relation rgt = Relation.innerUnion(Relation.join(singleY,y),hdrYX);
                if( type == Program.contains && Relation.le(lft, rgt) 
                 || type == Program.transpCont && Relation.ge(lft, rgt)   
                 || type == Program.disjoint && Relation.le(lft, (Relation)complement(rgt)) 
                 || type == Program.almostDisj && Relation.join(lft, rgt).content.size()==1 
                 || type == Program.big && Relation.ge(lft, (Relation)complement(rgt))   
                 || type == Program.setEQ && lft.equals(rgt)
                )
                    ret = Relation.innerUnion(ret, Relation.join(singleX, singleY));
                /*if( type == Grammar.unison && lft.equals(rgt) ) {
                    if( x.colNames.length != y.colNames.length )
                        return ret;

                    ret = Relation.innerUnion(ret, Relation.join(singleX, singleY));
                }*/
            }
        }        
        return ret;
    }
    
    Map<String,Boolean> finiteDomains = new HashMap<String,Boolean>();
    /**
     * Complement x' returns relation with the same header as x
     * with tuples which are not in x
     * Axioms:
        x' ^ x = x ^ R00.
        x' v x = x v R11.
     */
    Predicate complement( Relation x ) {
        for( String arg : x.colNames ) {
            Boolean isFinite = finiteDomains.get(arg);
            if( isFinite == null ) {
                Relation a = Relation.innerUnion(R11,new Relation(new String[]{arg}));
                isFinite = 0 < a.content.size() & R11.header.keySet().contains(arg);
                finiteDomains.put(arg, isFinite);
                if( !isFinite )
                    return new ComplementPredicate(x);
            }
        }
        
        Relation xvR11 = Relation.innerUnion(x, R11);
        Relation ret = new Relation(x.colNames);
        for( Tuple t : xvR11.content ) {
            boolean matched = false;
            for( Tuple tx : x.content )
                if( t.equals(tx, x, ret) ) {
                    matched = true;
                    break;
                }
            if( !matched )
                ret.content.add(t);
        }
        return ret;
    }

    /**
     * Inverse x` 
     * Axioms:
        x` ^ x = x ^ R11.
        x` v x = x v R00. 
    */ 
    Relation inverse( Relation x ) {
        String[] header = new String[R10.colNames.length-x.colNames.length];
        int i = -1;
        for( String attr : R10.colNames ) {
            if( x.header.get(attr) == null ) {
                i++;
                header[i] = attr;
            }
        }
        if( x.content.size() == 0 )
            return new Relation(header);
        else
            return Relation.innerUnion(new Relation(header),R11);
    }
    
    void buildR10() throws Exception {
        Predicate ret = new Relation(new String[]{});
        for( Predicate r : lattice.values() )
            ret = Predicate.join(ret, r);
        R10 = ret;
        lattice.put("R10",ret);
    }

    void buildR11() {
        Map<String, Relation> domains = new HashMap<String, Relation>();
        for( String col : R10.colNames )
            domains.put(col, new Relation(new String[]{col}));

        for( Predicate rel : lattice.values() )
            if( rel instanceof Relation )
                for( Tuple t : ((Relation)rel).content )
                    for( int i = 0; i < t.data.length; i++ ) {
                        Tuple newTuple = new Tuple(new Object[]{t.data[i]});
                        domains.get(rel.colNames[i]).content.add(newTuple);
                    }

        /* Bad performance for large db
        Relation ret = R01;
        for( Relation domain : domains.values() )
            ret = Relation.join(ret, domain);
        return ret;
        */
        
        Map<String, Object[]> doms = new TreeMap<String, Object[]>();
        for( String r : domains.keySet() ) {
            Set<Tuple> tuples = domains.get(r).content;
            Object[] content = new Object[tuples.size()];
            int i = 0;
            for( Tuple t : tuples )
                content[i++] = t.data[0];
            doms.put(r, content);
        }
        
        Relation ret = new Relation(doms.keySet().toArray(new String[0]));
        
        try {
            Map<String, Integer> indexes = new HashMap<String, Integer>();
            for (String key: doms.keySet())
                indexes.put(key, 0);
            do {
                Object[] t = new Object[ ret.colNames.length ];
                for (String key: doms.keySet())
                    t[ ret.header.get(key) ] = doms.get(key)[ indexes.get(key) ];

                ret.content.add(new Tuple(t));

            } while (next(indexes, doms));
        } catch( Exception e ) { // for empty domains
        }        
        R11 = ret;
        lattice.put("R11",ret);
    }    
    private boolean next( Map<String, Integer> state, Map<String, Object[]> doms ) {
        for( String pos: state.keySet() ) {
            int rownum = state.get(pos);
            if( rownum < doms.get(pos).length-1 ) {
                state.put(pos, rownum+1);
                return true;
            }
            state.put(pos, 0);
        }
        return false;
    }
    
    public boolean equivalent( Relation x, Relation y ) {
        if( x == y )
            return true;
        if( x.content.size() != y.content.size() )
            return false;
        
        if( !submissive(x,y) )
            return false;
        return submissive(y,x);
    } 
    
    public boolean submissive( Relation x, Relation y ) {
        
        if( y.colNames.length == 0 ) { // indexing is broken this case
            if( y.content.size() == 0  )
                return x.content.size() == 0;
            else
                return Relation.innerUnion(x,R11).equals(x);
        }
            
        int[] indexes = new int[x.colNames.length];
        for( int i = 0; i < indexes.length; i++ )
            indexes[i] = 0;
        do {
            boolean matchedAllRows = true;
            for ( Tuple tx: x.content ) { 
                boolean matchedX = false;
                for ( Tuple ty: y.content ) {
                    boolean mismatchedField = false;
                    for( int i = 0; i < indexes.length; i++ ) {
                        if( !tx.data[i].equals(ty.data[indexes[i]]) ) {
                            mismatchedField = true;
                            break;
                        }
                    }
                    if( mismatchedField )
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
        } while ( Util.next(indexes,y.colNames.length) );

        return false;
    }
        
    public Relation unnamedJoin( Relation x, Relation y ) {
        
        class Candidate {
            int[] indexes;
            Relation ret;
            public Candidate( Relation x, Relation y ) {
                indexes = new int[y.colNames.length];
                for( int i = 0; i < indexes.length; i++ )
                    indexes[i] = -1;
                ret = Relation.join(x,Relation.renameCols(x, y, indexes));
            }
            private Candidate( int[] indexes, Relation ret ) {
                this.indexes = indexes;
                this.ret = ret;
            }
            void spawn( Relation x, Relation y, int i, int j, 
                        List<Candidate> additions, List<Candidate> removals ) {
                int[] incr = new int[indexes.length];
                for( int k = 0; k < incr.length; k++ ) {
                    incr[k] = indexes[k];
                }
                incr[j] = i;
                Relation join = Relation.join(x,Relation.renameCols(x, y, incr));
                if( submissive(join,ret) 
                 && submissive(x,join) 
                 && submissive(y,join) 
                ) { 
                    boolean exists = false;
                    for( Candidate c: additions ) {
                        if( c.ret.equals(join) )
                            exists = true;
                    }
                    if( !exists )
                        additions.add( new Candidate(incr,join));
                    exists = false;
                    for( Candidate c: removals ) {
                        if( c.ret.equals(ret) )
                            exists = true;
                    }
                    if( !exists )
                        removals.add(this);
                }               
            }
        }
        
        //if( x.equals(R11) && y.equals(R11) ) // performance shortcut
            //return R11;
        
        List<Candidate> candidates = new LinkedList<Candidate>();
        candidates.add(new Candidate(x,y));

        for( int j = 0; j < y.colNames.length; j++ ) {
//System.out.println("j="+j);
            List<Candidate> additions = new LinkedList<Candidate>();
            List<Candidate> removals = new LinkedList<Candidate>();
            for( int i = 0; i < x.colNames.length; i++ ) {
//System.out.println("i="+i);
//if( j==2 && i==0 )
//System.out.println("*****");    
                if( !Relation.match(x,y,i,j) )
                    continue;
                for( Candidate src : candidates ) 
                    src.spawn(x, y, i, j, additions, removals);
            }
            candidates.removeAll(removals);
            candidates.addAll(additions); 
        }
        
        Relation ret = null;
        for( Candidate c : candidates )
            if( ret == null || submissive(c.ret, ret) )
                ret = c.ret;
        return ret;
    }

    public static Relation unnamedMeet( Relation x, Relation y ) {
        throw new RuntimeException("Not impl");
    }
    
    public static Database init( String dbSource ) throws Exception {
        Database database = new Database("qbql.lang");
        List<LexerToken> src =  new Lex().parse(dbSource);
        Matrix matrix = Program.cyk.initArray1(src);
        int size = matrix.size();
        TreeMap<Integer,Integer> skipRanges = new TreeMap<Integer,Integer>();
        Program.cyk.closure(matrix, 0, size+1, skipRanges, -1);
        ParseNode root = Program.cyk.forest(size, matrix);

        if( root.topLevel != null ) {
            System.out.println("*** Parse Error in database file ***");
            CYK.printErrors(dbSource, src, root);
            throw new Exception("Parse Error");
        }

        Program dbPrg = new Program(src,database);
        dbPrg.program(root);

        database.buildR10();
        database.buildR11();

        // relations that requre complement can be built only after R10 and R11 are defined
        try {
            database.addPredicate("UJADJBC'",database.complement((Relation)(database.predicate("UJADJBC"))));
        } catch( Exception e ) { // NPE if databaseFile is not Figure1.db
        }
        return database;
    }
    
    public static Database run( String dbSrc, String prg ) throws Exception {
        List<LexerToken> src =  new Lex().parse(prg);
        Matrix matrix = Program.cyk.initArray1(src);
        int size = matrix.size();
        TreeMap<Integer,Integer> skipRanges = new TreeMap<Integer,Integer>();
        Program.cyk.closure(matrix, 0, size+1, skipRanges, -1);
        ParseNode root = Program.cyk.forest(size, matrix);

        if( root.topLevel != null ) {
            System.out.println("*** Parse Error in assertions file ***");
            CYK.printErrors(prg, src, root);
            return null;
        }

        Database db = init(dbSrc);
        Program program = new Program(src,db); 
        long t1 = System.currentTimeMillis();
        ParseNode exception = program.program(root);
        long t2 = System.currentTimeMillis();
        System.out.println("Time = "+(t2-t1)); 
        if( exception != null ) {
            System.out.println("*** False Assertion ***");
            System.out.println(prg.substring(src.get(exception.from).begin, src.get(exception.to-1).end));
        }
        return db;
    }

}
