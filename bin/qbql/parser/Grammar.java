package qbql.parser;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class Grammar {
    private static CYK cyk = bnfParser();
    private static CYK bnfParser() {
        Set<RuleTuple> rules = new TreeSet<RuleTuple>();
        rules.add(new RuleTuple("variable", new String[] {"identifier"}));                            //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        rules.add(new RuleTuple("variable", new String[] {"string_literal"}));                             //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        rules.add(new RuleTuple("concat", new String[] {"variable"}));                             //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        rules.add(new RuleTuple("concat", new String[] {"concat","variable"}));                             //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        rules.add(new RuleTuple("disjunct", new String[] {"concat"}));                             //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        rules.add(new RuleTuple("disjunct", new String[] {"'|'","concat"}));                             //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        rules.add(new RuleTuple("disjunct", new String[] {"disjunct","'|'","concat"}));                             //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        rules.add(new RuleTuple("disjunct", new String[] {"disjunct","'|'"}));                             //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        rules.add(new RuleTuple("rule", new String[] {"variable","':'","disjunct","';'"}));                             //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        rules.add(new RuleTuple("rule", new String[] {"variable","':'","';'"}));                             //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        rules.add(new RuleTuple("rule", new String[] {"'-'","variable","';'"}));                             //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        rules.add(new RuleTuple("grammar", new String[] {"rule"}));                             //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        rules.add(new RuleTuple("grammar", new String[] {"grammar", "rule"}));                             //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        Set<RuleTuple> nonEmptyRules = rules; //RuleTransforms.eliminateEmptyProductions(rules); <--automatically transformed output is messy
        //RuleTuple.printRules(nonEmptyRules);
		cyk = new CYK(nonEmptyRules) {
            public int[] atomicSymbols() {
                return new int[]{ rule }; //$NON-NLS-1$
            }     
        };
        rule = cyk.symbolIndexes.get("rule");
        grammar = cyk.symbolIndexes.get("grammar");
        variable = cyk.symbolIndexes.get("variable");
        disjunct = cyk.symbolIndexes.get("disjunct");
        concat = cyk.symbolIndexes.get("concat");
        or = cyk.symbolIndexes.get("'|'");
        minus = cyk.symbolIndexes.get("'-'");
        return cyk;
    }
    static int rule;
    static int grammar;
    static int variable;
    static int disjunct;
    static int concat;
    static int or;
    static int minus;

    public static void main( String[] args ) throws Exception {
    	String input = "x: a b c | A | B;";
        List<LexerToken> src = (new Lex()).parse(input); 
        ParseNode root = Grammar.parseGrammarFile(src, input);
        root.printTree();
        Set<RuleTuple> rules = Grammar.grammar(root, src);
        RuleTuple.printRules(rules);
	}
    
    public static ParseNode parseGrammarFile( List<LexerToken> src, String input ) throws Exception {
        Visual visual = null;
        //visual = new Visual(src, cyk);
        Matrix matrix = cyk.initArray1(src);
        int size = matrix.size();
        cyk.closure(matrix, 0,size+1, new TreeMap<Integer,Integer>(), -1);
        if( visual != null )
            visual.draw(matrix);       
        ParseNode root = cyk.forest(src, matrix);
        //root.printTree();

        if( !root.contains(grammar) ) { 
            CYK.printErrors(input, src, root);
            throw new Exception("Parse error in grammar file"); 
        }
        return root;
    }
    
    public static void grammar( ParseNode root, List<LexerToken> src, Set<RuleTuple> grammar ) {
        if( root.contains(rule) ) 
            rule(root, src, grammar); 
        else for( ParseNode child: root.children() ) 
            grammar(child, src, grammar);            
    }
    public static Set<RuleTuple> grammar( ParseNode root, List<LexerToken> src ) {
        Set<RuleTuple> rules = new TreeSet<RuleTuple>();
        grammar(root, src, rules);
        return rules;
    }

	private static void rule( ParseNode node, List<LexerToken> src, Set<RuleTuple> grammar ) {
        String header = null;
        Set<RuleTuple> disj = null;
        for( ParseNode child: node.children() ) {
            if( header==null && child.contains(minus) ) {
                delete(node, src, grammar);
                return;
            }
                
            if( header==null && child.contains(variable) ) {
                header = child.content(src); 
            } else if( child.contains(disjunct) ) {
                disj = disjunct(header, child, src);
				grammar.addAll(disj);
            } 
        }
        if( disj == null )
        	grammar.add(new RuleTuple(header, new String[]{}));
    }

    private static void delete( ParseNode node, List<LexerToken> src, Set<RuleTuple> grammar ) {
        String var = null;
        for( ParseNode child: node.children() ) 
            if( var==null && child.contains(variable) ) {
                var = child.content(src);
                break;
            }
        Set<RuleTuple> deletions = new TreeSet<RuleTuple>();
        for( RuleTuple t : grammar )
            if( t.head.equals(var) ) 
                deletions.add(t);
            else for( String rhs : t.rhs )
                if( rhs.equals(var) ) {
                    deletions.add(t);
                    break;
                }
        
        grammar.removeAll(deletions);        
    }

    private static Set<RuleTuple> disjunct( String header, ParseNode node, List<LexerToken> src) {
        Set<RuleTuple> ret =  new TreeSet<RuleTuple>();
        if( node.contains(concat) || node.contains(disjunct) && node.from+1==node.to ) 
            ret.add(concat(header, node, src));
        else {
            int cnt = -1;
            int lastOrPos = -1;
            for( ParseNode child: node.children() )  {
                cnt++;
                if( child.contains(disjunct) ) 
                    ret.addAll(disjunct(header, child, src));
                else if( child.contains(concat) ) 
                    ret.add(concat(header, child, src));
                if( lastOrPos+1 == cnt && child.contains(or))
                    ret.add(new RuleTuple(header, new String[]{}));
                if( child.contains(or) ) {
                    lastOrPos = cnt;
                    if( cnt == node.children().size()-1 )
                    	 ret.add(new RuleTuple(header, new String[]{}));
                }
            }
        }
        return ret;
    }

    private static RuleTuple concat( String header, ParseNode node, List<LexerToken> src ) {
        List<String> payload =  new LinkedList<String>();
        for( int i = node.from; i < node.to; i++ )
            payload.add(src.get(i).content);
        return new RuleTuple(header,payload);
    }

}
