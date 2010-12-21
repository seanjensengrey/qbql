include "C:/eclipse/qbql_trunk/src/qbql/program/Figure1.db";
include udf.def;
--include volume.db;


-- wrong ("i in {1,...,1000}" ^ "3 * x = i" /^ "int x") 
--v ("i in {1,...,1000}" ^ "5 * x = i" /^ "int x");
/*
Colored = [name color]
           A    green
--missing info:  B    color=?
           C    blue
;

"Colored(name,color)" = Colored <OR> ([name] B); 

Next = [lft rgt]
            A     B
            B     A
            B     C
            C     B
;

--"Colored(name,color)";
--"Next(from,to)";

GreenBlocksAtTheLeft = Colored /^ "name=lft" /^ [color]green;
GreenBlocksAtTheLeft;

CB = Colored v ([name]B ^ ([color])');

(CB /^ "name=name1" /^ [color]green) 
/^ Next /^ 
(CB /^ "name=name2" /^ ([color]green)');
*/

x = (x <OR> y) <and> ( x <OR> <NOT>(<INV>(y)) ).
