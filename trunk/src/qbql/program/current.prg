-- 2 different styles of include
include "D:/qbql_trunk1/src/qbql/program/Figure1.db"; 
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

x < x*.

x**=x*.

x < y -> x* < y*.

-- invalid: (<NOT>x)* = <NOT>(x*).
-- invalid: (x ^ y)* = x* ^ y*.

(x v y)* = x* v y*.




/* Alice p.171 */
SDT = [Snack   Distr   Price Theater]
       coffee  Smart   235   Rex
       coffee  Smart   235   LeChampo
       coffee  Smart   235   Cinoche
       coffee  Leclerc 260   Cinoche
       wine    Smart   80    Rex
       wine    Smart   80    Cinoche
       popcorn Leclerc 560   Cinoche
;


sdp = SDT v [Snack Distr Price];
dt  = SDT v [Distr Theater];
st  = SDT v [Snack Theater];

SDT = sdp ^ dt ^ st.
x = SDT;

s = sdp ^ dt;
t = st; 
u = sdp;
w = dt ^ st;

s;
t;
s ^ t;
s ^ (s ^ t)';
t ^ R11 < s ^ t ^ R11. 
u ^ R11 < s ^ t ^ R11. 
w ^ R11 < s ^ t ^ R11.


x = 
(s v w) ^ 
(s v u) ^
(t v u) ^
(t v w).

s = a ^ c;
t = b; 
u = a;
w = b ^ c;

x = 
(s v w) ^ 
(s v u) ^
(t v u) ^
(t v w).


s;
x <and> s;
x <and> s = s.
x <and> t = t.
x <and> u = u.
x <and> w = w.
x = s ^ t.
x = u ^ w.
/**/

s ^ R11 < s ^ t ^ R11 & 
t ^ R11 < s ^ t ^ R11 & 
u ^ R11 < s ^ t ^ R11 & 
w ^ R11 < s ^ t ^ R11 & 
s ^ t = u ^ w ->
s ^ t = 
(s v w) ^ 
(s v u) ^
(t v u) ^
(t v w) 
.




