package qbql.lattice;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import qbql.parser.CYK;
import qbql.parser.LexerToken;
import qbql.parser.Matrix;
import qbql.parser.ParseNode;
import qbql.parser.RuleTuple;
import qbql.parser.Token;
import qbql.util.Util;

public class Database {

    Map<String,Relation> lattice = new TreeMap<String,Relation>();
    static Relation R00 = new Relation(new String[]{});
    Relation R11;
    static Relation R01 = new Relation(new String[]{});
    Relation R10;

    static {
        R01.addTuple(new TreeMap<String,String>());

        LexerToken.isPercentLineComment = true;
    }

    final static String databaseFile = "Figure1.db"; 
    final static String programFile = "Figure1.prg"; 
    //final static String databaseFile = "Wittgenstein.db"; 
    //final static String programFile = "Wittgenstein.assertions"; 
    //final static String databaseFile = "Sims.db"; 
    //final static String programFile = "Sims.assertions"; 
    //final static String databaseFile = "Aggregate.db"; 
    //final static String programFile = "Aggregate.prg"; 
    private static final String path = "/qbql/lattice/";
    public Database() throws Exception {				
        String database = Util.readFile(getClass(),path+databaseFile);

        List<LexerToken> src =  LexerToken.parse(database);
        Matrix matrix = cyk.initArray1(src);
        int size = matrix.size();
        TreeMap<Integer,Integer> skipRanges = new TreeMap<Integer,Integer>();
        cyk.closure(matrix, 0, size+1, skipRanges, -1);
        ParseNode root = cyk.forest(size, matrix);

        if( root.topLevel != null ) {
            System.out.println("*** Parse Error in database file ***");
            CYK.printErrors(database, src, root);
            throw new Exception("Parse Error");
        }

        lattice.put("R00",R00);	
        lattice.put("R01",R01);

        database(root,src);

        R10 = buildR10();
        R11 = buildR11();
        lattice.put("R10",R10);
        lattice.put("R11",R11);

        // relations that requre complement can be built only after R10 and R11 are defined
        try {
            lattice.put("UJADJBC'",complement(lattice.get("UJADJBC")));
        } catch( Exception e ) { // NPE if databaseFile is not Figure1.db
        }

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
        
        Relation bind = Relation.innerUnion(x, y); 
        Relation xjy = Relation.join(x, y);
              
        Relation ret = new Relation(headerSymDiff.toArray(new String[0]));
        Relation X = Relation.innerUnion(x,hdrXmY);
        Relation Y = Relation.innerUnion(y,hdrYmX);
        if( type == forAll )
            ret = Relation.join(X,Y);
        for( Tuple b : bind.content ) {
            Relation singleValue = new Relation(bind.colNames);
            singleValue.content.add(b);
            Relation summand = Relation.innerUnion(
                Relation.join(xjy, singleValue),
                Relation.join(
                    Relation.join(
                        X,
                        complement(Relation.innerUnion(Relation.join(x, singleValue),hdrXmY))
                    ),
                    Relation.join(
                        Y,
                        complement(Relation.innerUnion(Relation.join(y, singleValue),hdrYmX))
                    )
                )
            );
            if( type == exists )
                ret = Relation.innerUnion(ret, summand);
            else if( type == forAll )
                ret = Relation.join(ret, summand);
            else
                throw new Exception("Unknown quantifier type");
        }
        
        return ret;
    }
    /**
     * Complement x' returns relation with the same header as x
     * with tuples which are not in x
     * Axioms:
        x' ^ x = x ^ R00.
        x' v x = x v R11.
     */
    Relation complement( Relation x ) {
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
    
    Relation buildR10() {
        Relation ret = new Relation(new String[]{});
        for( Relation r : lattice.values() )
            ret = Relation.join(ret, r);
        return ret;	
    }

    Relation buildR11() {
        Map<String, Relation> domains = new HashMap<String, Relation>();
        for( String col : R10.colNames )
            domains.put(col, new Relation(new String[]{col}));

        for( Relation rel : lattice.values() ) 
            for( Tuple t : rel.content )
                for( int i = 0; i < t.data.length; i++ ) {
                    Tuple newTuple = new Tuple(new String[]{t.data[i]});
                    domains.get(rel.colNames[i]).content.add(newTuple);
                }

        /* Bad performance for large db
        Relation ret = R01;
        for( Relation domain : domains.values() )
            ret = Relation.join(ret, domain);
        return ret;
        */
        
        Map<String, String[]> doms = new TreeMap<String, String[]>();
        for( String r : domains.keySet() ) {
            Set<Tuple> tuples = domains.get(r).content;
            String[] content = new String[tuples.size()];
            int i = 0;
            for( Tuple t : tuples )
                content[i++] = t.data[0];
            doms.put(r, content);
        }
        
        Relation ret = new Relation(doms.keySet().toArray(new String[0]));
        
        Map<String, Integer> indexes = new HashMap<String, Integer>();
        for( String key : doms.keySet() )
            indexes.put(key, 0);
        do {
            String[] t = new String[ret.colNames.length];
            for( String key : doms.keySet() )
                t[ ret.header.get(key) ] = doms.get(key)[ indexes.get(key) ];
            
            ret.content.add(new Tuple(t));
            
        } while( next(indexes,doms) );
        
        return ret;
    }
    private boolean next( Map<String, Integer> state, Map<String, String[]> doms ) {
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

    /*public Relation eval( String input ) throws Exception {
        List<LexerToken> src =  LexerToken.parse(input);
        Matrix matrix = cyk.initArray1(src);
        int size = matrix.size();
        TreeMap<Integer,Integer> skipRanges = null;//new TreeMap<Integer,Integer>();
        cyk.closure(matrix, 0, size+1, skipRanges, -1);
        ParseNode root = cyk.forest(size, matrix);
        return eval(root,src);
    }*/
    private Relation eval( ParseNode node, List<LexerToken> src ) throws Exception {
        if( node.from + 1 == node.to ) {
            Relation ret = lattice.get(src.get(node.from).content);
            if( ret == null )
                throw new Exception("There is no relation "+src.get(node.from).content+" in the database");
            return ret;
        }

        Relation x = null;
        Relation y = null;
        boolean parenGroup = false;
        for( ParseNode child : node.children() ) {
            if( parenGroup )
                return eval(child, src);
            else if( child.contains(openParen) )
                parenGroup = true;
            else if( child.contains(relation) 
                  || child.contains(expr) 
                  || child.contains(identifier) 
                  || child.contains(parExpr) 
            ) {
                if( x == null )
                    x = compute(child,src);
                else
                    y = compute(child,src);
            } 
        }
        if( node.contains(join) ) 
            return Relation.join(x,y);
        if( node.contains(innerJoin) ) 
            return Relation.innerJoin(x,y);
        if( node.contains(outerUnion) ) 
            return outerUnion(x,y);
        if( node.contains(innerUnion) ) 
            return Relation.innerUnion(x,y);
        if( node.contains(unison) ) 
            return Relation.unison(x,y);
        if( node.contains(complement) ) 
            return complement(x);
        if( node.contains(inverse) ) 
            return inverse(x);
        if( node.contains(exists) ) 
            return quantifier(x,y,exists);
        if( node.contains(forAll) ) 
            return quantifier(x,y,forAll);
        return null;
    }
    
    public boolean bool( ParseNode root, List<LexerToken> src ) throws Exception {
        for( ParseNode child : root.children() ) 
            if( child.contains(expr) )
                return boolExpr(root, src);
            else 
                return logical(root, src);
        throw new Exception("Impossible exception, no children??"+root.content(src));
    }

    public boolean logical( ParseNode root, List<LexerToken> src ) throws Exception {
        Boolean left = null;
        Boolean right = null;
        int oper = -1;
        for( ParseNode child : root.children() ) {
            if( left == null ) {
                if( child.contains(minus) ) {
                    oper = minus;
                    left = true;
                } else if( child.contains(openParen) ) {
                        oper = openParen;
                        left = true;
                } else 
                    left = bool(child,src);
            } else if( child.contains(amp) ) 
                oper = amp;
            else if( child.contains(bar) ) 
                oper = bar;
            else {                               
                right = bool(child,src);
                break;    // e.g.   "(" "x = y" ")"
                          // break after ^^^^^
            }
        }
        if( oper == amp )
            return left & right;
        else if( oper == bar )
            return left | right;
        else if( oper == minus )
            return ! right;
        else if( oper == openParen )
            return right;
        throw new Exception("Unknown boolean operation "+oper);
    }
        
    public boolean boolExpr( ParseNode root, List<LexerToken> src ) throws Exception {
        int oper = -1;
        Relation left = null;
        Relation right = null;
        boolean not = false;
        for( ParseNode child : root.children() ) {
            if( left == null )
                left = compute(child,src);
            else if( child.contains(excl) )
                not = true;
            else if( child.contains(equality) )
                oper = equality;
            else if( child.contains(lt) )
                oper = lt;
            else if( child.contains(gt) )
                oper = gt;
            else if( child.contains(equivalence) )
                oper = equivalence;
            else 				
                right = compute(child,src);
        }
        if( oper == equality )
            return not ? !left.equals(right) : left.equals(right);
        if( oper == lt )
            return Relation.le(left,right);
        if( oper == gt )
            return Relation.ge(left,right);
        if( oper == equivalence )
            return Relation.equivalent(left,right);

        throw new Exception("Impossible case");		
    }
    
    public ParseNode implication( ParseNode root, List<LexerToken> src ) throws Exception {
        ParseNode left = null;
        ParseNode right = null;
        boolean impl = false;
        boolean bimpl = false;
        for( ParseNode child : root.children() ) {
            if( left == null ) {
                left = child;
            } else if( child.contains(gt)||child.contains(minus)||child.contains(lt) ) {
                if( child.contains(gt) )
                    impl = true;
                if( child.contains(lt) )
                    bimpl = true;
            } else 				
                right = child;
        }		
        if( impl && !bimpl && boolImpl(left,right,src) ) 
            return null;
        else if( !impl && bimpl && boolImpl(right,left,src) ) 
            return null;
        else if( impl && bimpl && boolImpl(left,right,src) && boolImpl(right,left,src) ) 
            return null;
        else
            return root;
    }
    private boolean boolImpl( ParseNode left,ParseNode right, List<LexerToken> src ) throws Exception {
        boolean l = bool(left,src);
        if( !l ) // optimization: early termination
            return true;
        return bool(right,src);
    }

    private boolean next( int[] state, int limit ) {
        for( int pos = 0; pos < state.length; pos++ ) {
            if( state[pos] < limit-1 ) {
                state[pos]++;
                return true;
            }
            state[pos] = 0;				
        }
        return false;
    }

    public ParseNode assertion( ParseNode root, List<LexerToken> src ) throws Exception {
        String[] tables = lattice.keySet().toArray(new String[0]);

        Set<String> variables = new HashSet<String>();
        for( ParseNode descendant : root.descendants() ) {
            String id = descendant.content(src);
            if( descendant.from+1 == descendant.to 
             && (descendant.contains(expr) || descendant.contains(identifier))
             && lattice.get(id) == null ) 
                variables.add(id);
        }

        int[] indexes = new int[variables.size()];
        for( int i = 0; i < indexes.length; i++ )
            indexes[i] = 0;
        do {
            int var = 0;
            for( String variable : variables ) {
                lattice.put(variable, lattice.get(tables[indexes[var++]]));
            }

            
            for( ParseNode child : root.children() ) {
                if( child.contains(bool) ) {
                    if( !bool(child,src) ) {
                        for( String variable : variables )
                            System.out.println(variable+" = "
                                               +lattice.get(variable).toString(variable.length()+3, false)
                                               +";");
                        return child;
                    }
                } else if( child.contains(implication) ) {
                    ParseNode ret = implication(child,src);
                    if( ret != null ) {
                        for( String variable : variables )
                            System.out.println(variable+" = "
                                               +lattice.get(variable).toString(variable.length()+3, false)
                                               +";");
                        return ret;
                    }
                } 
            }
        } while( next(indexes,tables.length) );

        
        for( String variable : variables )
            lattice.remove(variable);

        return null;
    }

    public ParseNode query( ParseNode root, List<LexerToken> src ) throws Exception {
        for( ParseNode child : root.children() ) {
            if( child.contains(expr) ) {
                System.out.println(child.content(src)+"="+compute(child,src).toString(child.content(src).length()+1, false)+";");
                return null;
            } 
        }
        throw new Exception("No expr in statement?");
    }
    /**
     * @param root
     * @param src
     * @return node that violates assertion
     * @throws Exception
     */
    public ParseNode program( ParseNode root, List<LexerToken> src ) throws Exception {
        if( root.contains(assertion) )
            return assertion(root,src);
        if( root.contains(query) )
            return query(root,src);
        if( root.contains(assignment) ) {
            createRelation(root,src);
            return null;
        }
        ParseNode ret = null;
        for( ParseNode child : root.children() ) {
            ret = program(child,src);	
            if( ret != null )
                return ret;
        }
        return ret;
    }


    public void database( ParseNode root, List<LexerToken> src ) throws Exception {
        if( root.contains(assignment) )
            createRelation(root,src);
        else
            for( ParseNode child : root.children() ) {				
                if( child.contains(assignment) )
                    createRelation(child,src);
                else 
                    database(child,src);
            }
    }
    public void createRelation( ParseNode root, List<LexerToken> src ) throws Exception {
        String left = null;
        Relation right = null;
        for( ParseNode child : root.children() ) {
            if( left == null )
                left = child.content(src);
            else if( child.contains(equality) )
                ;
            else { 				
                right = compute(child,src);
                break;
            }
        }
        lattice.put(left, right);
    }
    public Relation compute( ParseNode root, List<LexerToken> src ) throws Exception {
        if( root.contains(relation) ) {
            for( ParseNode child : root.children() ) {
                if( child.contains(tuples) )
                    return tuples(child,src);
            }
        } else if( root.contains(table) ) {
            Relation ret = null;
            for( ParseNode child : root.children() ) {
                if( child.contains(header) )                    
                    ret = new Relation(strings(child,src).toArray(new String[0]));
                else if( child.contains(content) ) {
                    addContent(ret,child,src);
                    return ret;
                }                 
            } 
            return ret;
        } else
            return eval(root,src);
        throw new Exception("Unknown case");
    }
    public Relation tuples( ParseNode root, List<LexerToken> src ) throws Exception {
        Set<String> attrs = new TreeSet<String>();
        for( ParseNode descendant: root.descendants() )
            if( descendant.contains(attribute) )
                attrs.add(descendant.content(src));
        Relation ret = new Relation(attrs.toArray(new String[0]));

        addTuples(ret,root,src);		
        return ret;
    }
    private void addTuples( Relation ret, ParseNode root, List<LexerToken> src ) throws Exception {
        if( root.contains(tuple) ) 
            ret.addTuple(tuple(root,src));
        else for( ParseNode child : root.children() )
            if( child.contains(tuple) )
                ret.addTuple(tuple(child,src));
            else if( child.contains(tuples) )
                addTuples(ret,child,src);
    }
    private Map<String,String> tuple( ParseNode root, List<LexerToken> src ) throws Exception {
        for( ParseNode child : root.children() ) {
            if( child.contains(values) ) {
                Map<String,String> tuple = new TreeMap<String,String>(); 
                values(tuple, child,src);
                return tuple;
            }
        }
        throw new Exception("Unknown case");
    }
    private void values( Map<String,String> tuple, ParseNode root, List<LexerToken> src ) {
        if( root.contains(namedValue) )
            value(tuple,root,src);
        else for( ParseNode child : root.children() )
            if( child.contains(namedValue) )
                value(tuple,child,src);
            else if( child.contains(values) )
                values(tuple,child,src);
    }
    public void value( Map<String,String> tuple, ParseNode root, List<LexerToken> src ) {
        String left = null;
        String right = null;
        for( ParseNode child : root.children() ) {
            if( left == null )
                left = child.content(src);
            else if( child.contains(equality) )
                ;
            else 				
                right = child.content(src);
        }
        tuple.put(left, right);
    }
    
    private List<String> strings( ParseNode root, List<LexerToken> src ) throws Exception {
        List<String> ret = new LinkedList<String>();
        if( root.from + 1 == root.to && 
            (src.get(root.from).type == Token.IDENTIFIER || src.get(root.from).type == Token.DIGITS )
        )
            ret.add(root.content(src));
        else
            for( ParseNode child : root.children() )
                ret.addAll(strings(child, src));
        return ret;
    }
    private void addContent( Relation ret, ParseNode root, List<LexerToken> src ) throws Exception {
        int i = 0;
        String[] t = new String[ret.colNames.length];
        for( String elem : strings(root, src) ) {
            t[i%ret.colNames.length] = elem;
            if( i%ret.colNames.length == ret.colNames.length-1 ) {
                ret.content.add(new Tuple(t));
                t = new String[ret.colNames.length];
            }
            i++;
        }
    }

    static CYK cyk;
    static int join;
    static int innerJoin;
    static int innerUnion;
    static int outerUnion;
    static int unison;
    static int exists;
    static int forAll;
    static int complement;
    static int inverse;
    static int equivalence;
    static int equality;
    static int minus;
    static int expr;
    static int parExpr;
    static int openParen;
    static int bool;
    static int implication;
    static int lt;
    static int gt;
    static int amp;
    static int bar;
    static int excl;
    static int assertion;
    static int query;
    static int identifier;

    static int assignment;
    static int relation;
    static int table;
    static int tuples;
    static int tuple;
    static int header;
    static int content;
    static int attribute;
    static int values;
    static int namedValue;
    static int comma;
    private static final String bnf = "grammar.serializedBNF";
    static {
        try {
            cyk = new CYK(RuleTuple.getRules(path+bnf)) {
                public int[] atomicSymbols() {
                    return new int[] {assertion};
                }
            };
            join = cyk.symbolIndexes.get("join");
            innerJoin = cyk.symbolIndexes.get("innerJoin");
            innerUnion = cyk.symbolIndexes.get("innerUnion");
            outerUnion = cyk.symbolIndexes.get("outerUnion");
            unison = cyk.symbolIndexes.get("unison");
            exists = cyk.symbolIndexes.get("exists");
            forAll = cyk.symbolIndexes.get("forAll");
            complement = cyk.symbolIndexes.get("complement");
            inverse = cyk.symbolIndexes.get("inverse");
            equivalence = cyk.symbolIndexes.get("'~'");
            equality = cyk.symbolIndexes.get("'='");
            minus = cyk.symbolIndexes.get("'-'");
            lt = cyk.symbolIndexes.get("'<'");
            gt = cyk.symbolIndexes.get("'>'");
            amp = cyk.symbolIndexes.get("'&'");
            bar = cyk.symbolIndexes.get("'|'");
            excl = cyk.symbolIndexes.get("'!'");
            expr = cyk.symbolIndexes.get("expr");
            parExpr = cyk.symbolIndexes.get("parExpr");
            openParen = cyk.symbolIndexes.get("'('");
            bool = cyk.symbolIndexes.get("boolean");
            implication = cyk.symbolIndexes.get("implication");
            assertion = cyk.symbolIndexes.get("assertion");
            query = cyk.symbolIndexes.get("query");
            identifier = cyk.symbolIndexes.get("identifier");

            assignment = cyk.symbolIndexes.get("assignment");
            relation = cyk.symbolIndexes.get("relation");
            table = cyk.symbolIndexes.get("table");
            tuples = cyk.symbolIndexes.get("tuples");
            tuple = cyk.symbolIndexes.get("tuple");
            header = cyk.symbolIndexes.get("header");
            content = cyk.symbolIndexes.get("content");
            attribute = cyk.symbolIndexes.get("attribute");
            values = cyk.symbolIndexes.get("values");
            namedValue = cyk.symbolIndexes.get("namedValue");
        } catch( Exception e ) {
            e.printStackTrace();
        }
    }


    public static void main( String[] args ) throws Exception {
        String prg = Util.readFile(Database.class,path+programFile);

        List<LexerToken> src =  LexerToken.parse(prg);
        Matrix matrix = cyk.initArray1(src);
        int size = matrix.size();
        TreeMap<Integer,Integer> skipRanges = new TreeMap<Integer,Integer>();
        cyk.closure(matrix, 0, size+1, skipRanges, -1);
        ParseNode root = cyk.forest(size, matrix);

        if( root.topLevel != null ) {
            System.out.println("*** Parse Error in assertions file ***");
            CYK.printErrors(prg, src, root);
            return;
        }

        Database model = new Database();
        long t1 = System.currentTimeMillis();
        ParseNode exception = model.program(root,src);
        long t2 = System.currentTimeMillis();
        System.out.println("Time = "+(t2-t1)); // (authorized)
        if( exception != null ) {
            System.out.println("*** False Assertion ***");
            System.out.println(prg.substring(src.get(exception.from).begin, src.get(exception.to-1).end));
            return;
        }
    }
}
