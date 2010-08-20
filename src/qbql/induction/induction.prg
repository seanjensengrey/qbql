/*x@@y = expr. 
x@@x = x. 
@@ != v. @@ != ^. @@ != @^. @@ != @v. @@ != @^v. @@ != @'`.
x@@y = y@@x. 
--x @@ (x @^v y) = x.
--x @^v (x @@ y) = x.
x @@ (y @@ z) = (x @@ y) @@ z.
--x v (y @@ z) = (x v y) @@ (x v z). 
*/

 
x @@ y = expr.
x@@y = y@@x. 
x /< (z @@ y) = (x /< z) @@ (x /< y).
