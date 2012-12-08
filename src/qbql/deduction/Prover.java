package qbql.deduction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
import qbql.symbolic.Expr;
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
        Map<Integer,Eq> theory = new Prover().program(root, src); 
        
        String divider = "-----------------";
        int dividerLoc = text.indexOf(divider);
        
        Theory axioms = new Theory();
        Eq goal = null;
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
   
   public Map<Integer, Eq> program( ParseNode root, List<LexerToken> src ) {
       Map<Integer, Eq> ret = new TreeMap<Integer, Eq>();
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

   public Eq assertion( ParseNode root, List<LexerToken> src ) {
       for( ParseNode child : root.children() ) {
           if( !child.contains(proposition) )
               throw new AssertionError("!child.contains(proposition)");
           return proposition(child, src);         
       }
       throw new AssertionError("empty assertion?");
   }
   public Eq proposition( ParseNode root, List<LexerToken> src ) {
       Expr left = null;
       Expr right = null;
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

       return new Eq(left,right);
   }

   private Expr expr( ParseNode root, List<LexerToken> src ) {
       if( root.from+1 == root.to )
           return compose(null,src.get(root.from).content,null);
       
       Expr left = null;
       String oper = null;
       Expr right = null;
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

       return compose(left,oper,right);
   }
   
   static Expr compose( Expr left, String oper, Expr right ) {
       return new TreeNode((TreeNode)left,oper,(TreeNode)right);
   }
   
   
   //////////////////////////////////////////////////////////////////////////
   
   private static void prove( Theory axioms, Eq goal ) {
       Set<String> usedVars = new HashSet<String>();
       for( Expr ge : goal.expressions )
           usedVars.addAll(ge.variables());
       axioms = axioms.assign(usedVars);
       for( Expr ge : goal.expressions ) {
           List<Expr> tmp = new LinkedList<Expr>();
           tmp.add(ge);
           axioms.add(new Eq(tmp));
       }
       System.out.println(axioms.toString());
       System.out.println("---------------------------------------------------------------");
       
       for (int i = 0; i < 3; i++) {
           axioms.step();
           System.out.println(axioms.toString());
           System.out.println("========================================" +i+ "====================================");
       }
   }



}
