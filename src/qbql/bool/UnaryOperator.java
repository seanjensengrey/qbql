package qbql.bool;

// up to 64 bit only
public class UnaryOperator {
    long[] map;
    int dimension;
    
    UnaryOperator( int dimension ) {
        this.dimension = dimension;
        int size = 1 << dimension;
        map = new long[size];
    }
    
    UnaryOperator next() {
        for( int i = 0; i < map.length; i++ ) {
            if( map[i] < map.length-1 ) {
                map[i]++;
                for( int j = 0; j < i; j++ ) {
                    map[j] = 0L;
                }
                return this;
            }
        }
        return null; // exhausted all operator possibilities
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        for( int i = 0; i < map.length; i++) {
            
            for( int j = 0; j < dimension; j++ ) {
                long bit = 1L << j;
                if( bit == (bit & i) )
                    ret.append('1');
                else
                    ret.append('0');
            }
            ret.append("->");
            for( int j = 0; j < dimension; j++ ) {
                long bit = 1L << j;
                if( bit == (bit & map[i]) )
                    ret.append('1');
                else
                    ret.append('0');
            }
            ret.append('\n');
        }
        return ret.toString();
    }
    
    
} 
