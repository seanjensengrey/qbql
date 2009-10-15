package qbql.parser;

import java.util.TreeMap;

import qbql.util.Util;

/**
 * Main Data Structure for CYK method
 */
public class Matrix extends TreeMap<Integer,int[]> {
    private CYK cyk;

    public Matrix( CYK cyk ) {
        this.cyk = cyk;
    }

    public boolean contains( int x, int y, int symbol ) {
        int[] js = get(Util.pair(x, y));
        if( js == null )
            return false;
        for( int ii : js )
            if( Util.Y(ii) == symbol )
                return true;
        return false;
    }

    public int getSymbol( int interval, int index ) {
        int[] tmp = get(interval);
        if( tmp == null )
            return -1;
        return Util.Y(tmp[index]);
    }

    public int getIntervalMiddle( int interval, int index ) {
        int[] tmp = get(interval);
        if( tmp == null )
            return -1;
        return Util.X(tmp[index]);
    }

    /*public void put( int interval, Set<Integer> value ) {
                if( value.size()>0 ) {
                        int[] tmp1 = new int[value.size()];
                        int i = 0;
                        for( int e : value )
                                tmp1[i++] = e;
                        put(interval,tmp1);
                } 
        }*/
    /*public void put( int interval, int[] value ) {
                impl.put(interval,value);
        }

        public SortedMap<Integer, int[]> subMap(Integer fromKey, Integer toKey) {
                return impl.subMap(fromKey, toKey);
        }

        public Set<Integer> keySet() {
                return impl.keySet();
        }

        public int[] remove(Object key) {
                return impl.remove(key);
        }

        public int size() {
                return impl.size();
        }

        public int[] get(Object key) {
                return impl.get(key);
        }*/

    /*  
        public void clear() {
                impl.clear();
        }

        public Comparator<? super Integer> comparator() {
                // TODO Auto-generated method stub
                return null;
        }

        public Integer firstKey() {
                // TODO Auto-generated method stub
                return null;
        }

        public SortedMap<Integer, int[]> headMap(Integer toKey) {
                // TODO Auto-generated method stub
                return null;
        }

        public Integer lastKey() {
                // TODO Auto-generated method stub
                return null;
        }


        public SortedMap<Integer, int[]> tailMap(Integer fromKey) {
                // TODO Auto-generated method stub
                return null;
        }

        public boolean containsKey(Object key) {
                // TODO Auto-generated method stub
                return false;
        }

        public boolean containsValue(Object value) {
                // TODO Auto-generated method stub
                return false;
        }

        public Set<java.util.Map.Entry<Integer, int[]>> entrySet() {
                // TODO Auto-generated method stub
                return null;
        }


        public boolean isEmpty() {
                // TODO Auto-generated method stub
                return false;
        }


        public int[] put(Integer key, int[] value) {
                // TODO Auto-generated method stub
                return null;
        }

        public void putAll(Map<? extends Integer, ? extends int[]> t) {
                // TODO Auto-generated method stub

        }

        public Collection<int[]> values() {
                // TODO Auto-generated method stub
                return null;
        }
     */

    public String toString() throws RuntimeException {
        StringBuffer ret = new StringBuffer();
        for( int xy : keySet() ) {
            int i = Util.X(xy);
            int j = Util.Y(xy);
            ret.append("["+i+","+j+")"); 
            int[] output = get(Util.pair(i, j));
            if( output ==  null ) {
                throw new RuntimeException("no value corresponding to the key ["+i+","+j+")"); 
            }
            int k = 0;
            for( int s : output ) {
                if( k < 5 )
                    ret.append("  "+cyk.allSymbols[Util.Y(s)]);
                else if( k == 5 )
                    ret.append(" ...");
                k++;
            }

            ret.append('\n'); 
        }
        return ret.toString();
    }
}