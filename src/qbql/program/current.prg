include Figure1.db;
include udf.def;

/*
E=[p]
   2
; 

x = [t] (A) (B) (C) (D) (E);
lex = ((x /^ [t=lft]) ^ (x /^ [t=rgt])) ^ LE;
--lex;
--(x /^ [t=lft]) /=  lex;

--lex = ((x /^ [t=lft]) ^ (x /^ [t=rgt])) ^ LE;
--lex;
GE = LE' v [lft=rgt];
gex = ((x /^ [t=lft]) ^ (x /^ [t=rgt])) ^ GE;
(x /^ [t=rgt]) /=  gex; -- or /<
--plux = ((x /^ [t=x]) ^ (x /^ [t=y])) ^ Plus;
--plux;
--(plux /^ [t=x]) /= x;
*/

/*
(<NOT>x) ^ x = x ^ R00.
(<NOT>x) v x = x v R11. 


(<INV>x) ^ x = x ^ R11.
(<INV>x) v x = x v R00. 

["------INV AXIOMS -----"];

(<INV>x) v (<INV>y) = <INV>(x <OR> y).
(<NOT>x) ^ (<NOT>y) = <NOT>(x <OR> y).
(<INV>x) <OR> (<INV>y) = <INV>(x v y).

["------De Morgan -----"];


x ^ ((<INV>y) v (<INV>z)) = (x ^ (<INV>y)) v (x ^ (<INV>z)).

["------Distributivity -----"];
*/

--x /= y = (x /< y) ^ (x /> y).
x /= y = <NOT>( ((y ^ <NOT>x) v (x ^ <NOT>y)) <and> <INV>(x v y) ).

A ^ B;
A <and> B;