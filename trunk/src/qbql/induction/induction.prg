/*
        //String goal = "(x ^ (y v z)) /< ((x ^ y) v (x ^ z)) = expr.";
        //String goal = "[] < x v y v z -> x /^ (y /^ z) = expr.";
        
        y <and> z = y <-> implication. 
        ***Found: 
        y <and> z = y <-> (((R00 ^ (z v y)) v (z ^ y)) = y).
        Elapsed=101427
        evalTime=24971
        y <and> z = y <-> (((R11 ^ z) v (R00 ^ y)) = (z v y)).
        Elapsed=137405
        evalTime=32051
        y <and> z = y <-> (((R11 ^ z) v y) = ((R00 ^ y) v z)).
        Elapsed=147369
        evalTime=33376

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

--(x /= y)  = expr.
--(x /< y)  = expr.
--(x /< y)  = <NOT>(((<NOT>(y) ^ x) v <INV>(((R00 ^ x) <"and"> y)))).
--(x /< y)  = <NOT>(((<NOT>(y) ^ x) v <INV>((R00 ^ (y v x))))). 
--(x /< y)  = <NOT>(((<NOT>(y) ^ x) <and> <INV>((y v x)))).

/*
x <plus> y = expr. 
x <plus> R00 = x.
x <plus> y = y <plus> x.
x <plus> (y <plus> z) = (x <plus> y) <plus> z.
x <mult> (y <plus> z) = (x <mult> y) <plus> (x <mult> z).
*/

/*x <mult> y = expr. 
-- <mult> != ^.
x <mult> R11 = x.
--x <mult> R00 = R00.

x <mult> y = y <mult> x.
x <mult> (y <mult> z) = (x <mult> y) <mult> z.

x <mult> (y v z) = (x <mult> y) v (x <mult> z).
*/


((R00 ^ s) v t) ^ s = s* &
((R00 ^ t) v s) ^ t = t* <-> implication.