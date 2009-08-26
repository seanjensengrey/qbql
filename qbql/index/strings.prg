/*
Cat ^ [source from] Hello 3;

Cat ^ [source] Hello World ^ [from] 3;
*/
Cat ^ [] = [from  postfix  prefix  source].

[source from] Hello 3 \|/ [from=f] \|/ [source=s];

--Cat(f, po, pr, s) ^ [s f] Hello 3;

--Substr = (Cat ^ Cat(postfix,to,fragment,dummy)) v [source from to fragment];

/*
(substr ^ [source] Hello World ^ [from to] 1 3) v [selection];

(substr ^ [source] Hello World ^ [selection] o) v [from];
*/