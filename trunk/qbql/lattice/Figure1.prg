/*
x ^ (y v z) = (x ^ (z v (R00 ^ y))) v (x ^ (y v (R00 ^ z))).

(R00 ^ (x ^ (y v z))) v (y ^ z) = ((R00 ^ (x ^ y)) v z) ^ ((R00 ^ (x ^ z)) v y).
(x ^ y) v (x ^ z) =  x ^ ( ((x v z) ^ y) v ((x v y) ^ z) ).
y v z > y + z. --x ^ (y v z) < x ^ (y + z).
x v y > x + y.
x ^ y < x * y.
x + y > x ^ y.
x v y > x * y.
(x ^ y) v (x ^ z) > x ^ (y + z).



(y ^ R00) v ((y ^ x) v (y ^ x')) = y.
R11^((y ^ x) v (y ^ x')) = R11^y.

y v z = (y + z) v ((y v z) ^ R00).

--R00 < (x v y v z) ^ ((x v y) + (x v z))'.-- -> x ^ (y v z) = (x ^ y) v (x ^ z).
(x v y v z) = ((x v y) + (x v z))' -> x ^ (y v z) = (x ^ y) v (x ^ z).

R00 ^ (x v y) = R00 ^ (x v z) -> R00 > (x v y v z) ^ ((x v y) + (x v z))'.

x ^ (y v z) = (x ^ y) v (x ^ z) v ((x ^ (y v z)) ^ ((x ^ y) v (x ^ z))').

--(x ^ y) v (x ^ z) = (x ^ (y + z)) v (((x ^ y) v (x ^ z)) ^ R00).


--false: (x /\ y) /\ z = x /\ (y /\ z).

--false: x /\ (x /\ y) = x /\ y.

x ^ y = x /\ y <-> (x v y)^R00 = R00. 


x * (x * y) = x * y.

--false:x * (y * z) = y * (x * z).

x + y = y -> x * y = x.

x v R11 = x -> (x ^ y) v ((x v y) ^ R00) = (x^R00) v y.

x * y = (x ^ y) v ((x v y) ^ R00).
x + y = (x' ^ y')'.


a v x = b &
a^R00=x^R00 &
(a^x)v R00 = R00 -> x = b ^ a'.



x` ^ x = x ^ R11.
x` v x = x v R00. 

a ^ x = b &
a v R00=x v R00 &
(a v x)^ R00 = R00 -> x = b * a`.

x` v y` = (x + y)`.
x' ^ y' = (x + y)'.

x` + y` = (x v y)`.


((x' ^ y')')` = x` v y`.


x ^ (y` v z`) = (x ^ y`) v (x ^ z`).

x`=y`-> x^R00 = y^R00.

x`=y`-> x v R00 = y v R00.


(x` v y`) ^ (y` v x`)=(x ^ y)` v (x v y)`.

--symmetric difference--
((x'^y)v(x^y')) * ((x` v y) ^ (x v y`)) = R00 <-> x = y.

(x'^y) * (x v y`) = R00 <-> x > y.
x > y <-> x ^ y = y.

x = (x + y) * (x + (y`)').
x = (x ^ y) v (x ^ (y')`). 



x = y ->  (x'^y)v(x^y') * ((x` v y) ^ (x v y`)) = R00.
x = y ->  ((x v y)^(x^y)') * ((x ^ y)v(x v y)`) = R00.

(x')` = (x`)' <-> R00 > x | x > R11.

x ^ R00 = R00 -> x = R00 | x = R00'.

(x`)` = x <-> (x')` = (x`)'.

(x ^ x') * (x v x`) = R00.

R00'=R11`.

x' v x` = R01. 


x' + x` = R11.

x ^ (x`)' = R10.

(x`)` > x.
x < y -> (x`)` < (y`)`. % `` is closure
((x`)`)` = (x)`.        % (x``)``=x``

x v R00 = R01 & y v R00 = R01 -> y`*x = (y^R00)` v x.
(x`*y)^(y`*x) = (x v (y^R00)`) ^ (y v (x^R00)`).

x /\ y = ((x`*y)+(y`*x)) ^ 
(
 ((x`*y)+(y`*x)) *
 ((x'^y)v(y'^x))
)'.

x ^ (y v z) > (x ^ y) v (x ^ z).


(y')` = (y`)' v (y` ^ (y')`).
(y`)' = (y')` ^ (y` ^ (y')`)'.
(y`)' = (y')` ^ ((y`)' v ((y')`)').

x+y = (x ^ R11) v (y ^ (x v R11)).
x = ( x ^ ( x v R00 ) ) ^ ( x v y ).
x = x v ( R00 ^ ( R11 ^ ( y ) ` ) ).
(((x)`)')` = ( x ) ' ^ ( ( x v R00 ) ) '.
(x`)` = (x v R11) ^ (x v R00). 

x + (x * y) > x.
x + (x ^ y) < x.
(x + (x ^ y))^R11 = x^R11.
(x + (x ^ y))v(x)` =  (x)` v (x ^ y).

x+y = (R11 ^ y) v (x ^ y) v (x ^ R11).
x*y = (x ^ R00) v (x ^ y) v (R00 ^ y).

x+y = (R00 ^ (x ^ y)) v (R11 ^ y) v (x ^ R11).
x*y = (x ^ y ^ R11) v ((x v y) ^ R00).


--x*y = (x' v y')' v (x` ^ y`)`.
x+y = (x' ^ y')' ^ (x` v y`)`.
x+y = (x' ^ y')' ^ (x v y).

x v (R11 /\ (R11 ^ (x /\ y)')) = R00 v R11.
*/
x = (x ^ (y')`) v (x ^ y).
(x`)` = (x v (y')`) ^ (x v y). 
