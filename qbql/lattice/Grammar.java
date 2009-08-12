package qbql.lattice;

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

public class Grammar {

    //// WRITE RULES
    
    public static void main( String[] args ) throws Exception {
        Set<RuleTuple> rules = latticeRules();
        RuleTuple.memorizeRules(rules,location);
        RuleTuple.printRules(rules);
    }
    
    private static final String fname = "grammar.serializedBNF";
    private static final String path = "/qbql/lattice/";
    private static final String location = "c:/qbql_trunk"+path+fname;
    private static Set<RuleTuple> latticeRules() {
        Set<RuleTuple> ret = new TreeSet<RuleTuple>();
        // Pointfree LATTICE part
        ret.add(new RuleTuple("expr", new String[] {"identifier"}));
        ret.add(new RuleTuple("parExpr", new String[] {"'('","expr","')'"}));
        ret.add(new RuleTuple("join", new String[] {"expr","'^'","expr"}));
        ret.add(new RuleTuple("innerJoin", new String[] {"expr","'*'","expr"}));
        ret.add(new RuleTuple("innerUnion", new String[] {"expr","'v'","expr"}));
        ret.add(new RuleTuple("outerUnion", new String[] {"expr","'+'","expr"}));
        ret.add(new RuleTuple("unison", new String[] {"expr","'@'","expr"}));
        ret.add(new RuleTuple("setIX", new String[] {"expr","'\\'","'|'","'/'","expr"}));
        ret.add(new RuleTuple("setEQ", new String[] {"expr","'/'","'|'","'\\'","expr"}));
        ret.add(new RuleTuple("contain", new String[] {"expr","'/'","'|'","expr"}));
        ret.add(new RuleTuple("transpCont", new String[] {"expr","'|'","'\\'","expr"}));
        ret.add(new RuleTuple("disjoint", new String[] {"expr","'/'","'\\'","expr"}));
        ret.add(new RuleTuple("almostDisj", new String[] {"expr","'/'","'1'","'\\'","expr"}));
        ret.add(new RuleTuple("big", new String[] {"expr","'\\'","'/'","expr"}));
        ret.add(new RuleTuple("complement", new String[] {"identifier","'''"}));  
        ret.add(new RuleTuple("complement", new String[] {"parExpr","'''"}));
        ret.add(new RuleTuple("inverse", new String[] {"identifier","'`'"}));  
        ret.add(new RuleTuple("inverse", new String[] {"parExpr","'`'"}));
        ret.add(new RuleTuple("expr", new String[] {"join"}));
        ret.add(new RuleTuple("expr", new String[] {"innerJoin"}));
        ret.add(new RuleTuple("expr", new String[] {"innerUnion"}));
        ret.add(new RuleTuple("expr", new String[] {"outerUnion"}));
        ret.add(new RuleTuple("expr", new String[] {"setIX"}));
        ret.add(new RuleTuple("expr", new String[] {"setEQ"}));
        ret.add(new RuleTuple("expr", new String[] {"contain"}));
        ret.add(new RuleTuple("expr", new String[] {"transpCont"}));
        ret.add(new RuleTuple("expr", new String[] {"disjoint"}));
        ret.add(new RuleTuple("expr", new String[] {"almostDisj"}));
        ret.add(new RuleTuple("expr", new String[] {"big"}));
        ret.add(new RuleTuple("expr", new String[] {"unison"}));
        ret.add(new RuleTuple("expr", new String[] {"parExpr"}));
        ret.add(new RuleTuple("expr", new String[] {"complement"}));
        ret.add(new RuleTuple("expr", new String[] {"inverse"}));
        ret.add(new RuleTuple("boolean", new String[] {"expr","'='","expr"}));
        ret.add(new RuleTuple("boolean", new String[] {"expr","'!'","'='","expr"}));
        ret.add(new RuleTuple("boolean", new String[] {"expr","'~'","expr"}));
        ret.add(new RuleTuple("boolean", new String[] {"expr","'<'","expr"}));
        ret.add(new RuleTuple("boolean", new String[] {"expr","'>'","expr"}));
        ret.add(new RuleTuple("boolean", new String[] {"boolean","'&'","boolean"}));
        ret.add(new RuleTuple("boolean", new String[] {"boolean","'|'","boolean"}));
        ret.add(new RuleTuple("boolean", new String[] {"'-'","parBool"}));
        ret.add(new RuleTuple("boolean", new String[] {"parBool"}));
        ret.add(new RuleTuple("parBool", new String[] {"'('","boolean","')'"}));
        ret.add(new RuleTuple("implication", new String[] {"boolean","'-'","'>'","boolean"}));
        ret.add(new RuleTuple("implication", new String[] {"boolean","'<'","'-'","boolean"}));
        ret.add(new RuleTuple("implication", new String[] {"boolean","'<'","'-'","'>'","boolean"}));
        ret.add(new RuleTuple("assertion", new String[] {"boolean","'.'"}));
        ret.add(new RuleTuple("assertion", new String[] {"implication","'.'"}));
        ret.add(new RuleTuple("query", new String[] {"expr","';'"}));
        ret.add(new RuleTuple("program", new String[] {"assignment"}));
        ret.add(new RuleTuple("program", new String[] {"query"}));
        ret.add(new RuleTuple("program", new String[] {"assertion"}));
        ret.add(new RuleTuple("program", new String[] {"program","program"}));       
        // Partitions and Functional Dependencies
        ret.add(new RuleTuple("partition", new String[] {"expr","'#'","expr"}));
        ret.add(new RuleTuple("query", new String[] {"partition","';'"}));
        ret.add(new RuleTuple("boolean", new String[] {"partition","'<'","partition"}));
        ret.add(new RuleTuple("boolean", new String[] {"partition","'>'","partition"}));
        ret.add(new RuleTuple("boolean", new String[] {"partition","'='","partition"}));
        ret.add(new RuleTuple("partition", new String[] {"partition","'^'","partition"}));
        ret.add(new RuleTuple("partition", new String[] {"partition","'v'","partition"}));
        ret.add(new RuleTuple("partition", new String[] {"'('","partition","')'"}));
        // Pointwise
        ret.add(new RuleTuple("attribute", new String[] {"identifier"}));
        ret.add(new RuleTuple("value", new String[] {"digits"}));
        ret.add(new RuleTuple("value", new String[] {"identifier"}));
        ret.add(new RuleTuple("namedValue", new String[] {"attribute","'='","value"}));
        ret.add(new RuleTuple("values", new String[] {"namedValue"}));
        ret.add(new RuleTuple("values", new String[] {"values","','","namedValue"}));
        ret.add(new RuleTuple("tuple", new String[] {"'<'","values","'>'"}));
        ret.add(new RuleTuple("tuples", new String[] {"tuple"}));
        ret.add(new RuleTuple("tuples", new String[] {"tuples","','","tuple"}));
        ret.add(new RuleTuple("relation", new String[] {"'{'","tuples","'}'"}));
        ret.add(new RuleTuple("table", new String[] {"'['","header","']'","content"}));
        ret.add(new RuleTuple("table", new String[] {"'['","header","']'"}));
        ret.add(new RuleTuple("header", new String[] {"header","identifier"}));
        ret.add(new RuleTuple("header", new String[] {"identifier"}));
        ret.add(new RuleTuple("content", new String[] {"content","value"}));
        ret.add(new RuleTuple("content", new String[] {"value"}));
        ret.add(new RuleTuple("partition", new String[] {"content"}));
        ret.add(new RuleTuple("partition", new String[] {"partition","'|'","content"}));
        ret.add(new RuleTuple("expr", new String[] {"relation"}));
        ret.add(new RuleTuple("expr", new String[] {"table"}));
        ret.add(new RuleTuple("assignment", new String[] {"identifier","'='","expr","';'"})); // if defined in terms of lattice operations
        ret.add(new RuleTuple("assignment", new String[] {"identifier","'='","partition","';'"})); 
        ret.add(new RuleTuple("database", new String[] {"assignment"}));
        ret.add(new RuleTuple("database", new String[] {"database","assignment"}));
        return ret;
    }
    
    //// READ RULES
    
    static CYK cyk;
    static int naturalJoin;
    static int innerJoin;
    static int innerUnion;
    static int outerUnion;
    static int unison;
    static int setIX;
    static int setEQ;
    static int contain;
    static int transpCont;
    static int disjoint;
    static int almostDisj;
    static int big;
    static int complement;
    static int inverse;
    static int join;
    static int meet;
    static int equivalence;
    static int equality;
    static int num;
    static int minus;
    static int expr;
    static int partition;
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
        LexerToken.isPercentLineComment = true;
        
        try {
            cyk = new CYK(RuleTuple.getRules(path+bnf)) {
                public int[] atomicSymbols() {
                    return new int[] {assertion};
                }
            };
            naturalJoin = cyk.symbolIndexes.get("join");
            innerJoin = cyk.symbolIndexes.get("innerJoin");
            innerUnion = cyk.symbolIndexes.get("innerUnion");
            outerUnion = cyk.symbolIndexes.get("outerUnion");
            unison = cyk.symbolIndexes.get("unison");
            setIX = cyk.symbolIndexes.get("setIX");
            setEQ = cyk.symbolIndexes.get("setEQ");
            contain = cyk.symbolIndexes.get("contain");
            transpCont = cyk.symbolIndexes.get("transpCont");
            disjoint = cyk.symbolIndexes.get("disjoint");
            almostDisj = cyk.symbolIndexes.get("almostDisj");
            big = cyk.symbolIndexes.get("big");
            complement = cyk.symbolIndexes.get("complement");
            inverse = cyk.symbolIndexes.get("inverse");
            join = cyk.symbolIndexes.get("'v'");
            meet = cyk.symbolIndexes.get("'^'");
            equivalence = cyk.symbolIndexes.get("'~'");
            equality = cyk.symbolIndexes.get("'='");
            minus = cyk.symbolIndexes.get("'-'");
            lt = cyk.symbolIndexes.get("'<'");
            gt = cyk.symbolIndexes.get("'>'");
            amp = cyk.symbolIndexes.get("'&'");
            num = cyk.symbolIndexes.get("'#'");
            bar = cyk.symbolIndexes.get("'|'");
            excl = cyk.symbolIndexes.get("'!'");
            expr = cyk.symbolIndexes.get("expr");
            partition = cyk.symbolIndexes.get("partition");
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
            //System.out.println(cyk.allSymbols[20]);
        } catch( Exception e ) {
            e.printStackTrace();
        }
    }
    
    //--------------------------------------------------------------------------
    
    public List<LexerToken> src;
    public Database database = new Database();
    public Grammar( List<LexerToken> program ) throws Exception {
        String dbSource = Util.readFile(getClass(),path+Database.databaseFile); 

        this.src =  LexerToken.parse(dbSource);
        Matrix matrix = Grammar.cyk.initArray1(src);
        int size = matrix.size();
        TreeMap<Integer,Integer> skipRanges = new TreeMap<Integer,Integer>();
        Grammar.cyk.closure(matrix, 0, size+1, skipRanges, -1);
        ParseNode root = Grammar.cyk.forest(size, matrix);

        if( root.topLevel != null ) {
            System.out.println("*** Parse Error in database file ***");
            CYK.printErrors(dbSource, src, root);
            throw new Exception("Parse Error");
        }

        database(root);

        database.buildR10();
        database.buildR11();

        // relations that requre complement can be built only after R10 and R11 are defined
        try {
            database.addRelation("UJADJBC'",database.complement(database.relation("UJADJBC")));
        } catch( Exception e ) { // NPE if databaseFile is not Figure1.db
        }

        this.src = program;
    }

    public boolean bool( ParseNode root ) throws Exception {
        for( ParseNode child : root.children() ) 
            if( child.contains(expr) || child.contains(partition) )
                return atomicProposition(root);
            else 
                return logical(root);
        throw new Exception("Impossible exception, no children??"+root.content(src));
    }

    public boolean logical( ParseNode root ) throws Exception {
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
                    left = bool(child);
            } else if( child.contains(amp) ) 
                oper = amp;
            else if( child.contains(bar) ) 
                oper = bar;
            else {                               
                right = bool(child);
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

    public boolean atomicProposition( ParseNode root ) throws Exception {
        for( ParseNode child : root.children() ) {
            if( child.contains(partition) )
                return partitionProposition(root);
            else if( child.contains(expr) )
                return relationalProposition(root);
            break;
        }
        throw new Exception("VT: neither relational not partitional proposition");
    }
    private boolean partitionProposition( ParseNode root ) throws Exception {
        int oper = -1;
        Partition left = null;
        Partition right = null;
        for( ParseNode child : root.children() ) {
            if( left == null )
                left = partition(child);
            else if( child.contains(lt) )
                oper = lt;
            else if( child.contains(gt) )
                oper = gt;
            else if( child.contains(equality) )
                oper = equality;
            else
                right = partition(child);
        }
        if( oper == lt )
            return Partition.le(left,right);
        if( oper == gt )
            return Partition.ge(left,right);
        if( oper == equality )
            return left.equals(right);

        throw new Exception("Impossible case");             
    }
    public boolean relationalProposition( ParseNode root ) throws Exception {
        int oper = -1;
        Relation left = null;
        Relation right = null;
        boolean not = false;
        for( ParseNode child : root.children() ) {
            if( left == null )
                left = expr(child);
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
                right = expr(child);
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

    public ParseNode implication( ParseNode root ) throws Exception {
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
        if( impl && !bimpl && impl(left,right) ) 
            return null;
        else if( !impl && bimpl && impl(right,left) ) 
            return null;
        else if( impl && bimpl && impl(left,right) && impl(right,left) ) 
            return null;
        else
            return root;
    }
    private boolean impl( ParseNode left,ParseNode right ) throws Exception {
        boolean l = bool(left);
        if( !l ) // optimization: early termination
            return true;
        return bool(right);
    }

    public ParseNode assertion( ParseNode root, boolean outputVariables ) throws Exception {
        ParseNode ret = null;
        Set<String> variables = new HashSet<String>();
        String[] tables = database.relNames();

        for( ParseNode descendant : root.descendants() ) {
            String id = descendant.content(src);
            if( descendant.from+1 == descendant.to 
                    && (descendant.contains(expr) || descendant.contains(identifier))
                    && !root.parent(descendant.from, descendant.to).contains(header)
                    && database.relation(id) == null ) 
                variables.add(id);
        }

        int[] indexes = new int[variables.size()];
        for( int i = 0; i < indexes.length; i++ )
            indexes[i] = 0;
        do {
            int var = 0;
            for( String variable : variables ) {
                database.addRelation(variable, database.relation(tables[indexes[var++]]));
            }


            for( ParseNode child : root.children() ) {
                if( child.contains(bool) ) {
                    if( !bool(child) ) {
                        for( String variable : variables )
                            if( outputVariables )
                                System.out.println(variable+" = "
                                                   +database.relation(variable).toString(variable.length()+3, false)
                                                   +";");
                        ret = child;
                        for( String variable : variables )
                            database.removeRelation(variable);
                        return ret;
                    }
                } else if( child.contains(implication) ) {
                    ret = implication(child);
                    if( ret != null ) {
                        for( String variable : variables )
                            if( outputVariables )
                                System.out.println(variable+" = "
                                                   +database.relation(variable).toString(variable.length()+3, false)
                                                   +";");
                        for( String variable : variables )
                            database.removeRelation(variable);
                        return ret;
                    }
                } 
            }
        } while( Util.next(indexes,tables.length) );

        for( String variable : variables )
            database.removeRelation(variable);
        return ret;
    }

    public ParseNode query( ParseNode root ) throws Exception {
        for( ParseNode child : root.children() ) {
            if( child.contains(partition) ) {
                System.out.println(child.content(src)+"="+partition(child).toString()+";");
                return null;
            } else if( child.contains(expr) ) {
                System.out.println(child.content(src)+"="+expr(child).toString(child.content(src).length()+1, false)+";");
                return null;
            } 
        }
        throw new Exception("No expr/partition in statement?");
    }
    /**
     * @param root
     * @param src
     * @return node that violates assertion
     * @throws Exception
     */
    public ParseNode program( ParseNode root ) throws Exception {
        if( root.contains(assertion) )
            return assertion(root,true);
        if( root.contains(query) )
            return query(root);
        if( root.contains(assignment) ) {
            createRelation(root);
            return null;
        }
        ParseNode ret = null;
        for( ParseNode child : root.children() ) {
            ret = program(child);       
            if( ret != null )
                return ret;
        }
        return ret;
    }


    public void database( ParseNode root ) throws Exception {
        if( root.contains(assignment) )
            createRelation(root);
        else
            for( ParseNode child : root.children() ) {                              
                if( child.contains(assignment) )
                    createRelation(child);
                else 
                    database(child);
            }
    }
    public void createRelation( ParseNode root ) throws Exception {
        String left = null;
        Relation right = null;
        for( ParseNode child : root.children() ) {
            if( left == null )
                left = child.content(src);
            else if( child.contains(equality) )
                ;
            else {                          
                right = expr(child);
                break;
            }
        }
        database.addRelation(left, right);
    }
    public Relation expr( ParseNode root ) throws Exception {
        if( root.contains(relation) ) {
            for( ParseNode child : root.children() ) {
                if( child.contains(tuples) )
                    return tuples(child);
            }
        } else if( root.contains(table) ) {
            Relation ret = null;
            for( ParseNode child : root.children() ) {
                if( child.contains(header) )                    
                    ret = new Relation(strings(child).toArray(new String[0]));
                else if( child.contains(content) ) {
                    addContent(ret,child);
                    return ret;
                }                 
            } 
            return ret;
        } else if( root.contains(naturalJoin) ) 
            return binaryOper(root,naturalJoin);
        else if( root.contains(innerJoin) ) 
            return binaryOper(root,innerJoin);
        else if( root.contains(outerUnion) ) 
            return binaryOper(root,outerUnion);
        else if( root.contains(innerUnion) ) 
            return binaryOper(root,innerUnion);
        else if( root.contains(unison) ) 
            return binaryOper(root,unison);
        else if( root.contains(complement) ) 
            return unaryOper(root,complement);
        else if( root.contains(inverse) ) 
            return unaryOper(root,inverse);
        else if( root.contains(setIX) ) 
            return binaryOper(root,setIX);
        else if( root.contains(setEQ) ) 
            return binaryOper(root,setEQ);
        else if( root.contains(contain) ) 
            return binaryOper(root,contain);
        else if( root.contains(transpCont) ) 
            return binaryOper(root,transpCont);
        else if( root.contains(disjoint) ) 
            return binaryOper(root,disjoint);
        else if( root.contains(almostDisj) ) 
            return binaryOper(root,almostDisj);
        else if( root.contains(big) ) 
            return binaryOper(root,big);
        
        else if( root.contains(parExpr) ) 
            return parExpr(root);
        
        else if( root.contains(identifier) || root.from+1 == root.to ) 
            return database.relation(src.get(root.from).content);
                    
        throw new Exception("Unknown case");
    }
    
    public Relation binaryOper( ParseNode root, int oper ) throws Exception  {
        Relation left = null;
        Relation right = null;
        for( ParseNode child : root.children() ) {
            if( left == null )
                left = expr(child);
            else if( child.contains(num) )
                ;
            else                            
                right = expr(child);
        }
        if( oper == naturalJoin )
            return Relation.join(left,right);
        else if( oper == innerJoin )
            return Relation.innerJoin(left,right);
        else if( oper == innerUnion )
            return Relation.innerUnion(left,right);
        else if( oper == outerUnion )
            return database.outerUnion(left,right);
        else if( oper == setEQ ) 
            return database.quantifier(left,right,setEQ);
        else if( oper == setIX ) 
            return database.quantifier(left,right,setIX);
        else if( oper == contain ) 
            return database.quantifier(left,right,contain);
        else if( oper == transpCont ) 
            return database.quantifier(left,right,transpCont);
        else if( oper == disjoint ) 
            return database.quantifier(left,right,disjoint);
        else if( oper == almostDisj ) 
            return database.quantifier(left,right,almostDisj);
        else if( oper == big ) 
            return database.quantifier(left,right,big);
        else if( oper == unison ) 
            return Relation.unison(left,right);
        throw new Exception("Unknown case");
    }
    public Relation unaryOper( ParseNode root, int oper ) throws Exception  {
        for( ParseNode child : root.children() ) {
            Relation rel = expr(child);
            if( oper == complement )
                return database.complement(rel);
            else if( oper == inverse )
                return database.inverse(rel);
        }
        throw new Exception("Unknown case");
    }
        
    private Relation parExpr( ParseNode root ) throws Exception {
        boolean parenthesis = false;
        for( ParseNode child : root.children() ) {
            if( child.contains(openParen) ) {
                parenthesis = true;
                continue;
            }
            if( parenthesis ) 
                return expr(child);           
        }
        throw new Exception("No parenthesis found");
    }

        
   /*     
        else {
            if( root.from + 1 == root.to ) {
                Relation ret = database.relation(src.get(root.from).content);
                if( ret == null )
                    throw new Exception("There is no relation "+src.get(root.from).content+" in the database");
                return ret;
            }
            
            Relation x = null;
            Relation y = null;
            boolean parenGroup = false;
            for( ParseNode child : root.children() ) {
                if( parenGroup )
                    return expr(child);
                else if( child.contains(openParen) )
                    parenGroup = true;
                else if( child.contains(relation) 
                        || child.contains(expr) 
                        || child.contains(identifier) 
                        || child.contains(parExpr) 
                        || child.contains(attribute) // produced by ExprGen 
                ) {
                    if( x == null )
                        x = expr(child);
                    else
                        y = expr(child);
                } 
            }
            return null;
        }
        throw new Exception("Unknown case");
    }
    */
    
    public Partition partition( ParseNode root ) throws Exception {
        boolean parenthesis = false;
        for( ParseNode child : root.children() ) {
            if( child.contains(openParen) ) {
                parenthesis = true;
                continue;
            }
            if( parenthesis ) 
                return partition(child);
            if( child.contains(partition) )
                return partPart(root);
            else if( child.contains(expr) )
                return relPart(root);
            break;
        }
        throw new Exception("Unknown case");
    }
    private Partition partPart( ParseNode root ) throws Exception {
        Partition left = null;
        Partition right = null;
        int oper = -1;
        for( ParseNode child : root.children() ) {
            if( left == null )
                left = partition(child);
            else if( child.contains(join) )
                oper = join;
            else if( child.contains(meet) )
                oper = meet;
            else                            
                right = partition(child);
        }
        if( oper == join )
            return Partition.union(left,right);
        if( oper == meet )
            return Partition.intersect(left,right);

        throw new Exception("Impossible case");             
    }
    private Partition relPart( ParseNode root ) throws Exception {
        Relation left = null;
        Relation right = null;
        for( ParseNode child : root.children() ) {
            if( left == null )
                left = expr(child);
            else if( child.contains(num) )
                ;
            else                            
                right = expr(child);
        }
        return new Partition(left,right,database);
    }

    
    public Relation tuples( ParseNode root ) throws Exception {
        Set<String> attrs = new TreeSet<String>();
        for( ParseNode descendant: root.descendants() )
            if( descendant.contains(attribute) )
                attrs.add(descendant.content(src));
        Relation ret = new Relation(attrs.toArray(new String[0]));

        addTuples(ret,root);            
        return ret;
    }
    private void addTuples( Relation ret, ParseNode root ) throws Exception {
        if( root.contains(tuple) ) 
            ret.addTuple(tuple(root));
        else for( ParseNode child : root.children() )
            if( child.contains(tuple) )
                ret.addTuple(tuple(child));
            else if( child.contains(tuples) )
                addTuples(ret,child);
    }
    private Map<String,String> tuple( ParseNode root ) throws Exception {
        for( ParseNode child : root.children() ) {
            if( child.contains(values) ) {
                Map<String,String> tuple = new TreeMap<String,String>(); 
                values(tuple, child);
                return tuple;
            }
        }
        throw new Exception("Unknown case");
    }
    private void values( Map<String,String> tuple, ParseNode root ) {
        if( root.contains(namedValue) )
            value(tuple,root);
        else for( ParseNode child : root.children() )
            if( child.contains(namedValue) )
                value(tuple,child);
            else if( child.contains(values) )
                values(tuple,child);
    }
    public void value( Map<String,String> tuple, ParseNode root ) {
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

    List<String> strings( ParseNode root ) throws Exception {
        List<String> ret = new LinkedList<String>();
        if( root.from + 1 == root.to && 
            (src.get(root.from).type == Token.IDENTIFIER || src.get(root.from).type == Token.DIGITS )
        )
            ret.add(root.content(src));
        else
            for( ParseNode child : root.children() )
                ret.addAll(strings(child));
        return ret;
    }   
    void addContent( Relation ret, ParseNode root ) throws Exception {
        int i = 0;
        String[] t = new String[ret.colNames.length];
        for( String elem : strings(root) ) {
            t[i%ret.colNames.length] = elem;
            if( i%ret.colNames.length == ret.colNames.length-1 ) {
                ret.content.add(new Tuple(t));
                t = new String[ret.colNames.length];
            }
            i++;
        }
    }
}
