package qbql.lang.parse;

import qbql.index.NamedTuple;
import qbql.lattice.Database;
import qbql.lattice.Relation;
import qbql.lattice.Tuple;
import qbql.parser.ParseNode;
import qbql.util.Util;

public class Links {

    // Legend: pos,up,down are intervals [from,to)
    // Vars ^ [] = [name pos].  
    // Tokens ^ [] = [txt pos].
    // Links ^ [] = [up down].
    // Paths ^ [] = [up down].
    ParseDb db = null;
    public Links( Database d ) {
        db = (ParseDb)d;       
    }
    
    public NamedTuple down_up( String pos ) throws AssertionError {
        ParseNode prt = db.root.parent(Util.X(pos),Util.Y(pos));
        return new NamedTuple(new String[]{"up"},new Object[]{"["+prt.from+","+prt.to+")"});      
    }
    public Relation up_down( String pos ) {
        ParseNode atPos = db.root.locate(Util.X(pos),Util.Y(pos));
        Relation ret = new Relation(new String[]{"down"});
        for( ParseNode child : atPos.children() ) {
           ret.content.add(new Tuple(new Object[]{"["+child.from+","+child.to+")"}));
        }
        return ret;      
    }
}
