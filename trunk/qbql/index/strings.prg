/*
Cat ^ [source from] Hello 3;

Cat ^ [source] Hello World ^ [from] 3;

Cat ^ [] = [from  prefix  postfix   source].

[source from] Hello 3 \|/ [from=f] \|/ [source=s];

(Cat \|/ [source=src]) ^ [src] Hello World ^ [from] 3
~
Cat ^ [source] Hello World ^ [from] 3
.
*/

Substr = (Cat \|/ [postfix=src2] ) ^ 
(Cat \|/ [source=src2] \|/ [from=to] \|/ [prefix=fragment]); 

(Substr ^ [source] Hello World ^ [from to] 1 3) v [fragment];
