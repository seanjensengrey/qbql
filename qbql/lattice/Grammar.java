package qbql.lattice;

import java.util.Set;
import java.util.TreeSet;

import qbql.parser.RuleTuple;

public class Grammar {

    private static final String fname = "grammar.serializedBNF";
    private static final String path = "/qbql/lattice/";
    private static final String location = "c:/qbql_trunk"+path+fname;
    private static Set<RuleTuple> latticeRules() {
        Set<RuleTuple> ret = new TreeSet<RuleTuple>();
        // LATTICE part
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

        // Set Theoretic part
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
        ret.add(new RuleTuple("expr", new String[] {"partition"}));
        ret.add(new RuleTuple("assignment", new String[] {"identifier","'='","expr","';'"})); // if defined in terms of lattice operations
        ret.add(new RuleTuple("database", new String[] {"assignment"}));
        ret.add(new RuleTuple("database", new String[] {"database","assignment"}));
        return ret;
    }

    public static void main( String[] args ) throws Exception {
        Set<RuleTuple> rules = latticeRules();
        RuleTuple.memorizeRules(rules,location);
        RuleTuple.printRules(rules);
    }
}
