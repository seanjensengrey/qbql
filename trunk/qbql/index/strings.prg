
Cat ^ [source from] Hello 3;

Cat ^ [source] Hello World ^ [from] 3;

Cat ^ [] = [from  prefix  postfix   source].

[source from] Hello 3 \|/ [from=f] \|/ [source=s];

(Cat \|/ [source=src]) ^ [src] Hello World ^ [from] 3
~
Cat ^ [source] Hello World ^ [from] 3
.


/* wrong! want [from,to), while getting [from,from+to)!
Substr = (Cat \|/ [postfix=src2] ) ^ 
(Cat \|/ [source=src2] \|/ [from=to] \|/ [prefix=fragment]); 
*/
Substr =  
(Cat \|/ [from=to] \|/ [prefix=src2])
\|/
(Cat \|/ [source=src2] \|/ [postfix=fragment]) 
; 

Substr;

(Substr ^ [source] Hello World ^ [from to] 1 3) v [fragment];

(Substr ^ [source] Hello World ^ [fragment] o l ^ [from to] 1 2 2 3 3 4 4 5 ) v [from fragment];