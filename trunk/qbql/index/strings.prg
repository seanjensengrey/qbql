Cat ^ [source from] Hello 3;
-- Cat ^ [source] Hello World ^ [from] 3;
/*
cat ^ R00 = [source from prefix postfix].

substr = (cat(source,from,prefix,postfix) ^ cat(postfix,to,fragment,dummy)) v [source from to fragment];

(substr ^ [source] Hello World ^ [from to] 1 3) v [selection];

(substr ^ [source] Hello World ^ [selection] o) v [from];
*/