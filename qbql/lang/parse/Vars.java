package qbql.lang.parse;

import qbql.index.NamedTuple;
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
        ParseNode atPos = db.root.locate(Util.X(pos),Util.Y(pos));
        Relation ret = new Relation(new String[]{"name"});
        for( int name : atPos.content() )
            ret.content.add(new Tuple(new Object[]{db.cyk.allSymbols[name]}));
        return ret;      
    }
}
