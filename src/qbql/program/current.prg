include "C:/eclipse/qbql_trunk/src/qbql/program/Figure1.db";
include udf.def;

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
/* unit test */
AtOneSide 
/^ ([x1 y1] 0 1
 ^  [x2 y2] 2 2
 ^  [xa ya] 0 3
 ^  [xb yb] 3 1) = R00.
AtOneSide 
/^ ([x1 y1] 0 1
 ^  [x2 y2] 2 2
 ^  [xa ya] 0 3
 ^  [xb yb] 3 3) = R01.
 
Exp /^ [y] 1 "2.7";

Mult /^ [f1 f2] 3 2;

Mult /^ [f1 p] 3 6;


R = (
     (Points /^ [x=x1] /^ [y=y1]) 
   ^ (Points /^ [x=x2] /^ [y=y2]) 
   ^ (Points /^ [x=xa] /^ [y=ya]) 
   ^ (Points /^ [x=xb] /^ [y=yb]) 
 ^ AtOneSide 
) /= (
     (Points /^ [x=xa] /^ [y=ya]) 
   ^ (Points /^ [x=xb] /^ [y=yb])
)^ ([x1=x2] ^ [y1=y2])';

R v [x1 y1];
         