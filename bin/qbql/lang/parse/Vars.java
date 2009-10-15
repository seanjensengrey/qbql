package qbql.lang.parse;

import qbql.lattice.Database;
import qbql.lattice.Relation;
import qbql.lattice.Tuple;
import qbql.parser.ParseNode;
import qbql.util.Util;

public class Vars {
    // Legend: pos,up,down are intervals [from,to)
    // Vars ^ [] = [name pos].  
    // Tokens ^ [] = [txt pos].
    // Links ^ [] = [up down].
    // Paths ^ [] = [up down].
    ParseDb db = null;
    public Vars( Database d ) {
        db = (ParseDb)d;       
    }
    
    public Relation pos_name( String pos ) {
        Relation ret = new Relation(new String[]{"name"});
        ParseNode atPos = db.root.locate(Util.X(pos),Util.Y(pos));
        if( atPos != null )
            for( int name : atPos.content() )
                ret.content.add(new Tuple(new Object[]{db.cyk.allSymbols[name]}));
        return ret;      
    }
    public Relation name_pos( String name ) {
        Relation ret = new Relation(new String[]{"pos"});
        descend(db.root,db.cyk.symbolIndexes.get(name),ret);
        return ret;      
    }
    private void descend( ParseNode root, int name, Relation ret ) {
        if( root.contains(name) )
            ret.content.add(new Tuple(new Object[]{"["+root.from+","+root.to+")"}));
        for( ParseNode child : root.children() )
            descend(child,name,ret);
    }
}
