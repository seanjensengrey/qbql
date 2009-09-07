package qbql.index;

import qbql.lattice.Tuple;

public class NamedTuple extends Tuple {
    String[] columns;
           
    public NamedTuple( String[] columns, Object[] data ) {
        super(data);
        assert(data.length==columns.length);
        this.columns = columns;
    }
    
    public Object get( String col ) throws Exception {
        for( int i = 0; i < columns.length; i++ ) {
            if( col.equals(columns[i]) )
                return data[i];
        }
        throw new Exception("Mismatching column?");
    }

}
