package qbql.induction;

public class TreeNode {
    TreeNode lft;
    TreeNode rgt;
    public TreeNode( TreeNode lft, TreeNode rgt ) {
        this.lft = lft;
        this.rgt = rgt;
    }   
    
    void print() {
        System.out.println(toString()); // (authorized)
    }
    String toString( int depth ) {      
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < depth ;i++)
            sb.append("  "); 
        sb.append("*\n");
        if( lft != null )
            sb.append(lft.toString(depth+1));
        if( rgt != null )
            sb.append(rgt.toString(depth+1));
        return sb.toString();
    }
    public String toString() {
        if( this == zero )
            return "<0>";
        if( this == one )
            return "<1>";
        if( this == two )
            return "<2>";
        return toString(0);
    }
    
    final private static TreeNode zero = new TreeNode(null,null);
    final public static TreeNode one = new TreeNode(zero,null);
    final public static TreeNode two = new TreeNode(zero,zero);
    TreeNode incr() {
        if( this == two ) return null;
        else if( this == one ) return two;
        else return one;
    }
    TreeNode zero() {
        return zero;
    }
}
