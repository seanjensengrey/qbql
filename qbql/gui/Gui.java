package qbql.gui;

import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import qbql.lattice.Database;
import qbql.parser.CYK;
import qbql.parser.LexerToken;
import qbql.parser.Matrix;
import qbql.parser.ParseNode;
import qbql.parser.RuleTuple;
import qbql.util.Util;

public class Gui {   
    private static final String path = "/qbql/gui/";
    private static final String bnf = "grammar.serializedBNF";
    private static CYK customParser() throws Exception{
        return new CYK(RuleTuple.getRules(path+bnf)) {
            public int[] atomicSymbols() {
                return new int[] {};
            }
        };
    }

    
    /////////////////////////GRAMMAR//////////////////////////
    private static Set<RuleTuple> guiRules() {
        Set<RuleTuple> ret = new TreeSet<RuleTuple>();
        ret.add(new RuleTuple("node", new String[] {"layout"}));
        ret.add(new RuleTuple("node", new String[] {"widget"}));
        ret.add(new RuleTuple("nodes", new String[] {"nodes","node"}));
        ret.add(new RuleTuple("nodes", new String[] {"node"}));
        ret.add(new RuleTuple("widget", new String[] {"'button'"}));
        ret.add(new RuleTuple("layout", new String[] {"'('","nodes","')'","'/'","digits"}));
        return ret;
    }
    
    public static void main( String[] args ) throws Exception {
        /*Set<RuleTuple> rules = guiRules();
        RuleTuple.memorizeRules(rules,"c:/qbql_trunk"+path+bnf);
        RuleTuple.printRules(rules);*/
        
        Gui model = new Gui();
        String guiCode = Util.readFile(model.getClass(),path+"test.gui");

        List<LexerToken> src =  LexerToken.parse(guiCode);
        CYK cyk = customParser();
        Matrix matrix = cyk.initArray1(src);
        int size = matrix.size();
        TreeMap<Integer,Integer> skipRanges = new TreeMap<Integer,Integer>();
        cyk.closure(matrix, 0, size+1, skipRanges, -1);
        ParseNode root = cyk.forest(size, matrix);

        if( root.topLevel != null ) {
            System.out.println("*** Parse Error in assertions file ***");
            CYK.printErrors(guiCode, src, root);
        }
        root.printTree();

        //model.iterate(root, src);
        
    }

}

