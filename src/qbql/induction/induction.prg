/*x@@y = expr. 
x@@x = x. 
@@ != v. @@ != ^. @@ != @^. @@ != @v. @@ != @^v. @@ != @'`.
x@@y = y@@x. 
--x @@ (x @^v y) = x.
--x @^v (x @@ y) = x.
x @@ (y @@ z) = (x @@ y) @@ z.
--x v (y @@ z) = (x v y) @@ (x v z). 
*/

 
x @< y = expr.
x @< x = R01.
x @< y = R01 & y @< x = R01 -> y=x.
x @< y = R01 & y @< z = R01 -> x @< z = R01.
--x /^ y < z <-> x < y /= z.
