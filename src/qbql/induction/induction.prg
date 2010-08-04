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
x @< x = R00.
x @< y = R00 & y @< x = R00 -> y=x.
x @< y = R00 & y @< z = R00 -> x @< z = R00.
(x /^ y) @< z = R00 <-> x @< (y /< z) = R00.


