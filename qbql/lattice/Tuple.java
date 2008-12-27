package qbql.lattice;

class Tuple implements Comparable {
    String[] data;

    public Tuple( String[] data ) {
        super();
        this.data = data;
    }

    // careful: this works in the scope of one relation only, 
    // not between different relations
    // see the overloaded equals( Object obj, Relation x, Relation y )
    public boolean equals( Object obj ) {
        Tuple src = (Tuple) obj;
        if( src.data.length != data.length )
            return false;
        for( int i = 0; i < data.length; i++ )
            if( !src.data[i].equals(data[i]) )
                return false;
        return true;
    }
    /**
     * @param obj
     * @param x -- need to map named perspective into positional
     * @param y -- the order of x and y doesn't matter
     * @return
     */
    public boolean equals( Object obj, Relation x, Relation y ) {
        Tuple src = (Tuple) obj;
        if( src.data.length != data.length )
            return false;
        for( int i = 0; i < data.length; i++ ) {
            String colName = x.colNames[i];
            int j = y.header.get(colName);
            if( !src.data[i].equals(data[j]) )
                return false;
        }
        return true;
    }

    public int hashCode() {
        if( data.length == 0 )
            return 0;
        else
            return data[0].hashCode();
    }

    public int compareTo( Object o ) {
        Tuple src = (Tuple) o;
        return toString().compareTo(src.toString());
    }

    public String toString() {
        StringBuffer ret = new StringBuffer("<");
        for( int i = 0; i < data.length; i++ )
            ret.append((i==0?"":",")+data[i]);
        ret.append(">");
        return ret.toString();
    }


}
