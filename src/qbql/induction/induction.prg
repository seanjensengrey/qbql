/*x@@y = expr. 
x@@x = x. 
@@ != v. @@ != ^. @@ != @^. @@ != @v. @@ != @^v. @@ != @'`.
x@@y = y@@x. 
--x @@ (x @^v y) = x.
--x @^v (x @@ y) = x.
x @@ (y @@ z) = (x @@ y) @@ z.
--x v (y @@ z) = (x v y) @@ (x v z). 
*/

/* 
x @@ y = expr.
x@@y = y@@x. 
x /< (z @@ y) = (x /< z) @@ (x /< y).
*/
/*
        //String goal = "(x ^ (y v z)) /< ((x ^ y) v (x ^ z)) = expr.";
        //String goal = "[] < x v y v z -> x /^ (y /^ z) = expr.";
        //String goal = "y + z = y <-> implication."; // Found: y * z = y <-> (((R11 ^ z) v (R00 ^ y)) = (z v y)).

        //String goal = "(x @^ y) @v (x @^ z) = expr.";
        //String goal = "x /< y = expr.";
        //String goal = "r#x < r#y <-> implication.";
        //String goal = "x = y <-> R00 = expr.";
*/


--(x ^ y) v (x ^ <NOT>(<INV>y)) = expr.

--x <op> y = expr. 
--(x <OR> y) < z <-> x < (y <op> z).
--(x <OR> y) /< z = R01 <-> x /< (y <op> z) = R01.

--x ^ (y ^ <NOT> x) = expr.

(x /= y)  = expr.