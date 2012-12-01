package qbql.deduction;

import qbql.induction.TreeNode;

public class Postulate extends TreeNode {

    public Postulate( TreeNode lft, TreeNode rgt ) {
        super( lft, "=", rgt );
    }

}
