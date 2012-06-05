-- 2 different styles of include
include "c:/qbql_trunk/src/qbql/program/Figure1.db"; 
include udf.def;  
--include volume.db;


-- wrong "i in [1,...,20)" ^ "3 * x = i" ^ "5 * x = i" /^ "int x";

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
/*
X=[p  q  r]
   0  a  0
   0  a  1
   1  c  0
   1  c  1
   2  a  0
;
X#R00;
X#[p];
X#[q];
X#[r];
X#[p q];
X#[q r];
X#[r p];
X#R10;

X#[p] /^ X#[p];
X#[p] /^ X#[q];
X#[p] /^ X#[r];
X#[p] /^ X#[p q];
X#[p] /^ X#[q r];
X#[p] /^ X#[r p];
X#[p] /^ X#R00;
X#[p] /^ X#R10;
*/
/*x /< y = <NOT>( <INV>(x v y) <and> (x ^ <NOT>y) ).

x <mult> y = x ^ y.
x <plus> y = x <OR> y.

-- semiring
x <plus> y = y <plus> x.
x <plus> (y <plus> z) = (x <plus> y) <plus> z.
x <plus> R00 = x.

x <mult> R01 = x.
x <mult> (y <mult> z) = (x <mult> y) <mult> z.
x <mult> (y <plus> z) = (x <mult> y) <plus> (x <mult> z).

x <mult> R00 = R00.

x ^ (y v z) = (x ^ (z v (R00 ^ y))) v (x ^ (y v (R00 ^ z))).
(x ^ y) v (x ^ z) =  x ^ ( ((x v z) ^ y) v ((x v y) ^ z) ).
(R00 ^ (x ^ (y v z))) v (y ^ z) = ((R00 ^ (x ^ y)) v z) ^ ((R00 ^ (x ^ z)) v y).
*/

/*
M_PQ=[p  q] v MaierP137;
M_PR=[p  r] v MaierP137;
M_PQjM_PR = M_PQ ^ M_PR;
M_RQ=[r  q] v MaierP137;
M_PQjM_RQ = M_PQ ^ M_RQ;
M_PRjM_RQ = M_PR ^ M_RQ;

MaierP137 = M_PQ ^ M_PRjM_RQ.
MaierP137 = M_PR ^ M_PQjM_RQ.
MaierP137 = 
(M_PQ v M_PQjM_RQ) ^ 
(M_PQ v M_PR) ^
(M_PRjM_RQ v M_PR) ^
(M_PRjM_RQ v M_PQjM_RQ). 
M_PQ v M_PQjM_RQ = M_PQ.
M_PQ v M_PR;
M_PRjM_RQ v M_PR = M_PR.
PQR2 = M_PRjM_RQ v M_PQjM_RQ;
--Q v [p  q];
--Q v [p  r];
PQR2_PQ = PQR2 v [p  q];
PQR2_PR = PQR2 v [p  r];
M_RQ = PQR2 v [r  q].
PQR2 = PQR2_PQ ^ M_RQ.
PQR2 = M_RQ ^ PQR2_PR.
--PQR2 = PQR2_PQ ^ PQR2_PR.
PQR2_PQ v M_RQ;
PQR2_PQ v PQR2_PR;
M_RQ v M_RQ = M_RQ.
M_RQ v PQR2_PR;
*/

/*
((s ^ t) ^ s) v (((s ^ t) v s) ^ R00)*  = (((R00 ^ s) v t) ^ s). -- thrm

x < x*.

x < y -> x* < y*.

x ^ x* = x.

(x*)*=x*.

x ^ y = x -> x* ^ y* =x*.

(x v y) ^ R00 = R00 
-> (x ^ y)* = x* ^ y*.

x ^ R00=x* ^ R00.
R00* = R00.


(((R00 ^ s) v t) ^ s) = s* & 
(((R00 ^ t) v s) ^ t) = t* & 
(((R00 ^ u) v w) ^ u) = u* & 
(((R00 ^ w) v u) ^ w) = w* & 
s ^ t = u ^ w ->
s ^ t = 
(s v w) ^ 
(s v u) ^
(t v u) ^
(t v w). 


(s <and> (s^t))* = s* & 
(t <and> (s^t))* = t* & 
(u <and> (s^t))* = u* & 
(w <and> (s^t))* = w* & 
s ^ t = u ^ w &
s ^ t = 
(s v w) ^ 
(s v u) ^
(t v u) ^
(t v w) 
-> s = u | s = w | t = u | t = w | (s v t)^R00 = R00
| (u v w)^R00 = R00 | s^t=(s^t)* | s=s* | t=t* | u=u* | w=w*
.
*/

/*
(x+)+ = x+.
x+ < x.
x < y -> x+ < y+.

x+ ^ y+ = (x ^ y)+. 

([s]a b)+;         
*/

/*
y < x -> (r ^ y) < x.
(r ^ y) < x -> (r ^ y ^ z) < x ^ z.
(r ^ y) < x & (r ^ z) < y -> (r ^ z) < x.
*/


<NOT>(y)^x < y <-> <NOT>(y)^x < y^x.

<INV>(x) ^ x < y <-> <NOT>(y)^x < y^x.

