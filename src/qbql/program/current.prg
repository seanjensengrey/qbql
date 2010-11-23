--include "C:/eclipse/qbql_trunk/src/qbql/program/Figure1.db";
--include udf.def;
--include volume.db;

--"x+y=5" ^ "x-y=1" ^ "int x < 10";

--("x+z=5" /^ "x-y=1") ^ "y=z";

--"5 <= int x < 10" ^ "2*y=x"; --^ "int y";

--"3 < 2";


"i in {1,...,5}" /^ "i * i = i2";

        