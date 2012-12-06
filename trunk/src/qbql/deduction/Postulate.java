package qbql.deduction;

import qbql.induction.TreeNode;

public class Postulate extends TreeNode {

    public Postulate( TreeNode lft, TreeNode rgt ) {
        super( lft, "=", rgt );
    }

    public Postulate substitute( String x, TreeNode treeNode ) {
        return new Postulate(getLft().substitute(x, treeNode),getRgt().substitute(x, treeNode));
    }

}
