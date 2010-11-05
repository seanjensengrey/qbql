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
import qbql.parser.Matrix;
import qbql.parser.ParseNode;
import qbql.parser.RuleTuple;
import qbql.parser.Token;
import qbql.program.Run;
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
    static int userOper;
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
    public static int closeParen;
    public static int implication;
    static int proposition;
    static int oper;
    static int lt;
    static int gt;
    static int amp;
    static int bar;
    static int excl;
    public static int assertion;
    static int query;
    
    static int include;
    static int filename;
    
    public static int identifier;
    //public static int string_literal;


    static int assignment;
    static int relation;
    static int table;
    /*static int tuples;
    static int tuple;*/
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
            userOper = cyk.symbolIndexes.get("userOper");
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
            closeParen = cyk.symbolIndexes.get("')'");
            implication = cyk.symbolIndexes.get("implication");
            proposition = cyk.symbolIndexes.get("proposition");
            oper = cyk.symbolIndexes.get("oper");
            assertion = cyk.symbolIndexes.get("assertion");
            query = cyk.symbolIndexes.get("query");

            include = cyk.symbolIndexes.get("include");
            filename = cyk.symbolIndexes.get("filename");

            identifier = cyk.symbolIndexes.get("identifier");
            //string_literal = cyk.symbolIndexes.get("string_literal");

            assignment = cyk.symbolIndexes.get("assignment");
            relation = cyk.symbolIndexes.get("relation");
            table = cyk.symbolIndexes.get("table");
            /*tuples = cyk.symbolIndexes.get("tuples");
            tuple = cyk.symbolIndexes.get("tuple");*/
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
    
    private static Set<RuleTuple> latticeRules()  {
        String input;
		try {
			input = Util.readFile(Program.class, "lattice.grammar");
		} catch (Exception e) {
			throw new AssertionError("Faikled to read lattice.grammar file");
		}
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
    
    public Database database;
    public Program( Database db ) {
        this.database = db;
    }

    private boolean bool( ParseNode root, List<LexerToken> src ) {
        for( ParseNode child : root.children() ) 
            if( child.contains(expr) || child.contains(partition) )
                return atomicProposition(root,src);
            else 
                return logical(root,src);
        throw new AssertionError("Impossible exception, no children??"+root.content(src));
    }

    private boolean logical( ParseNode root, List<LexerToken> src )  {
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
                    left = bool(child, src);
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
                        return bool(child, src);
                } else if( oper == bar ) {
                    //return left | bool(child);
                    if( left )
                        return true;
                    else
                        return bool(child, src);
                } else if( oper == minus && !child.contains(openParen) )
                    return ! bool(child, src);
                else if( oper == openParen )
                    return bool(child, src);
                else if( impl && !bimpl ) { 
                    //return !left | bool(child);
                    if( !left )
                        return true;
                    else {
                    	boolean ret = bool(child, src);
                    	/*if( ret ) {
                            for( String variable : variables(root, false) )
                            	System.out.println(variable+" = "
                            			+database.predicate(variable).toString(variable.length()+3, false)
                            			+";");

                    	}*/
                        return ret;
                    }
                } else if( !impl && bimpl ) 
                    return left | !bool(child, src);
                else if( impl && bimpl ) 
                    return left == bool(child, src);
            }
        }
        throw new AssertionError("Unknown boolean operation "+oper);
    }

    private boolean atomicProposition( ParseNode root, List<LexerToken> src ) {
        for( ParseNode child : root.children() ) {
            if( child.contains(partition) )
                return partitionProposition(root, src);
            else if( child.contains(expr) )
                return relationalProposition(root,src);
            break;
        }
        throw new AssertionError("VT: neither relational not partitional proposition");
    }
    private boolean partitionProposition( ParseNode root, List<LexerToken> src ) {
        int oper = -1;
        Partition left = null;
        Partition right = null;
        for( ParseNode child : root.children() ) {
            if( left == null )
                left = partition(child, src);
            else if( child.contains(lt) )
                oper = lt;
            else if( child.contains(gt) )
                oper = gt;
            else if( child.contains(equality) )
                oper = equality;
            else
                right = partition(child, src);
        }
        if( oper == lt )
            return Partition.le(left,right);
        if( oper == gt )
            return Partition.ge(left,right);
        if( oper == equality )
            return left.equals(right);

        throw new AssertionError("Impossible case");             
    }
    private boolean relationalProposition( ParseNode root, List<LexerToken> src ) {
        int oper = -1;
        Predicate left = null;
        Predicate right = null;
        boolean not = false;
        for( ParseNode child : root.children() ) {
            if( left == null )
                left = expr(child, src);
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
                right = expr(child, src);
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

            throw new AssertionError("Impossible case");             
    }

    boolean outputVariables = false;
    
    public ParseNode assertion( ParseNode root, List<LexerToken> src ) {
        ParseNode ret = null;
        for( ParseNode child : root.children() ) {
        	if( isDeclaration(child, src) )
        		return null;
            String[] ops = operIneqArgs(child,src);
            if( ops == null )
            	break;
            if( assertEqOp(ops[0], ops[1]) )
            	return child;
        	return null;
        }
        
        Set<String> variables = variables(root,src);
        
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
                    if( !bool(child, src) ) {
                        for( String variable : variables )
                            if( outputVariables )
                                System.out.println(variable+" = "
                                                   +database.getPredicate(variable).toString(variable.length()+3)
                                                   +";");
                        ret = child;
                        for( String variable : variables )
                            database.removePredicate(variable);
                        return ret;
                    } 
                    break;
                } else
                	throw new AssertionError("Non boolean assertion???"); 
            }
        } while( Util.next(indexes,tables.length) );

        for( String variable : variables )
            database.removePredicate(variable);
        return ret;
    }
    private String[] operIneqArgs( ParseNode root, List<LexerToken> src )  {
        if( root.contains(proposition) ) {
        	String[] ret = new String[2];
        	boolean sawEq = false;
        	boolean sawExcl = false;
            for( ParseNode child : root.children() ) {
            	if( ret[0] == null ) 
            		if( !child.contains(oper) )
            			break;
            		else {
            			ret[0] = child.content(src);
            			continue;
            		}
            	if( !sawExcl ) 
               		if( !child.contains(excl) )
               			throw new AssertionError("Expected '!'");
            		else {
            			sawExcl = true;
            			continue;
            		}
            	if( !sawEq ) 
               		if( !child.contains(equality) )
               			throw new AssertionError("Expected '='");
            		else {
            			sawEq = true;
            			continue;
            		}
            	if( ret[1] == null) {
            		ret[1] = child.content(src);
            		return ret;
            	} else
            		throw new AssertionError("UnExpected case");
            }
        	
        }
		return null;
	}
	private boolean assertEqOp( String op1, String op2 ) {
        String input = "x "+op1+" y = x "+op2+" y.";
        
        LinkedList<LexerToken> src = new Lex().parse(input);
        Matrix matrix = Program.cyk.initMatrixSubdiagonal(src);
        int size = matrix.size();
        Program.cyk.closure(matrix, 0, size+1, null, -1);
        ParseNode root = Program.cyk.forest(size, matrix);
        if( !root.contains(Program.cyk.symbolIndexes.get("assertion") ) )
            throw new AssertionError("Parse Eror");     
        
        return assertion(root,src)==null;
	}

	private boolean isDeclaration( ParseNode root, List<LexerToken> src ) throws AssertionError {
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
            			if( lft == null && grandChild.contains(userOper) ) {
            				lft = "R00";
            				operation = grandChild.content(src);
            				try { 
            					database.getOperation(operation);
            					return false;
            				} catch( AssertionError e ) {} 
            			} else if( lft == null ) 
            				lft = grandChild.content(src);
            			else if( operation == null ) {
            				operation = grandChild.content(src);
            				try { 
            					database.getOperation(operation);
            					return false;
            				} catch( AssertionError e ) {} 
            			} else if( rgt == null ) 
            				rgt = grandChild.content(src);
            			else if( grandChild.contains(closeParen) ) 
            				;
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


	private Set<String> variables( ParseNode root, List<LexerToken> src, boolean notAssignedOnes ) {
        Set<String> variables = new HashSet<String>();
        for( ParseNode descendant : root.descendants() ) {
            String id = descendant.content(src);
            if( descendant.from+1 == descendant.to 
                    && (descendant.contains(expr) || descendant.contains(identifier))
                    && !root.parent(descendant.from, descendant.to).contains(header)
                    && !root.parent(descendant.from, descendant.to).contains(table)
                    && !root.parent(descendant.from, descendant.to).contains(userOper)
                    && (!notAssignedOnes || lookup(id) == null) ) 
                variables.add(id);
        }
        return variables;
    }
    public Set<String> variables( ParseNode root, List<LexerToken> src ) {
    	return variables(root, src, true);
    }

    private ParseNode query( ParseNode root, List<LexerToken> src )  {
        for( ParseNode child : root.children() ) {
            if( child.contains(partition) ) {
                System.out.println(child.content(src)+"="+partition(child, src).toString()+";");
                return null;
            } else if( child.contains(expr) ) {
                System.out.println(child.content(src)+"="+expr(child, src).toString(child.content(src).length()+1)+";");
                return null;
            } 
        }
        throw new AssertionError("No expr/partition in statement?");
    }
    /**
     * @param root
     * @param src
     * @return node that violates assertion
     * @
     */
    public ParseNode program( ParseNode root, List<LexerToken> src ) {
        if( root.contains(include) )
            return include(root, src);
        if( root.contains(assertion) )
            return assertion(root, src);
        if( root.contains(query) )
            return query(root, src);
        if( root.contains(assignment) ) {
            createPredicate(root, src);
            return null;
        }
        ParseNode ret = null;
        for( ParseNode child : root.children() ) {
            ret = program(child, src);       
            if( ret != null )
                return ret;
        }
        return ret;
    }


    private ParseNode include( ParseNode root, List<LexerToken> src ) {
        String prg = null;	
        String fname = null;
		try {
			StringBuilder fn = new StringBuilder();
			for( int i = root.from+1; i < root.to-1; i++ ) 
				fn.append(src.get(i).content);
			fname = fn.toString();
			if( fname.startsWith("\"") )
				prg = Util.readFile(fname.substring(1,fname.length()-1));
			else
				prg = Util.readFile(Run.class,fname);
			System.out.println("Processing "+fname+" ...");
		} catch ( Exception e ) {
			throw new RuntimeException("failed to read the file "+fname);
		}
		try {
			run(prg);
		} catch( AssertionError e ) {
			if( Program.PARSE_ERROR_IN_ASSERTIONS_FILE.equals(e.getMessage()) ) {
				System.out.println("^^^ Parse error in "+fname.toString());
			}
			throw e;
		}
		return null;
	}

	private void createPredicate( ParseNode root, List<LexerToken> src )  {
        String left = null;
        Predicate right = null;
        for( ParseNode child : root.children() ) {
            if( left == null )
                left = child.content(src);
            else if( child.contains(equality) )
                ;
            else {                          
                right = expr(child, src);
                break;
            }
        }
        database.addPredicate(left, right);
    }
    private Predicate expr( ParseNode root, List<LexerToken> src ) {
        /*if( root.contains(relation) ) {
            for( ParseNode child : root.children() ) {
                if( child.contains(tuples) )
                    return tuples(child, src);
            }
        } else*/ if( root.contains(table) ) {
            Relation ret = Database.R00;
            String colX = null;
            for( ParseNode child : root.children() ) {
                if( child.contains(header) )                    
                    ret = new Relation(values(child, src).toArray(new String[0]));
                else if( child.contains(content) ) {
                    addContent(ret,child, src);
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
                throw new RuntimeAssertionError();
            for( int i = 0; i < ret.colNames.length; i++ ) {
                ret.renameInPlace(ret.colNames[i], columns.get(i));
            }
            return ret;
        }*/ else if( root.contains(naturalJoin) ) 
            return binaryOper(root,src, naturalJoin);
        else if( root.contains(userDefined) ) 
            return userDefined(root,src);
        else if( root.contains(innerUnion) ) 
            return binaryOper(root,src, innerUnion);
        else if( root.contains(unnamedJoin) ) 
            return binaryOper(root,src, unnamedJoin);
        else if( root.contains(unnamedMeet) ) 
            return binaryOper(root,src, unnamedMeet);
        else if( root.contains(complement) ) 
            return unaryOper(root,complement, src);
        else if( root.contains(inverse) ) 
            return unaryOper(root,inverse, src);
        else if( root.contains(setIX) ) 
            return binaryOper(root,src, setIX);
        else if( root.contains(setEQ) ) 
            return binaryOper(root,src, setEQ);
        else if( root.contains(contains) ) 
            return binaryOper(root,src, contains);
        else if( root.contains(transpCont) ) 
            return binaryOper(root,src, transpCont);
        else if( root.contains(disjoint) ) 
            return binaryOper(root,src, disjoint);
        else if( root.contains(almostDisj) ) 
            return binaryOper(root,src, almostDisj);
        else if( root.contains(big) ) 
            return binaryOper(root,src, big);
        
        else if( root.contains(parExpr) ) 
            return parExpr(root, src);
        
        else if( root.contains(identifier) || root.from+1 == root.to ) {
            LexerToken token = src.get(root.from);
			String name = token.type == Token.DQUOTED_STRING ? token.content.substring(1, token.content.length()-1) : token.content;
            Predicate candidate = lookup(name);
            if( candidate == null )
                throw new AssertionError("Predicate/Table '"+name+"' not in the database");
			return candidate;
        }
                    
        throw new AssertionError("Unknown case");
    }

    private Predicate lookup( String name ) {
        Predicate ret = database.getPredicate(name);
        if( ret != null ) 
            return ret;
        try {
            return new IndexedPredicate(database,name);
        } catch ( Exception e ) {
            return null;
        }
    }
    
    private Predicate userDefined( ParseNode root, List<LexerToken> src )   {
        Predicate left = null;
        Predicate right = null;
        String oper = null;
        for( ParseNode child : root.children() ) {
            if( left == null && child.contains(userOper) ) { // e.g. ( <NOT> x )
            	left = Database.R00; 
            	oper = child.content(src);
            } else if( left == null && child.contains(expr) )
                left = expr(child, src);
            else if( child.contains(expr) ) {                           
                right = expr(child, src);
                break;  // in order not to step on closing parenthesis
            } else
            	oper = child.content(src);
        }
        Predicate lft = database.getPredicate("?lft");
        Predicate rgt = database.getPredicate("?rgt");
    	database.addPredicate("?lft",left);
    	database.addPredicate("?rgt",right);
    	Expr e = database.getOperation(oper);
    	Predicate ret = e.eval(database);
        database.removePredicate("?lft");
        database.removePredicate("?rgt");
        if( lft != null )
        	database.addPredicate("?lft",lft);
        if( rgt != null )
        	database.addPredicate("?rgt",rgt);
        return ret;
    }
    
    private Predicate binaryOper( ParseNode root, List<LexerToken> src, int oper )   {
        Predicate left = null;
        Predicate right = null;
        for( ParseNode child : root.children() ) {
            if( left == null && child.contains(expr) )
                left = expr(child,src);
            else if( child.contains(expr) )                           
                right = expr(child,src);
        }
        if( oper == naturalJoin )
            return Predicate.join(left,right);
        else if( oper == innerUnion )
            return Predicate.union(left,right);
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
        throw new AssertionError("Unknown case");
    }
    private Predicate unaryOper( ParseNode root, int oper, List<LexerToken> src )   {
        for( ParseNode child : root.children() ) {
            Predicate rel = expr(child,src);
            if( oper == complement )
                return database.complement(rel);
            else if( oper == inverse )
                return database.inverse((Relation)rel);
        }
        throw new AssertionError("Unknown case");
    }
        
    private Predicate parExpr( ParseNode root, List<LexerToken> src )  {
        boolean parenthesis = false;
        for( ParseNode child : root.children() ) {
            if( child.contains(openParen) ) {
                parenthesis = true;
                continue;
            }
            if( parenthesis ) 
                return expr(child,src);           
        }
        throw new AssertionError("No parenthesis found");
    }

            
    private Partition partition( ParseNode root, List<LexerToken> src ) {
        boolean parenthesis = false;
        for( ParseNode child : root.children() ) {
            if( child.contains(openParen) ) {
                parenthesis = true;
                continue;
            }
            if( parenthesis ) 
                return partition(child, src);
            if( child.contains(partition) )
                return partPart(root, src);
            else if( child.contains(expr) )
                return relPart(root, src);
            break;
        }
        throw new AssertionError("Unknown case");
    }
    private Partition partPart( ParseNode root, List<LexerToken> src ) {
        Partition left = null;
        Partition right = null;
        int oper = -1;
        for( ParseNode child : root.children() ) {
            if( left == null )
                left = partition(child, src);
            else if( child.contains(join) )
                oper = join;
            else if( child.contains(meet) )
                oper = meet;
            else                            
                right = partition(child, src);
        }
        if( oper == join )
            return Partition.union(left,right);
        if( oper == meet )
            return Partition.intersect(left,right);

        throw new AssertionError("Impossible case");             
    }
    private Partition relPart( ParseNode root, List<LexerToken> src )  {
        Relation left = null;
        Relation right = null;
        for( ParseNode child : root.children() ) {
            if( left == null )
                left = (Relation)expr(child,src);
            else if( child.contains(num) )
                ;
            else                            
                right = (Relation)expr(child,src);
        }
        return new Partition(left,right,database);
    }

    
    /*private Relation tuples( ParseNode root, List<LexerToken> src )  {
        Set<String> attrs = new TreeSet<String>();
        for( ParseNode descendant: root.descendants() )
            if( descendant.contains(attribute) )
                attrs.add(descendant.content(src));
        Relation ret = new Relation(attrs.toArray(new String[0]));

        addTuples(ret,root, src);            
        return ret;
    }
    private void addTuples( Relation ret, ParseNode root, List<LexerToken> src )  {
        if( root.contains(tuple) ) 
            ret.addTuple(tuple(root, src));
        else for( ParseNode child : root.children() )
            if( child.contains(tuple) )
                ret.addTuple(tuple(child, src));
            else if( child.contains(tuples) )
                addTuples(ret,child, src);
    }
    private Map<String,Object> tuple( ParseNode root, List<LexerToken> src )  {
        for( ParseNode child : root.children() ) {
            if( child.contains(values) ) {
                Map<String,Object> tuple = new TreeMap<String,Object>(); 
                values(tuple, child, src);
                return tuple;
            }
        }
        throw new AssertionError("Unknown case");
    }*/
    
    private void values( Map<String,Object> tuple, ParseNode root, List<LexerToken> src ) {
        if( root.contains(namedValue) )
            value(tuple,root, src);
        else for( ParseNode child : root.children() )
            if( child.contains(namedValue) )
                value(tuple,child, src);
            else if( child.contains(values) )
                values(tuple,child, src);
    }
    private void value( Map<String,Object> tuple, ParseNode root, List<LexerToken> src ) {
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

    private List<String> strings( ParseNode root, List<LexerToken> src )  {
        List<String> ret = new LinkedList<String>();
        if( root.from + 1 == root.to && src.get(root.from).type == Token.IDENTIFIER )
            ret.add(root.content(src));
        else if( root.from + 1 == root.to && src.get(root.from).type == Token.DIGITS )
            throw new AssertionError("Got number while expected string");
        else
            for( ParseNode child : root.children() )
                ret.addAll(strings(child,src));
        return ret;
    }   
    private List<Object> values( ParseNode root, List<LexerToken> src ) {
        List<Object> ret = new LinkedList<Object>();
        if( root.contains(parExpr) )
            ret.add(parExpr(root, src));
        else if( root.from + 1 == root.to && src.get(root.from).type == Token.IDENTIFIER ) 
            ret.add(root.content(src));
        else if( root.from + 1 == root.to && src.get(root.from).type == Token.DIGITS  
            ||   root.from + 2 == root.to && "-".equals(src.get(root.from).content) && src.get(root.from+1).type == Token.DIGITS )
            ret.add(Integer.parseInt(root.content(src)));
        else if( root.from + 1 == root.to && src.get(root.from).type == Token.DQUOTED_STRING )
            ret.add(root.content(src).substring(1,root.content(src).length()-1));
        else
            for( ParseNode child : root.children() )
                ret.addAll(values(child,src));
        return ret;
    }   
    private void addContent( Relation ret, ParseNode root, List<LexerToken> src ) {
        int i = 0;
        Object[] t = new Object[ret.colNames.length];
        for( Object elem : values(root, src) ) {
            t[i%ret.colNames.length] = elem;
            if( i%ret.colNames.length == ret.colNames.length-1 ) {
                ret.content.add(new Tuple(t));
                t = new Object[ret.colNames.length];
            }
            i++;
        }
    }
    
    
    static final String FALSE_ASSERTION = "*** False Assertion ***";
	static final String PARSE_ERROR_IN_ASSERTIONS_FILE = "*** Parse Error in assertions file ***";
    public void run( String prg ) {
        List<LexerToken> src =  new Lex().parse(prg);
        Matrix matrix = Program.cyk.initMatrixSubdiagonal(src);
        int size = matrix.size();
        TreeMap<Integer,Integer> skipRanges = new TreeMap<Integer,Integer>();
        Program.cyk.closure(matrix, 0, size+1, skipRanges, -1);
        ParseNode root = Program.cyk.forest(size, matrix);

        if( root.topLevel != null ) {
            System.out.println(PARSE_ERROR_IN_ASSERTIONS_FILE);
            CYK.printErrors(prg, src, root);
            throw new AssertionError(PARSE_ERROR_IN_ASSERTIONS_FILE);
        }

        long t1 = System.currentTimeMillis();
        outputVariables = true;
        ParseNode exception = program(root,src);
        long t2 = System.currentTimeMillis();
        System.out.println("Time = "+(t2-t1)); 
        if( exception != null ) {
            System.out.println(FALSE_ASSERTION);
            System.out.println(prg.substring(src.get(exception.from).begin, src.get(exception.to-1).end));
            throw new AssertionError(FALSE_ASSERTION);
        }
    }

}
