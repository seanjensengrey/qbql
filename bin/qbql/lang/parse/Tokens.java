package qbql.lang.parse;

import qbql.index.NamedTuple;
import qbql.lattice.Database;
import qbql.lattice.Relation;
import qbql.lattice.Tuple;
import qbql.parser.LexerToken;
import qbql.parser.ParseNode;
import qbql.util.Util;

public class Tokens {

    // Legend: pos,up,down are intervals [from,to)
    // Vars ^ [] = [name pos].  
    // Tokens ^ [] = [txt pos].
    // Links ^ [] = [up down].
    // Paths ^ [] = [up down].
    ParseDb db = null;
    public Tokens( Database d ) {
        db = (ParseDb)d;       
    }
    
    public NamedTuple pos_txt( String pos ) throws AssertionError {
        int x = Util.X(pos);
        int y = Util.Y(pos);
        if( x+1 != y )
            throw new AssertionError("[Util.X(pos),Util.Y(pos))=["+x+","+y+")");
        return new NamedTuple(new String[]{"txt"},new Object[]{db.src.get(x).content});      
    }
    public Relation txt_pos( String txt ) {
        Relation ret = new Relation(new String[]{"pos"});
        int pos = -1;
        for( LexerToken t : db.src ) {
            pos++;
            if( txt.equals(t.content) )        
                ret.addTuple(new Object[]{"["+pos+","+(pos+1)+")"});
        }
        return ret;      
    }
}
