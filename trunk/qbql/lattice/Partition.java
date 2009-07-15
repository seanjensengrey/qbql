package qbql.lattice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import qbql.util.Util;

public class Partition {
    private Set<Block> blocks = new TreeSet<Block>();
    
    
    public static Partition intersect( Partition x, Partition y ) {
        Partition ret = new Partition();
        for( Block bx : x.blocks )
            for( Block by : y.blocks ) {
                Block intersect = Block.intersect(bx,by);
                if( intersect.content.size() > 0 )
                    ret.blocks.add(intersect);
            }
        return ret;
    }
    public static Partition union( Partition x, Partition y ) {
        Partition ret = new Partition();
        Set<Block> allBlocks = new TreeSet<Block>();
        allBlocks.addAll(x.blocks);
        allBlocks.addAll(y.blocks);        
        while( allBlocks.size() > 0 ) {
            Block ab = null;
            for( Block b : allBlocks ) {
                ab = b;
                break;
            }
            allBlocks.remove(ab);
            
            Block intersectsAB = null;
            for( Block b : ret.blocks )         
                if( !Block.isDisjoint(ab, b) ) {
                    intersectsAB = b;
                }
            if( intersectsAB != null ) {
                ret.blocks.remove(intersectsAB);
                ret.blocks.add(Block.union(intersectsAB, ab));
            } else                
                ret.blocks.add(ab);
        }
        return ret;
    }

    public static Partition construct( int[] indexes ) {
        Map<Integer,Block> numberedBlocks = new HashMap<Integer,Block>();
        for( int i = 0; i < indexes.length; i++ ) {
            Integer partNo = indexes[i];
            Block block = numberedBlocks.get(partNo);
            if( block == null ) {
                block = new Block();
                numberedBlocks.put(partNo, block);
            }
            block.add(i);
        } 
        Partition ret = new Partition();
        ret.blocks.addAll(numberedBlocks.values());
        return ret;
    }
    
    public String toString() {       
        StringBuilder ret = new StringBuilder();
        boolean firstIter = true;
        for( Block block : blocks ) {
            if( !firstIter )
                ret.append(" |");
            ret.append(block.toString());
            firstIter = false;
        }
        return ret.toString();
    }
    
    private static class Block implements Comparable<Block> {
        TreeSet<Integer> content = new TreeSet<Integer>();

        public int compareTo( Block b ) {
            return toString().compareTo(b.toString());
        }
        
        public static Block intersect( Block bx, Block by ) {
            Block ret = new Block();
            ret.content.addAll(bx.content);
            ret.content.retainAll(by.content);
            return ret;
        }

        public static boolean isDisjoint( Block bx, Block by ) {
            for( int i : bx.content )
                for( int j : by.content )
                    if( i == j )
                        return false;
            return true;
        }
        
        public static Block union( Block bx, Block by ) {
            Block ret = new Block();
            ret.content.addAll(bx.content);
            ret.content.addAll(by.content);
            return ret;
        }
        
        public void add( int i ) {
            content.add(i);
        }

        public String toString() {
            StringBuilder ret = new StringBuilder();
            for( int i : content )
                ret.append(" "+i);
            return ret.toString();
        }
    }
    
    
    public static void main( String[] args ) {
        Partition p = construct(new int[]{1,2,1,2,1,1,3,3,4});
        Partition q = construct(new int[]{3,4,3,4,5,5,4,2,2});
        System.out.println(q.toString());
        System.out.println(p.toString());
        System.out.println(intersect(p,q).toString());
        System.out.println(union(p,q).toString());
    }
    
}
