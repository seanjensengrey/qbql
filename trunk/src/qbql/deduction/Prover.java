package qbql.deduction;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import qbql.induction.TreeNode;
import qbql.lattice.Predicate;
import qbql.lattice.Program;
import qbql.parser.Earley;
import qbql.parser.Lex;
import qbql.parser.LexerToken;
import qbql.parser.Matrix;
import qbql.parser.ParseNode;
import qbql.parser.SyntaxError;
import qbql.util.Util;

public class Prover {
   public static Earley earley;
   public static int naturalJoin;
   public static int innerUnion;
   public static int count;
   static int userDefined;
   static int unaryUserDefined;
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
   static int complement;
   static int CPclosure;
   static int EQclosure;
   static int inverse;
   static int composition;
   static int join;
   static int meet;
   static int equivalence;
   static int equality;
   public static int expr;
   static int num;
   static int minus;
   static int partition;
   static int parExpr;
   public static int openParen;
   public static int closeParen;
   public static int implication;
   public static int inductionFormula;
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
   static int value;
   public static int attribute;
   //static int values;
   //static int namedValue;
   static int comma;
   static {        
       try {
           earley = new Earley(Program.latticeRules());
           naturalJoin = earley.symbolIndexes.get("join");
           userDefined = earley.symbolIndexes.get("userDefined");
           unaryUserDefined = earley.symbolIndexes.get("unaryUserDefined");
           userOper = earley.symbolIndexes.get("userOper");
           innerUnion = earley.symbolIndexes.get("innerUnion");
           count = earley.symbolIndexes.get("count");
           unnamedJoin = earley.symbolIndexes.get("unnamedJoin");
           unnamedMeet = earley.symbolIndexes.get("unnamedMeet");
           setIX = earley.symbolIndexes.get("setIX");
           setEQ = earley.symbolIndexes.get("setEQ");
           contains = earley.symbolIndexes.get("contains");
           transpCont = earley.symbolIndexes.get("transpCont");
           disjoint = earley.symbolIndexes.get("disjoint");
           almostDisj = earley.symbolIndexes.get("almostDisj");
           big = earley.symbolIndexes.get("big");
           complement = earley.symbolIndexes.get("complement");
           CPclosure = earley.symbolIndexes.get("CPclosure");
           EQclosure = earley.symbolIndexes.get("EQclosure");
           inverse = earley.symbolIndexes.get("inverse");
           composition = earley.symbolIndexes.get("composition");
           join = earley.symbolIndexes.get("'v'");
           meet = earley.symbolIndexes.get("'^'");
           equivalence = earley.symbolIndexes.get("'~'");
           equality = earley.symbolIndexes.get("'='");
           minus = earley.symbolIndexes.get("'-'");
           lt = earley.symbolIndexes.get("'<'");
           gt = earley.symbolIndexes.get("'>'");
           amp = earley.symbolIndexes.get("'&'");
           num = earley.symbolIndexes.get("'#'");
           bar = earley.symbolIndexes.get("'|'");
           excl = earley.symbolIndexes.get("'!'");
           expr = earley.symbolIndexes.get("expr");
           partition = earley.symbolIndexes.get("partition");
           parExpr = earley.symbolIndexes.get("parExpr");
           openParen = earley.symbolIndexes.get("'('");
           closeParen = earley.symbolIndexes.get("')'");
           implication = earley.symbolIndexes.get("implication");
           inductionFormula = earley.symbolIndexes.get("inductionFormula");
           proposition = earley.symbolIndexes.get("proposition");
           oper = earley.symbolIndexes.get("oper");
           assertion = earley.symbolIndexes.get("assertion");
           query = earley.symbolIndexes.get("query");

           include = earley.symbolIndexes.get("include");
           filename = earley.symbolIndexes.get("filename");

           identifier = earley.symbolIndexes.get("identifier");
           //string_literal = earley.symbolIndexes.get("string_literal");

           assignment = earley.symbolIndexes.get("assignment");
           relation = earley.symbolIndexes.get("relation");
           table = earley.symbolIndexes.get("table");
           /*tuples = earley.symbolIndexes.get("tuples");
           tuple = earley.symbolIndexes.get("tuple");*/
           header = earley.symbolIndexes.get("header");
           content = earley.symbolIndexes.get("content");
           value = earley.symbolIndexes.get("value");
           attribute = earley.symbolIndexes.get("attribute");
           //values = earley.symbolIndexes.get("values");
           //namedValue = earley.symbolIndexes.get("namedValue");
           //System.out.println(earley.allSymbols[20]);
       } catch( Exception e ) {
           e.printStackTrace(); // (authorized)
       }
   }
          
   public static void main( String[] args ) throws Exception {
        String file = "boolean algebra.axm";
        String axioms = Util.readFile(Prover.class,file);

        List<LexerToken> src =  new Lex().parse(axioms);
        Matrix matrix = new Matrix(earley);
        earley.parse(src, matrix); 
        SyntaxError err = SyntaxError.checkSyntax(axioms, new String[]{"program"}, src, earley, matrix);      
        if( err != null ) {
            System.out.println(err.toString());
            throw new AssertionError("Syntax Error");
        }

        ParseNode root = earley.forest(src, matrix);
        //root.printTree();
        List<Postulate> theory = new Prover().program(root, src); 
        for( Postulate p : theory )
            System.out.println(p.toString());
    }
   
   public List<Postulate> program( ParseNode root, List<LexerToken> src ) {
       List<Postulate> ret = new LinkedList<Postulate>();
       //if( root.contains(include) )
           //return include(root, src);
       if( root.contains(assertion) ) {
           ret.add(assertion(root, src));
           return ret;
       }
       for( ParseNode child : root.children() ) 
           ret.addAll(program(child, src));             
       return ret;
   }

   public Postulate assertion( ParseNode root, List<LexerToken> src ) {
       for( ParseNode child : root.children() ) {
           if( !child.contains(proposition) )
               throw new AssertionError("!child.contains(proposition)");
           return proposition(child, src);         
       }
       throw new AssertionError("empty assertion?");
   }
   public Postulate proposition( ParseNode root, List<LexerToken> src ) {
       TreeNode left = null;
       TreeNode right = null;
       for( ParseNode child : root.children() ) {
           if( left == null )
               left = expr(child, src);
           else if( child.contains(equality) )
               ;
           else {                          
               right = expr(child, src);
               //breaking test: right = right.reEvaluateByUnnesting();
               break;
           }
       }

       return new Postulate(left,right);
   }

   private TreeNode expr( ParseNode root, List<LexerToken> src ) {
       if( root.from+1 == root.to )
           return new TreeNode(null,src.get(root.from).content,null);
       
       TreeNode left = null;
       String oper = null;
       TreeNode right = null;
       for( ParseNode child : root.children() ) {
           if( left == null && !"(".equals(oper) ) {
               if( child.contains(openParen) ) {
                   oper = "(";
                   continue;
               }
               left = expr(child, src);
           } else if( "(".equals(oper) ) 
               return expr(child, src);
           else if( oper == null && child.from+1 == child.to )
               oper = src.get(child.from).content;
           else {                          
               right = expr(child, src);
               //breaking test: right = right.reEvaluateByUnnesting();
               break;
           }
       }

       return new TreeNode(left,oper,right);
   }
}
