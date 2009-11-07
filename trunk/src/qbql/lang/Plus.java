package qbql.lang;

import qbql.index.NamedTuple;

public class Plus {

    public static NamedTuple x_y_z( 
            int x, int y 
    ) {
        String[] columns = new String[]{"z"};
        Object[] data = new Object[]{x+y};
        return new NamedTuple(columns,data);
    }
    public static NamedTuple x_z_y( 
            int x, int z 
    ) {
        String[] columns = new String[]{"y"};
        Object[] data = new Object[]{z-x};
        return new NamedTuple(columns,data);
    }
    public static NamedTuple y_z_x( 
            int y, int z 
    ) {
        String[] columns = new String[]{"x"};
        Object[] data = new Object[]{z-y};
        return new NamedTuple(columns,data);
    }
}
