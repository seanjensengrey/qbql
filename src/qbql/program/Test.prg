include "C:/eclipse/qbql_trunk/src/qbql/program/Figure1.db";
include udf.def;

(<NOT>x) ^ x = x ^ R00.
(<NOT>x) v x = x v R11. 


(<INV>x) ^ x = x ^ R11.
(<INV>x) v x = x v R00. 

<NOT>R00= <INV>R11.

["------INV AXIOMS -----"];

(<INV>x) v (<INV>y) = <INV>(x <OR> y).
(<NOT>x) ^ (<NOT>y) = <NOT>(x <OR> y).
(<INV>x) <OR> (<INV>y) = <INV>(x v y).

["------De Morgan -----"];


x ^ ((<INV>y) v (<INV>z)) = (x ^ (<INV>y)) v (x ^ (<INV>z)).

["------Distributivity -----"];

