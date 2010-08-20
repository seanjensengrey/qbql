/*
Sets /! Sets1;
Sets /0 Sets1;
Sets /1 Sets1;
Sets /= Sets1;
Sets /> Sets1;
Sets /< Sets1;
Sets /^ Sets1;
*/
Sets /! Sets1=[_s  s1]
.
Sets /0 Sets1=[_s  s1]
             "{a,b}"  "{c}"
             "{a}"  "{b,c}"
             "{a}"  "{b}"
             "{a}"  "{c}"
             "{b,c}"  "{a}"
             "{b}"  "{a}"
             "{b}"  "{c}"
             "{c}"  "{a,b}"
             "{c}"  "{a}"
             "{c}"  "{b}"
             "{}"  "{a,b,c}"
             "{}"  "{a,b}"
             "{}"  "{a}"
             "{}"  "{b,c}"
             "{}"  "{b}"
             "{}"  "{c}"
.
Sets /1 Sets1=[_s  s1]
             "{a,b,c}"  "{a}"
             "{a,b,c}"  "{b}"
             "{a,b,c}"  "{c}"
             "{a,b}"  "{a}"
             "{a,b}"  "{b,c}"
             "{a,b}"  "{b}"
             "{a}"  "{a,b,c}"
             "{a}"  "{a,b}"
             "{a}"  "{a}"
             "{b,c}"  "{a,b}"
             "{b,c}"  "{b}"
             "{b,c}"  "{c}"
             "{b}"  "{a,b,c}"
             "{b}"  "{a,b}"
             "{b}"  "{b,c}"
             "{b}"  "{b}"
             "{c}"  "{a,b,c}"
             "{c}"  "{b,c}"
             "{c}"  "{c}"
.
Sets /= Sets1=[_s  s1]
             "{a,b,c}"  "{a,b,c}"
             "{a,b}"  "{a,b}"
             "{a}"  "{a}"
             "{b,c}"  "{b,c}"
             "{b}"  "{b}"
             "{c}"  "{c}"
.
Sets /> Sets1=[_s  s1]
             "{a,b,c}"  "{a,b,c}"
             "{a,b,c}"  "{a,b}"
             "{a,b,c}"  "{a}"
             "{a,b,c}"  "{b,c}"
             "{a,b,c}"  "{b}"
             "{a,b,c}"  "{c}"
             "{a,b}"  "{a,b}"
             "{a,b}"  "{a}"
             "{a,b}"  "{b}"
             "{a}"  "{a}"
             "{b,c}"  "{b,c}"
             "{b,c}"  "{b}"
             "{b,c}"  "{c}"
             "{b}"  "{b}"
             "{c}"  "{c}"
.
Sets /< Sets1=[_s  s1]
             "{a,b,c}"  "{a,b,c}"
             "{a,b}"  "{a,b,c}"
             "{a,b}"  "{a,b}"
             "{a}"  "{a,b,c}"
             "{a}"  "{a,b}"
             "{a}"  "{a}"
             "{b,c}"  "{a,b,c}"
             "{b,c}"  "{b,c}"
             "{b}"  "{a,b,c}"
             "{b}"  "{a,b}"
             "{b}"  "{b,c}"
             "{b}"  "{b}"
             "{c}"  "{a,b,c}"
             "{c}"  "{b,c}"
             "{c}"  "{c}"
             "{}"  "{a,b,c}"
             "{}"  "{a,b}"
             "{}"  "{a}"
             "{}"  "{b,c}"
             "{}"  "{b}"
             "{}"  "{c}"
.
Sets /^ Sets1=[_s  s1]
             "{a,b,c}"  "{a,b,c}"
             "{a,b,c}"  "{a,b}"
             "{a,b,c}"  "{a}"
             "{a,b,c}"  "{b,c}"
             "{a,b,c}"  "{b}"
             "{a,b,c}"  "{c}"
             "{a,b}"  "{a,b,c}"
             "{a,b}"  "{a,b}"
             "{a,b}"  "{a}"
             "{a,b}"  "{b,c}"
             "{a,b}"  "{b}"
             "{a}"  "{a,b,c}"
             "{a}"  "{a,b}"
             "{a}"  "{a}"
             "{b,c}"  "{a,b,c}"
             "{b,c}"  "{a,b}"
             "{b,c}"  "{b,c}"
             "{b,c}"  "{b}"
             "{b,c}"  "{c}"
             "{b}"  "{a,b,c}"
             "{b}"  "{a,b}"
             "{b}"  "{b,c}"
             "{b}"  "{b}"
             "{c}"  "{a,b,c}"
             "{c}"  "{b,c}"
             "{c}"  "{c}"
.


