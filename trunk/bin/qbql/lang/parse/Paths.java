package qbql.lang.parse;

import qbql.index.NamedTuple;
import qbql.lattice.Database;
import qbql.lattice.Relation;
import qbql.lattice.Tuple;
import qbql.parser.ParseNode;
import qbql.util.Util;

public class Paths {

    // Legend: pos,up,down are intervals [from,to)
    // Vars ^ [] = [name pos].  
    // Tokens ^ [] = [txt pos].
    // Links ^ [] = [up down].
    // Paths ^ [] = [up down].
    ParseDb db = null;
    public Paths( Database d ) {
        db = (ParseDb)d;       
    }
    
    public Relation down_up( String pos ) throws AssertionError {
        ParseNode atPos = db.root.locate(Util.X(pos),Util.Y(pos));
        Relation ret = new Relation(new String[]{"up"});
        for( ParseNode ancestor : db.root.intermediates(atPos.from,atPos.to) ) {
           ret.content.add(new Tuple(new Object[]{"["+ancestor.from+","+ancestor.to+")"}));
        }
        return ret;      
    }
    public Relation up_down( String pos ) {
        ParseNode atPos = db.root.locate(Util.X(pos),Util.Y(pos));
        Relation ret = new Relation(new String[]{"down"});
        if( atPos != null )
            for( ParseNode child : atPos.descendants() ) 
                ret.content.add(new Tuple(new Object[]{"["+child.from+","+child.to+")"}));
        return ret;      
    }
}
