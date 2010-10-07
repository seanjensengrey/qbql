package qbql.lattice;

import java.util.List;

import qbql.parser.LexerToken;
import qbql.parser.ParseNode;

public class Expr {
	String type; // operation name or variable
	Expr left;
	Expr right;
	
	public Expr( String type, Expr lft, Expr rgt ) {
		super();
		this.type = type;
		this.left = lft;
		this.right = rgt;
	}
	
	public Predicate eval( Database d ) {
        if( "^".equals(type) )
            return Predicate.join(left.eval(d),right.eval(d));
        else if( "v".equals(type) )
            return Predicate.union(left.eval(d),right.eval(d));
        else if( "'".equals(type) )
            return d.complement(left.eval(d));
        else if( "`".equals(type) )
            return d.inverse((Relation)left.eval(d));
        else if( "/^".equals(type) )
            return Predicate.setIX((Relation)left.eval(d), (Relation)right.eval(d));
        else if( "/=".equals(type) )
            return d.quantifier((Relation)left.eval(d),(Relation)right.eval(d),Program.setEQ);
        else if( "/<".equals(type) )
            return d.quantifier((Relation)left.eval(d),(Relation)right.eval(d),Program.contains);
        else if( "/>".equals(type) )
            return d.quantifier((Relation)left.eval(d),(Relation)right.eval(d),Program.transpCont);
        else if( "/0".equals(type) )
            return d.quantifier((Relation)left.eval(d),(Relation)right.eval(d),Program.disjoint);
        else if( "/1".equals(type) )
            return d.quantifier((Relation)left.eval(d),(Relation)right.eval(d),Program.almostDisj);
        else if( "/!".equals(type) )
            return d.quantifier((Relation)left.eval(d),(Relation)right.eval(d),Program.big);
        
        else if( type.startsWith("<") ) {
        	
        	Expr e = d.getOperation(type);
        	Predicate lft = d.getPredicate("?lft");
        	Predicate rgt = d.getPredicate("?rgt");
            Predicate l = left.eval(d);
            Predicate r = right.eval(d);
        	d.addPredicate("?lft",l);
        	d.addPredicate("?rgt",r);
        	Predicate ret = e.eval(d);
            d.removePredicate("?lft");
            d.removePredicate("?rgt");
            if( lft != null )
            	d.addPredicate("?lft",lft);
            if( rgt != null )
            	d.addPredicate("?rgt",rgt);
            return ret;
            
        } else  // variable
        	return d.getPredicate(type);
		
	}
	
	static Expr convert( String l, String r, ParseNode root, List<LexerToken> src ) {
		if( root.from+1 == root.to ) {
			String var = root.content(src);
			if( l.equals(var) )
				return new Expr("?lft",null,null);
			else if( var.equals(r) )
				return new Expr("?rgt",null,null);
			else
				return new Expr(var,null,null); // e.g. R00
		} 
		
		if( root.contains(Program.parExpr) )
	        for( ParseNode child : root.children() ) {
	    		if( !child.contains(Program.openParen) )
	    			return convert(l,r,child,src);
	        }
		
		String oper = ""; 
		Expr lft = null;
		Expr rgt = null;
        for( ParseNode child : root.children() ) {
        	if( lft == null && child.contains(Program.userOper) ) {
				lft = new Expr("R00",null,null);
				oper = child.content(src);
			} else if( lft == null ) {
        		lft = convert(l,r,child,src);
        	} else if( child.contains(Program.expr) ) {
        		rgt = convert(l,r,child,src);
        	} else
        		oper += child.content(src);        		
        }
		return new Expr(oper,lft,rgt);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if( left != null )
			sb.append('('+left.toString()+')');
		sb.append(type);
		if( right != null )
			sb.append('('+right.toString()+')');
		return sb.toString();
	}

}
