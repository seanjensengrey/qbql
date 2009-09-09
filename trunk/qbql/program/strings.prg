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

/*
Substr;

(Substr ^ [source] Hello World ^ [from to] 1 3) v [fragment];

(Substr ^ [source] Hello World ^ [fragment] "o" l) v [from fragment];
*/
/*
Substr1 = (Substr /^ [source=src1] /^ [from=from1]) v [src1 from1 fragment];
Substr2 = (Substr /^ [source=src2] /^ [from=from2]) v [src2 from2 fragment];
((Substr1 ^ [src1] Hello) ^ (Substr2 ^ [src2] World) ^ ([fragment]"")') 
v [from1 from2 fragment];
*/
Substr1 = ((Substr /^ [source] Hello) /^ [from=from1]) v [from1 fragment];
Substr2 = ((Substr /^ [source] World) /^ [from=from2]) v [from2 fragment];
Substr1 ^ Substr2 ^ ([fragment]"")';