package qbql.lattice;

import java.util.List;

import qbql.parser.LexerToken;
import qbql.parser.ParseNode;

public class Expr {
	String type; // operation name or variable
	Expr lft;
	Expr rgt;
	
	public Expr( String type, Expr lft, Expr rgt ) {
		super();
		this.type = type;
		this.lft = lft;
		this.rgt = rgt;
	}
	
	public Predicate eval( Database d ) throws Exception {
        if( "^".equals(type) )
            return Predicate.join(lft.eval(d),rgt.eval(d));
        else if( "v".equals(type) )
            return Predicate.innerUnion(lft.eval(d),rgt.eval(d));
        else if( "'".equals(type) )
            return d.complement((Relation)lft.eval(d));
        else if( "`".equals(type) )
            return d.inverse((Relation)lft.eval(d));
        else if( "/^".equals(type) )
            return Predicate.setIX((Relation)lft.eval(d), (Relation)rgt.eval(d));
        else if( "/=".equals(type) )
            return d.quantifier((Relation)lft.eval(d),(Relation)rgt.eval(d),Program.setEQ);
        else if( "/<".equals(type) )
            return d.quantifier((Relation)lft.eval(d),(Relation)rgt.eval(d),Program.contains);
        else if( "/>".equals(type) )
            return d.quantifier((Relation)lft.eval(d),(Relation)rgt.eval(d),Program.transpCont);
        else if( "/0".equals(type) )
            return d.quantifier((Relation)lft.eval(d),(Relation)rgt.eval(d),Program.disjoint);
        else if( "/1".equals(type) )
            return d.quantifier((Relation)lft.eval(d),(Relation)rgt.eval(d),Program.almostDisj);
        else if( "/!".equals(type) )
            return d.quantifier((Relation)lft.eval(d),(Relation)rgt.eval(d),Program.big);
        
        else if( type.startsWith("@") ) {
        	throw new AssertionError("No nested defs for now");
        	
        	/*Expr e = d.getOperation(type);
        	d.addPredicate("?lft",d.getPredicate(lft.type));
        	d.addPredicate("?rgt",d.getPredicate(rgt.type));
        	Predicate ret = e.eval(d);
            d.removePredicate("?lft");
            d.removePredicate("?rgt");
            return ret;*/
            
        } else  // variable
        	return d.getPredicate(type);
		
	}
	
	static Expr convert( String l, String r, ParseNode root, List<LexerToken> src ) {
		if( root.from+1 == root.to ) {
			String var = root.content(src);
			if( l.equals(var) )
				return new Expr("?lft",null,null);
			else if( r.equals(var) )
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
        	if( lft == null ) {
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
		if( lft != null )
			sb.append('('+lft.toString()+')');
		sb.append(type);
		if( rgt != null )
			sb.append('('+rgt.toString()+')');
		return sb.toString();
	}

}
