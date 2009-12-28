package qbql.parser;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;


public class BNFGrammar {

    private static CYK cyk = bnfParser();
    private static CYK bnfParser() {
        Set<RuleTuple> rules = new TreeSet<RuleTuple>();
        rules.add(new RuleTuple("variable", new String[] {"identifier"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        rules.add(new RuleTuple("variable", new String[] {"string_literal"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        rules.add(new RuleTuple("concat", new String[] {"variable"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        rules.add(new RuleTuple("concat", new String[] {"concat","variable"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        rules.add(new RuleTuple("disjunct", new String[] {"concat"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        rules.add(new RuleTuple("disjunct", new String[] {"disjunct","'|'","concat"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        rules.add(new RuleTuple("rule", new String[] {"identifier","':'","disjunct","';'"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        rules.add(new RuleTuple("grammar", new String[] {"rule"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        rules.add(new RuleTuple("grammar", new String[] {"grammar", "rule"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        cyk = new CYK(rules) {
            public int[] atomicSymbols() {
                return new int[]{ rule }; //$NON-NLS-1$
            }     
        };
        rule = cyk.symbolIndexes.get("rule");
        grammar = cyk.symbolIndexes.get("grammar");
        identifier = cyk.symbolIndexes.get("identifier");
        variable = cyk.symbolIndexes.get("variable");
        disjunct = cyk.symbolIndexes.get("disjunct");
        concat = cyk.symbolIndexes.get("concat");
        return cyk;
    }
    static int rule;
    static int grammar;
    static int identifier;
    static int variable;
    static int disjunct;
    static int concat;


    public static ParseNode parseGrammarFile( List<LexerToken> src, String input ) throws Exception {
        Matrix ret = cyk.initMatrixSubdiagonal(src);
        int size = ret.size();
        cyk.closure(ret, 0,size+1, new TreeMap<Integer,Integer>(), -1);
        ParseNode root = cyk.forest(size, ret);
        //root.printTree();

        if( !root.contains(grammar) ) { //$NON-NLS-1$
            CYK.printErrors(input, src, root);
            throw new Exception("Parse error in grammar file"); //$NON-NLS-1$
        }
        return root;
    }

    public static Set<RuleTuple> grammar( ParseNode root, List<LexerToken> src ) {
        Set<RuleTuple> ret =  new TreeSet<RuleTuple>();
        if( root.contains(rule) )
            ret.addAll(rule(root, src)); 
        else 
            for( ParseNode child: root.children() ) 
                ret.addAll(grammar(child, src));            
        return ret;
    }

    private static Set<RuleTuple> rule( ParseNode node, List<LexerToken> src ) {
        Set<RuleTuple> ret =  new TreeSet<RuleTuple>();
        String header = null;
        for( ParseNode child: node.children() ) {
            if( child.contains(identifier) ) {
                header = child.content(src); 
            } else if( child.contains(disjunct) ) {
                ret.addAll(disjunct(header, child, src));
            } 
        }
        return ret;
    }

    private static Set<RuleTuple> disjunct( String header, ParseNode node, List<LexerToken> src) {
        Set<RuleTuple> ret =  new TreeSet<RuleTuple>();
        if( node.contains(concat) || node.contains(disjunct) && node.from+1==node.to ) 
            ret.add(concat(header, node, src));
        else 
            for( ParseNode child: node.children() ) 
                if( child.contains(disjunct) ) 
                    ret.addAll(disjunct(header, child, src));
                else if( child.contains(concat) ) 
                    ret.add(concat(header, child, src)); 
        return ret;
    }

    private static RuleTuple concat( String header, ParseNode node, List<LexerToken> src ) {
        List<String> payload =  new LinkedList<String>();
        for( int i = node.from; i < node.to; i++ )
            payload.add(src.get(i).content);
        return new RuleTuple(header,payload);
    }

}
