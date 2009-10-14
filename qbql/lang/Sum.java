package qbql.lang;

import qbql.index.NamedTuple;
import qbql.lattice.Relation;
import qbql.lattice.Tuple;

public class Sum {
    public static NamedTuple summands_result( 
            Relation summands 
    ) {
        int ret = 0;
        for( Tuple t: summands.content ) {
            ret += (Integer)t.data[0];
        }
        String[] columns = new String[]{"result"};
        Object[] data = new Object[]{ret};
        return new NamedTuple(columns,data);
    }
}
