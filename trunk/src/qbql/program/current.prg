--include "C:/eclipse/qbql_trunk/src/qbql/program/Figure1.db";
include udf.def;
--include volume.db;


-- wrong ("i in {1,...,1000}" ^ "3 * x = i" /^ "int x") 
--v ("i in {1,...,1000}" ^ "5 * x = i" /^ "int x");

Colored = [name color]
           A    green
           --B    red
           C    blue
;

"Colored(name,color)" = Colored <OR> ([name] B); 

Next = [name1 name2]
            A     B
            B     A
            B     C
            C     B
;

--"Colored(name,color)";
--"Next(from,to)";

("Colored(name1,color)" /^ [color]green) 
/^ Next /^ 
("Colored(name2,color)" /^ ([color]green)');