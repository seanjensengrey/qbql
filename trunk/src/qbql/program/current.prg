--include "C:/eclipse/qbql_trunk/src/qbql/program/Figure1.db";
--include udf.def;
--include volume.db;


-- wrong ("i in {1,...,1000}" ^ "3 * x = i" /^ "int x") 
--v ("i in {1,...,1000}" ^ "5 * x = i" /^ "int x");
/*
Colored = [name color]
           A    green
--missing info:  B    color=?
           C    blue
;

"Colored(name,color)" = Colored <OR> ([name] B); 

Next = [lft rgt]
            A     B
            B     A
            B     C
            C     B
;

--"Colored(name,color)";
--"Next(from,to)";

GreenBlocksAtTheLeft = Colored /^ "name=lft" /^ [color]green;
GreenBlocksAtTheLeft;

CB = Colored v ([name]B ^ ([color])');

(CB /^ "name=name1" /^ [color]green) 
/^ Next /^ 
(CB /^ "name=name2" /^ ([color]green)');
*/

--x = (x <OR> y) <and> ( x <OR> <NOT>(<INV>(y)) ).

-- false: x < y & u < v -> x /^ u < y /^ v.

--<NOT>(y) /< <NOT>(x) = x /< y.

--x <OR> (y ^ <NOT> x) = y <OR> (x ^ <NOT> y). --=<NOT>((<NOT>(y) ^ <NOT>(x))).
--x ^ <NOT> (y <OR> z) = (x ^ <NOT> y) ^ <NOT> z.
--x ^ (y ^ <NOT> z) = (x ^ y) ^<NOT> (x ^ z).
/*
[p q]
 1 a
 2 b
<OR>
[r q]
 1 a
 0 c
;
*/
Au=[i j s]
    1 1 3
    1 2 1
    2 1 1
    2 2 0
;
Ap=[i j1 j2]
    1 3  1
    2 1  0
;
Au =
((Ap /^ "j1=s") /^ [j]1)
v
((Ap /^ "j2=s") /^ [j]2)
. 
Ap =
((Au /^ "j1=s") /^ [j]1)
^
((Au /^ "j2=s") /^ [j]2)
.

