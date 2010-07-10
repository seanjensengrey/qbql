package qbql.lattice;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import qbql.index.IndexedPredicate;
import qbql.parser.BNFGrammar;
import qbql.parser.CYK;
import qbql.parser.Lex;
import qbql.parser.LexerToken;
import qbql.parser.ParseNode;
import qbql.parser.RuleTuple;
import qbql.parser.Token;
import qbql.util.Util;

public class Program {

    public static void main( String[] args ) throws Exception {
        Set<RuleTuple> rules = latticeRules();
        RuleTuple.printRules(rules);
    }
    
    //// READ RULES
    
    public static CYK cyk;
    public static int naturalJoin;
    public static int innerUnion;
    static int userDefined;
    static int unnamedJoin;
    static int unnamedMeet;
    static int setIX;
    static int setEQ;
    static int contains;
    static int transpCont;
    static int disjoint;
    static int almostDisj;
    static int big;
    public static int complement;
    public static int inverse;
    static int join;
    static int meet;
    static int equivalence;
    static int equality;
    static int num;
    static int minus;
    public static int expr;
    static int partition;
    static int parExpr;
    public static int openParen;
    static int implication;
    static int proposition;
    static int lt;
    static int gt;
    static int amp;
    static int bar;
    static int excl;
    static int assertion;
    static int query;
    public static int identifier;

    static int assignment;
    static int relation;
    static int table;
    static int tuples;
    static int tuple;
    static int header;
    static int content;
    public static int attribute;
    static int values;
    static int namedValue;
    static int comma;
    static {        
        try {
            cyk = new CYK(latticeRules()) {
                public int[] atomicSymbols() {
                    return new int[] {assertion,assignment,query};
                }
            };
            naturalJoin = cyk.symbolIndexes.get("join");
            userDefined = cyk.symbolIndexes.get("userDefined");
            innerUnion = cyk.symbolIndexes.get("innerUnion");
            unnamedJoin = cyk.symbolIndexes.get("unnamedJoin");
            unnamedMeet = cyk.symbolIndexes.get("unnamedMeet");
            setIX = cyk.symbolIndexes.get("setIX");
            setEQ = cyk.symbolIndexes.get("setEQ");
            contains = cyk.symbolIndexes.get("contains");
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
            implication = cyk.symbolIndexes.get("implication");
            proposition = cyk.symbolIndexes.get("proposition");
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
            e.printStackTrace(); // (authorized)
        }
    }
    
    private static Set<RuleTuple> latticeRules() throws Exception {
        String input = Util.readFile(Program.class, "lattice.grammar");
        HashMap<String, String> specialSymbols = new HashMap<String, String>();
        specialSymbols.put("qtSymbol", "'");
        List<LexerToken> src = new Lex(
                      true, true, false,
                      specialSymbols                 
        ).parse(input);
        //LexerToken.print(src);
        ParseNode root = BNFGrammar.parseGrammarFile(src, input);
        return BNFGrammar.grammar(root, src);
    }
    
    //--------------------------------------------------------------------------
    
    public List<LexerToken> src;
    public Database database;
    public Program( List<LexerToken> program, Database db ) {
        this.src = program;
        this.database = db;
    }

    private boolean bool( ParseNode root ) throws Exception {
        for( ParseNode child : root.children() ) 
            if( child.contains(expr) || child.contains(partition) )
                return atomicProposition(root);
            else 
                return logical(root);
        throw new Exception("Impossible exception, no children??"+root.content(src));
    }

    private boolean logical( ParseNode root ) throws Exception {
        Boolean left = null;
        int oper = -1;
        boolean impl = false;
        boolean bimpl = false;
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
            else if( child.contains(bar) ) {
                oper = bar;
            } else if( child.contains(gt)||child.contains(minus)||child.contains(lt) ) {
                if( child.contains(gt) )
                    impl = true;
                if( child.contains(lt) )
                    bimpl = true;
            } else {                               
                if( oper == amp ) {
                    // return left & bool(child) -- experienced function eval even when unnecessary
                    // therefore, optimized explicitly
                    if( !left )
                        return false;
                    else
                        return bool(child);
                } else if( oper == bar ) {
                    //return left | bool(child);
                    if( left )
                        return true;
                    else
                        return bool(child);
                } else if( oper == minus && !child.contains(openParen) )
                    return ! bool(child);
                else if( oper == openParen )
                    return bool(child);
                else if( impl && !bimpl ) { 
                    //return !left | bool(child);
                    if( !left )
                        return true;
                    else {
                    	boolean ret = bool(child);
                    	/*if( ret ) {
                            for( String variable : variables(root, false) )
                            	System.out.println(variable+" = "
                            			+database.predicate(variable).toString(variable.length()+3, false)
                            			+";");

                    	}*/
                        return ret;
                    }
                } else if( !impl && bimpl ) 
                    return left | !bool(child);
                else if( impl && bimpl ) 
                    return left == bool(child);
            }
        }
        throw new Exception("Unknown boolean operation "+oper);
    }

    private boolean atomicProposition( ParseNode root ) throws Exception {
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
    private boolean relationalProposition( ParseNode root ) throws Exception {
        int oper = -1;
        Predicate left = null;
        Predicate right = null;
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
            else if( oper == lt && child.contains(equivalence) )
                oper = -lt;
            else if( oper == gt && child.contains(equivalence) )
                oper = -gt;
            else if( child.contains(equivalence) )
                oper = equivalence;
            else                            
                right = expr(child);
        }
        if( oper == lt )
            return Relation.le((Relation)left,(Relation)right);
        else if( oper == gt )
            return Relation.ge((Relation)left,(Relation)right);
        else if( oper == -lt )
            return database.submissive((Relation)left,(Relation)right);
        else if( oper == -gt )
            return database.submissive((Relation)right,(Relation)left);
        else if( oper == equivalence )
            return database.equivalent((Relation)left,(Relation)right);
        else if( oper == equality )
            return not ? !left.equals(right) : left.equals(right);

            throw new Exception("Impossible case");             
    }

    public ParseNode assertion( ParseNode root, boolean outputVariables ) throws Exception {
        for( ParseNode child : root.children() ) {
        	if( isDeclaration(child) )
        		return null;
        	break;
        }
        
        ParseNode ret = null;
        Set<String> variables = variables(root);
        
        String[] tables = database.relNames();
        int[] indexes = new int[variables.size()];
        for( int i = 0; i < indexes.length; i++ )
            indexes[i] = 0;
        do {
            int var = 0;
            for( String variable : variables ) {
//System.out.println(variable+"="+tables[indexes[var]]);
                database.addPredicate(variable, database.getPredicate(tables[indexes[var++]]));
            }

            for( ParseNode child : root.children() ) {
                if( child.contains(implication) ) {
                    if( !bool(child) ) {
                        for( String variable : variables )
                            if( outputVariables )
                                System.out.println(variable+" = "
                                                   +database.getPredicate(variable).toString(variable.length()+3, false)
                                                   +";");
                        ret = child;
                        for( String variable : variables )
                            database.removePredicate(variable);
                        return ret;
                    } 
                    break;
                } else
                	throw new Exception("Non boolean assertion???"); 
            }
        } while( Util.next(indexes,tables.length) );

        for( String variable : variables )
            database.removePredicate(variable);
        return ret;
    }

	private boolean isDeclaration( ParseNode root ) throws AssertionError {
        if( root.contains(proposition) ) {
        	String lft = null;
        	String operation = null;
        	String rgt = null;
        	boolean sawEQ = false;
            for( ParseNode child : root.children() ) {
            	if( !child.contains(userDefined) && operation == null )
            		break;
            	if( operation == null ) {
            		for( ParseNode grandChild : child.children() ) {
            			if( lft == null ) 
            				lft = grandChild.content(src);
            			else if( operation == null ) {
            				operation = grandChild.content(src);
            				try { 
            					database.getOperation(operation);
            					return false;
            				} catch( AssertionError e ) {} 
            			} else if( rgt == null ) 
            				rgt = grandChild.content(src);
            			else
            				throw new AssertionError("Unexpected user defined expression");
            		}
            	} else if( !sawEQ ) {
            		if( !child.contains(equality) ) 
            			throw new AssertionError("Unexpected user defined expression");
            		else 
            			sawEQ = true;
            	} else {
            		database.addOperation(operation, Expr.convert(lft, rgt, child, src));
            		return true;
            	}
            }
        }
        return false;
	}


	private Set<String> variables( ParseNode root, boolean notAssignedOnes ) throws Exception {
        Set<String> variables = new HashSet<String>();
        for( ParseNode descendant : root.descendants() ) {
            String id = descendant.content(src);
            if( descendant.from+1 == descendant.to 
                    && (descendant.contains(expr) || descendant.contains(identifier))
                    && !root.parent(descendant.from, descendant.to).contains(header)
                    && !root.parent(descendant.from, descendant.to).contains(table)
                    && (!notAssignedOnes || lookup(id) == null) ) 
                variables.add(id);
        }
        return variables;
    }
    public Set<String> variables( ParseNode root ) throws Exception {
    	return variables(root, true);
    }

    private ParseNode query( ParseNode root ) throws Exception {
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
            createPredicate(root);
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


    private void createPredicate( ParseNode root ) throws Exception {
        String left = null;
        Predicate right = null;
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
        database.addPredicate(left, right);
    }
    private Predicate expr( ParseNode root ) throws Exception {
        if( root.contains(relation) ) {
            for( ParseNode child : root.children() ) {
                if( child.contains(tuples) )
                    return tuples(child);
            }
        } else if( root.contains(table) ) {
            Relation ret = Database.R00;
            String colX = null;
            for( ParseNode child : root.children() ) {
                if( child.contains(header) )                    
                    ret = new Relation(values(child).toArray(new String[0]));
                else if( child.contains(content) ) {
                    addContent(ret,child);
                    return ret;
                } else if( child.contains(identifier) && colX == null ) 
                    colX = child.content(src);
                else if( child.contains(identifier) ) {
                    return new EqualityPredicate(colX, child.content(src));
                }
            } 
            return ret;
        } /*else if( root.contains(renamedRel) ) {
            String relName = null;
            List<String> columns = null;
            for( ParseNode child : root.children() ) {
                if( child.contains(header) )
                    columns = strings(child);
                else if( child.contains(identifier) ) 
                    relName = child.content(src);
            }
            Relation ret = Relation.join(lookup(relName),Database.R01); // clone
            if( columns.size() != ret.colNames.length )
                throw new RuntimeException();
            for( int i = 0; i < ret.colNames.length; i++ ) {
                ret.renameInPlace(ret.colNames[i], columns.get(i));
            }
            return ret;
        }*/ else if( root.contains(naturalJoin) ) 
            return binaryOper(root,naturalJoin);
        else if( root.contains(userDefined) ) 
            return userDefined(root);
        else if( root.contains(innerUnion) ) 
            return binaryOper(root,innerUnion);
        else if( root.contains(unnamedJoin) ) 
            return binaryOper(root,unnamedJoin);
        else if( root.contains(unnamedMeet) ) 
            return binaryOper(root,unnamedMeet);
        else if( root.contains(complement) ) 
            return unaryOper(root,complement);
        else if( root.contains(inverse) ) 
            return unaryOper(root,inverse);
        else if( root.contains(setIX) ) 
            return binaryOper(root,setIX);
        else if( root.contains(setEQ) ) 
            return binaryOper(root,setEQ);
        else if( root.contains(contains) ) 
            return binaryOper(root,contains);
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
        
        else if( root.contains(identifier) || root.from+1 == root.to ) {
            String name = src.get(root.from).content;
            Predicate candidate = lookup(name);
            if( candidate == null )
                throw new Exception("Predicate/Table '"+name+"' not in the database");
			return candidate;
        }
                    
        throw new Exception("Unknown case");
    }

    private Predicate lookup( String name ) throws Exception {
        Predicate ret = database.getPredicate(name);
        if( ret != null ) 
            return ret;
        try {
            return new IndexedPredicate(database,name);
        } catch ( Exception e ) {
            return null;
        }
    }
    
    private Predicate userDefined( ParseNode root ) throws Exception  {
        Predicate left = null;
        Predicate right = null;
        String oper = null;
        for( ParseNode child : root.children() ) {
            if( left == null && child.contains(expr) )
                left = expr(child);
            else if( child.contains(expr) )                           
                right = expr(child);
            else
            	oper = child.content(src);
        }
    	database.addPredicate("?lft",left);
    	database.addPredicate("?rgt",right);
    	Expr e = database.getOperation(oper);
    	Predicate ret = e.eval(database);
        database.removePredicate("?lft");
        database.removePredicate("?rgt");
        return ret;
    }
    
    private Predicate binaryOper( ParseNode root, int oper ) throws Exception  {
        Predicate left = null;
        Predicate right = null;
        for( ParseNode child : root.children() ) {
            if( left == null && child.contains(expr) )
                left = expr(child);
            else if( child.contains(expr) )                           
                right = expr(child);
        }
        if( oper == naturalJoin )
            return Predicate.join(left,right);
        else if( oper == innerUnion )
            return Predicate.innerUnion(left,right);
        else if( oper == unnamedJoin )
            return database.unnamedJoin((Relation)left,(Relation)right);
        else if( oper == unnamedMeet )
            return database.unnamedMeet((Relation)left,(Relation)right);
        else if( oper == setEQ ) {
            if( left instanceof Relation && right instanceof Relation )
                return database.quantifier((Relation)left,(Relation)right,setEQ);
            else
                return Predicate.setEQ(left,right);
        } else if( oper == setIX ) 
            return Predicate.setIX(left,right);
        else if( oper == contains ) 
            return database.quantifier((Relation)left,(Relation)right,contains);
        else if( oper == transpCont ) 
            return database.quantifier((Relation)left,(Relation)right,transpCont);
        else if( oper == disjoint ) 
            return database.quantifier((Relation)left,(Relation)right,disjoint);
        else if( oper == almostDisj ) 
            return database.quantifier((Relation)left,(Relation)right,almostDisj);
        else if( oper == big ) 
            return database.quantifier((Relation)left,(Relation)right,big);
        throw new Exception("Unknown case");
    }
    private Predicate unaryOper( ParseNode root, int oper ) throws Exception  {
        for( ParseNode child : root.children() ) {
            Relation rel = (Relation)expr(child);
            if( oper == complement )
                return database.complement(rel);
            else if( oper == inverse )
                return database.inverse(rel);
        }
        throw new Exception("Unknown case");
    }
        
    private Predicate parExpr( ParseNode root ) throws Exception {
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

            
    private Partition partition( ParseNode root ) throws Exception {
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
                left = (Relation)expr(child);
            else if( child.contains(num) )
                ;
            else                            
                right = (Relation)expr(child);
        }
        return new Partition(left,right,database);
    }

    
    private Relation tuples( ParseNode root ) throws Exception {
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
    private Map<String,Object> tuple( ParseNode root ) throws Exception {
        for( ParseNode child : root.children() ) {
            if( child.contains(values) ) {
                Map<String,Object> tuple = new TreeMap<String,Object>(); 
                values(tuple, child);
                return tuple;
            }
        }
        throw new Exception("Unknown case");
    }
    private void values( Map<String,Object> tuple, ParseNode root ) {
        if( root.contains(namedValue) )
            value(tuple,root);
        else for( ParseNode child : root.children() )
            if( child.contains(namedValue) )
                value(tuple,child);
            else if( child.contains(values) )
                values(tuple,child);
    }
    private void value( Map<String,Object> tuple, ParseNode root ) {
        String left = null;
        Object right = null;
        for( ParseNode child : root.children() ) {
            if( left == null )
                left = child.content(src);
            else if( child.contains(equality) )
                ;
            else { 
                String data = child.content(src);
                try {
                    right = Integer.parseInt(data);
                } catch( NumberFormatException e ) {
                    right = data;
                }
            }
        }
        tuple.put(left, right);
    }

    private List<String> strings( ParseNode root ) throws Exception {
        List<String> ret = new LinkedList<String>();
        if( root.from + 1 == root.to && src.get(root.from).type == Token.IDENTIFIER )
            ret.add(root.content(src));
        else if( root.from + 1 == root.to && src.get(root.from).type == Token.DIGITS )
            throw new Exception("Got number while expected string");
        else
            for( ParseNode child : root.children() )
                ret.addAll(strings(child));
        return ret;
    }   
    private List<Object> values( ParseNode root ) throws Exception {
        List<Object> ret = new LinkedList<Object>();
        if( root.from + 1 == root.to && src.get(root.from).type == Token.IDENTIFIER )
            ret.add(root.content(src));
        else if( root.from + 1 == root.to && src.get(root.from).type == Token.DIGITS  
            ||   root.from + 2 == root.to && "-".equals(src.get(root.from).content) && src.get(root.from+1).type == Token.DIGITS )
            ret.add(Integer.parseInt(root.content(src)));
        else if( root.from + 1 == root.to && src.get(root.from).type == Token.DQUOTED_STRING )
            ret.add(root.content(src).substring(1,root.content(src).length()-1));
        else
            for( ParseNode child : root.children() )
                ret.addAll(values(child));
        return ret;
    }   
    private void addContent( Relation ret, ParseNode root ) throws Exception {
        int i = 0;
        Object[] t = new Object[ret.colNames.length];
        for( Object elem : values(root) ) {
            t[i%ret.colNames.length] = elem;
            if( i%ret.colNames.length == ret.colNames.length-1 ) {
                ret.content.add(new Tuple(t));
                t = new Object[ret.colNames.length];
            }
            i++;
        }
    }
}
