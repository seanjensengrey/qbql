include "C:/eclipse/qbql_trunk/src/qbql/program/Figure1.db";
include udf.def;

Cat ^ [source from] Hello 3 =[from  postfix  prefix  source]
                        3  lo  Hel  Hello
.
Cat ^ [source] Hello World ^ [from] 3=[from  postfix  prefix  source]
                                3  ld  Wor  World
                                3  lo  Hel  Hello
.
Cat ^ [] = [from  prefix  postfix   source].

[source from] Hello 3 /^ [from=f] /^ [source=s]=[f  s]
                                          3  Hello
.
(Cat /^ [source=src]) ^ [src] Hello World ^ [from] 3
~
Cat ^ [source] Hello World ^ [from] 3
.
(Substr ^ [source] Hello World ^ [from to] 1 3) v [fragment]=[fragment]
                                                   el
                                                   or
.
(Substr ^ [source] Hello World ^ [fragment] "o" l) v [from fragment]=[fragment  from]
                                                           l  2
                                                           l  3
                                                           o  1
                                                           o  4
.
["------ Predicates -----"]; 

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

