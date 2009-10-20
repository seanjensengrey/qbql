/*

--tests for Vars, Tokens, Links, and Paths predicates

[pos] "[15,57)" ^ Vars;

[pos] "[21,22)" ^ Tokens;
[txt] CDATA ^ Tokens;

[up] "[15,57)" ^ Paths;
[down] "[15,57)" ^ Paths;

[up] "[15,57)" ^ Links;
[down] "[15,57)" ^ Links;

*/

/*Tokens /^ [txt] item /^ [pos=down] /^ Links /^ [pos=up]^ Vars;
[name  pos]
                                                     closetag  [115,119)
                                                     closetag  [57,61)
                                                     opentag  [3,15)
                                                     opentag  [61,73)
;
*/
Items = Tokens /^ [txt] item /^ [pos=down] /^ Links /^ [up=down] /^ Links;
Items;
Equalities = Vars /^ [name]equality;
Equalities;
Attributes = Equalities /^ [pos=up] ^ Links /^ [pos=down] 
^ Vars /^ [name]identifier
/^ Tokens;
Attributes;
Values = Attributes /^ [txt=var] ^ Links /^ [pos=down]
^ Vars /^ [name]value
/^ Tokens /^[up=down];
Values;
Pivoted = Items ^ Paths /^ Values;

Vertical = Pivoted /^ [var]vertical /^ [txt=vertical];
Vertical;
Node = Pivoted /^ [var]node /^ [txt=node];
Node;

Sql = Tokens /^ [txt] sql /^ [pos=down] /^ Links /^ [up=down] /^ Links 
/^ Paths /^ [pos=down]  
^ Vars /^ [name]cdata
^ Tokens;
Sql;

Title = Tokens /^ [txt]title /^ [pos=down] /^ Links /^ [up=down] /^ Links 
/^ Paths /^ [pos=down]  
^ Vars /^ [name]cdata
^ Tokens;
Title;

TitleItems = Items ^ Paths /^ [down=pos] /^ Title /^ [txt=title];
TitleItems;

SqlItems = Items ^ Paths /^ [down=pos] /^ Sql /^ [txt=sql];
SqlItems;


Result = TitleItems ^ SqlItems ^ Vertical /^ Node;
--Items ^ Paths /^ [down=pos] ^ Equalities;
--Items ^ Paths /^ [down=pos] ^ Vars /^ [name]equality;

Result;
