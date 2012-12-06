package qbql.deduction;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
   static int proposition;
   static int assertion;
   static int equality;
   static int openParen;

   static int include;
   static int filename;

   static int identifier;
   
   static {        
       try {
           earley = new Earley(Program.latticeRules());
           equality = earley.symbolIndexes.get("'='");
           openParen = earley.symbolIndexes.get("'('");
           proposition = earley.symbolIndexes.get("proposition");
           assertion = earley.symbolIndexes.get("assertion");

           include = earley.symbolIndexes.get("include");
           filename = earley.symbolIndexes.get("filename");

           identifier = earley.symbolIndexes.get("identifier");
       } catch( Exception e ) {
           e.printStackTrace(); // (authorized)
       }
   }
          
   public static void main( String[] args ) throws Exception {
        String file = "boolean algebra.axm";
        String text = Util.readFile(Prover.class,file);

        List<LexerToken> src =  new Lex().parse(text);
        Matrix matrix = new Matrix(earley);
        earley.parse(src, matrix); 
        SyntaxError err = SyntaxError.checkSyntax(text, new String[]{"program"}, src, earley, matrix);      
        if( err != null ) {
            System.out.println(err.toString());
            throw new AssertionError("Syntax Error");
        }

        ParseNode root = earley.forest(src, matrix);
        //root.printTree();
        Map<Integer,Postulate> theory = new Prover().program(root, src); 
        
        String divider = "-----------------";
        int dividerLoc = text.indexOf(divider);
        
        List<Postulate> axioms = new LinkedList<Postulate>();
        Postulate goal = null;
        for( int i : theory.keySet() ) {
            if( i < dividerLoc ) 
                axioms.add(theory.get(i));
            else {
                if( goal != null )
                    throw new AssertionError("more than one goal");
                goal = theory.get(i);
            }
            //System.out.println(theory.get(i).toString());
        }
        prove(axioms,goal);
    }
   
   public Map<Integer, Postulate> program( ParseNode root, List<LexerToken> src ) {
       Map<Integer, Postulate> ret = new TreeMap<Integer, Postulate>();
       //if( root.contains(include) )
       //return include(root, src);
       if( root.contains(assertion) ) {
           ret.put(src.get(root.from).begin ,assertion(root, src));
           return ret;
       }
       for( ParseNode child : root.children() ) 
           ret.putAll(program(child, src));             
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
   
   //////////////////////////////////////////////////////////////////////////
   
   private static void prove( List<Postulate> axioms, Postulate goal ) {
       for( Postulate i : axioms ) {
           Postulate o = i.substitute("x",  new TreeNode(new TreeNode(null,"x",null),"'",null));
           System.out.println(o.toString());
       }
   }


}
