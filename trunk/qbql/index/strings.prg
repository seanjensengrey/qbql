/*
Cat ^ [source from] Hello 3;

Cat ^ [source] Hello World ^ [from] 3;

Cat ^ [] = [from  prefix  postfix   source].

[source from] Hello 3 /^ [from=f] /^ [source=s];

(Cat /^ [source=src]) ^ [src] Hello World ^ [from] 3
~
Cat ^ [source] Hello World ^ [from] 3
.
*/

Substr =  
(Cat /^ [from=to] /^ [prefix=s])
/^
(Cat /^ [source=s] /^ [postfix=fragment]) 
; 
/*
Substr;

(Substr ^ [source] Hello World ^ [from to] 1 3) v [fragment];

(Substr ^ [source] Hello World ^ [fragment] o l) v [from fragment];
*/

Substr1 = (Substr /^ [source=src1] /^ [from=from1]) v [src1 from1 fragment];
Substr1;
Substr2 = (Substr /^ [source=src2] /^ [from=from2]) v [src2 from2 fragment];
Substr2;
(Substr1 ^ [src1] Hello) ^ (Substr2 ^ [src2] World);