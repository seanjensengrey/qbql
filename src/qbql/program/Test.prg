include "C:/eclipse/qbql_trunk/src/qbql/program/Figure1.db";
include udf.def;

R00 ^ (x v y v z) = R00 &
(x /^ (y ^ z)) ^ R00 = ((x /^ y) ^ (x /^ z)) ^ R00 ->
x /^ (y ^ z) = (x /^ y) ^ (x /^ z).

["------CONDITIONAL DISTRIBUTIVITY -----"];

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



Cat ^ [source from] Hello 3 =[from  postfix  prefix  source]
                        3  lo  Hel  Hello
.
Cat ^ [source] Hello World ^ [from] 3=[from  postfix  prefix  source]
                                3  ld  Wor  World
                                3  lo  Hel  Hello
.
Cat ^ [] = [from  prefix  postfix   source].

[source from] Hello 3 /^ "from=f" /^ "source=s"=[f  s]
                                          3  Hello
.
(Cat /^ "source=src") ^ [src] Hello World ^ [from] 3
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

Substr1 = (Substr /^ "source=src1" /^ "from=from1") v [src1 from1 fragment];
Substr2 = (Substr /^ "source=src2" /^ "from=from2") v [src2 from2 fragment];
Substr1^[]=[src1 from1 fragment].
Substr2^[]=[src2 from2 fragment].

((Substr1 ^ [src1] Hello) ^ (Substr2 ^ [src2] World) ^ ([fragment]"")') 
v [from1 from2 fragment]=[fragment  from1  from2]
                          l  2  3
                          l  3  3
                          o  4  1
.


HelloFgmts = ("from=from1" /^ Substr /^ [source] Hello) v [from1 fragment];
WorldFgmts = ([source] World /^ Substr /^ "from=from2") v [from2 fragment];
HelloFgmts ^ WorldFgmts ^ ([fragment]"")'=[fragment  from1  from2]
                                       l  2  3
                                       l  3  3
                                       o  4  1
.

SubstrSrc = (Substr /^ [source] "Hello World" /^ [fragment]o) v [prefix postfix];
(Substr /^ (SubstrSrc /^ [fragment]"***")) v [source] = [source]
                                                 "Hell*** World"
                                                 "Hello W***rld"
.



["------ Predicates -----"]; 

Sum /= [summands] 1 2 3 4=[result]
                     10
.
 
["------ Aggregates -----"]; 

Points = [x y]
          0 1
          0 3
          2 2
          2 5
          3 1
          3 3
          4 2
          5 0
          6 3
;

Points ^ "x <= y" =[x  y]
                 0  1
                 0  3
                 2  2
                 2  5
                 3  3
. 
Points ^ "x <= 2"=[x  y]
                 0  1
                 0  3
                 2  2
                 2  5
.

["------ Generic Predicates -----"]; 