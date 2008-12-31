package qbql.lattice;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import qbql.parser.CYK;
import qbql.parser.LexerToken;
import qbql.parser.Matrix;
import qbql.parser.ParseNode;
import qbql.parser.RuleTuple;
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
     * Complement x' returns relation with the same header as x
     * with tuples which are not in x
     * Axioms:
        x' ^ x = x ^ R00.
        x' v x = x v R11.
     */
    Relation complement( Relation x ) {
        Relation xvR11 = Relation.innerUnion(x, R11);
        Relation ret = Relation.join(x, R00);
        for( Tuple t : xvR11.content ) {
            boolean matched = false;
            for( Tuple tx : x.content )
                if( t.equals(tx, ret, x) ) {
                    matched = true;
                    break;
                }
            if( !matched )
                ret.content.add(t);
        }
        return ret;
    }

    /**
     * Semi inverse x~ returns empty relation with header complementary to that of x
     * Axioms:
        x~ ^ x = R00 ^ R11.
        x~ v x = x v R00.
       (Genuine inverse would honor
        x~ ^ x = x ^ R11.
        x~ v x = x v R00. 
        which is unsatisfiable by finite models!)
     
    Relation semiInverse( Relation x ) {
        String[] header = new String[R10.colNames.length-x.colNames.length];
        int i = -1;
        for( String attr : R10.colNames ) {
            if( x.header.get(attr) == null ) {
                i++;
                header[i] = attr;
            }
        }
        return new Relation(header);
    }*/
    
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

        Relation ret = R01;
        for( Relation domain : domains.values() )
            ret = Relation.join(ret, domain);
        return ret;	
    }

    public Relation eval( String input ) throws Exception {
        List<LexerToken> src =  LexerToken.parse(input);
        Matrix matrix = cyk.initArray1(src);
        int size = matrix.size();
        TreeMap<Integer,Integer> skipRanges = null;//new TreeMap<Integer,Integer>();
        cyk.closure(matrix, 0, size+1, skipRanges, -1);
        ParseNode root = cyk.forest(size, matrix);
        return eval(root,src);
    }
    private Relation eval( ParseNode node, List<LexerToken> src ) throws Exception {
        if( node.from + 1 == node.to ) {
            Relation ret = lattice.get(src.get(node.from).content);
            if( ret == null )
                throw new Exception("There is no relation "+src.get(node.from).content+" in the database");
            return ret;
        }

        Relation x = null;
        Relation y = null;
        int oper = -1;
        boolean parenGroup = false;
        for( ParseNode child : node.children() ) {
            if( parenGroup )
                return eval(child, src);
            else if( child.contains(openParen) )
                parenGroup = true;
            else if( child.contains(relation) || child.contains(expr) ) {
                if( x == null )
                    x = compute(child,src);
                else
                    y = compute(child,src);
            } else
                oper = child.content().toArray(new Integer[0])[0];
        }
        if( oper == join ) 
            return Relation.join(x,y);
        if( oper == innerJoin ) 
            return Relation.innerJoin(x,y);
        if( oper == outerUnion ) 
            return outerUnion(x,y);
        if( oper == innerUnion ) 
            return Relation.innerUnion(x,y);
        if( oper == unison ) 
            return Relation.unison(x,y);
        if( oper == complement ) 
            return complement(x);
        return null;
    }

    public boolean bool( ParseNode root, List<LexerToken> src ) throws Exception {
        boolean isParen = false;
        for( ParseNode c : root.children() ) 
            if( c.contains(openParen) ) {
                isParen = true;
                continue;
            } else if( isParen ) {
                return bool(c,src);
            } else if( c.contains(bool) ) {
                Boolean left = null;
                Boolean right = null;
                for( ParseNode child : root.children() ) {
                    if( left == null )
                        left = bool(child,src);
                    else if( child.contains(amp) ) {
                    } else 				
                        right = bool(child,src);
                }
                return left & right;
            } else {
                int oper = -1;
                Relation left = null;
                Relation right = null;
                for( ParseNode child : root.children() ) {
                    if( left == null )
                        left = compute(child,src);
                    else if( child.contains(equality) )
                        oper = equality;
                    else if( child.contains(lt) )
                        oper = lt;
                    else if( child.contains(equivalence) )
                        oper = equivalence;
                    else 				
                        right = compute(child,src);
                }
                if( oper == equality && !left.equals(right)
                 || oper == lt && !Relation.le(left,right)
                 || oper == equivalence && !Relation.equivalent(left,right)
                ) 
                    return false;

                return true;				
            }
        throw new Exception("Impossible case");		
    }
    public ParseNode implication( ParseNode root, List<LexerToken> src ) throws Exception {
        Boolean left = null;
        Boolean right = null;
        for( ParseNode child : root.children() ) {
            if( left == null )
                left = bool(child,src);
            else if( child.contains(gt)||child.contains(minus) )
                ;
            else 				
                right = bool(child,src);
        }		
        if( !left || right )
            return null;
        else
            return root;
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
             && descendant.contains(expr) 
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
                                               +lattice.get(variable).toString(variable.length()+3)
                                               +";");
                        return child;
                    }
                } else if( child.contains(implication) ) {
                    ParseNode ret = implication(child,src);
                    if( ret != null ) {
                        for( String variable : variables )
                            System.out.println(variable+" = "
                                               +lattice.get(variable).toString(variable.length()+3)
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
                System.out.println(child.content(src)+"="+compute(child,src).toString(child.content(src).length()+1));
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
        } else
            return eval(root,src);
        throw new Exception("Unknown case");
    }
    public Relation tuples( ParseNode root, List<LexerToken> src ) throws Exception {
        Set<String> attrs = new HashSet<String>();
        for( ParseNode descendant: root.descendants() )
            if( descendant.contains(attribute) )
                attrs.add(descendant.content(src));
        Relation ret = new Relation(attrs.toArray(new String[0]));

        addTuples(ret,root,src);		
        return ret;
    }
    public void addTuples( Relation ret, ParseNode root, List<LexerToken> src ) throws Exception {
        if( root.contains(tuple) ) 
            ret.addTuple(tuple(root,src));
        else for( ParseNode child : root.children() )
            if( child.contains(tuple) )
                ret.addTuple(tuple(child,src));
            else if( child.contains(tuples) )
                addTuples(ret,child,src);
    }
    public Map<String,String> tuple( ParseNode root, List<LexerToken> src ) throws Exception {
        for( ParseNode child : root.children() ) {
            if( child.contains(values) ) {
                Map<String,String> tuple = new TreeMap<String,String>(); 
                values(tuple, child,src);
                return tuple;
            }
        }
        throw new Exception("Unknown case");
    }
    public void values( Map<String,String> tuple, ParseNode root, List<LexerToken> src ) {
        if( root.contains(value) )
            value(tuple,root,src);
        else for( ParseNode child : root.children() )
            if( child.contains(value) )
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

    static CYK cyk;
    static int join;
    static int innerJoin;
    static int innerUnion;
    static int outerUnion;
    static int unison;
    static int complement;
    static int equivalence;
    static int equality;
    static int minus;
    static int expr;
    static int openParen;
    static int bool;
    static int implication;
    static int lt;
    static int gt;
    static int amp;
    static int assertion;
    static int query;
    static int identifier;

    static int assignment;
    static int relation;
    static int tuples;
    static int tuple;
    static int attribute;
    static int values;
    static int value;
    static int comma;
    private static final String bnf = "grammar.serializedBNF";
    static {
        try {
            cyk = new CYK(RuleTuple.getRules(path+bnf)) {
                public int[] atomicSymbols() {
                    return new int[] {assertion};
                }
            };
            join = cyk.symbolIndexes.get("'^'");
            innerJoin = cyk.symbolIndexes.get("'*'");
            innerUnion = cyk.symbolIndexes.get("'v'");
            outerUnion = cyk.symbolIndexes.get("'+'");
            unison = cyk.symbolIndexes.get("'@'");
            complement = cyk.symbolIndexes.get("'''");
            equivalence = cyk.symbolIndexes.get("'~'");
            equality = cyk.symbolIndexes.get("'='");
            minus = cyk.symbolIndexes.get("'-'");
            lt = cyk.symbolIndexes.get("'<'");
            gt = cyk.symbolIndexes.get("'>'");
            amp = cyk.symbolIndexes.get("'&'");
            expr = cyk.symbolIndexes.get("expr");
            openParen = cyk.symbolIndexes.get("'('");
            bool = cyk.symbolIndexes.get("boolean");
            implication = cyk.symbolIndexes.get("implication");
            assertion = cyk.symbolIndexes.get("assertion");
            query = cyk.symbolIndexes.get("query");
            identifier = cyk.symbolIndexes.get("identifier");

            assignment = cyk.symbolIndexes.get("assignment");
            relation = cyk.symbolIndexes.get("relation");
            tuples = cyk.symbolIndexes.get("tuples");
            tuple = cyk.symbolIndexes.get("tuple");
            attribute = cyk.symbolIndexes.get("attribute");
            values = cyk.symbolIndexes.get("values");
            value = cyk.symbolIndexes.get("value");
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
        ParseNode exception = model.program(root,src);
        if( exception != null ) {
            System.out.println("*** False Assertion ***");
            System.out.println(prg.substring(src.get(exception.from).begin, src.get(exception.to-1).end));
            return;
        }
    }
}
