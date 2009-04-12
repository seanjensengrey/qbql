/*
x ^ (y v z) = (x ^ (z v (R00 ^ y))) v (x ^ (y v (R00 ^ z))).
(R00 ^ (x ^ (y v z))) v (y ^ z) = ((R00 ^ (x ^ y)) v z) ^ ((R00 ^ (x ^ z)) v y).
(x ^ y) v (x ^ z) =  x ^ ( ((x v z) ^ y) v ((x v y) ^ z) ).
y v z < y + z. --x ^ (y v z) < x ^ (y + z).
y + z < y ^ z.
(x ^ y) v (x ^ z) < x ^ (y + z).



(y ^ R00) v ((y ^ x) v (y ^ x')) = y.
R11^((y ^ x) v (y ^ x')) = R11^y.

y v z = (y + z) v ((y v z) ^ R00).

--R00 < (x v y v z) ^ ((x v y) + (x v z))'.-- -> x ^ (y v z) = (x ^ y) v (x ^ z).
(x v y v z) = ((x v y) + (x v z))' -> x ^ (y v z) = (x ^ y) v (x ^ z).

R00 ^ (x v y) = R00 ^ (x v z) -> R00 < (x v y v z) ^ ((x v y) + (x v z))'.

x ^ (y v z) = (x ^ y) v (x ^ z) v ((x ^ (y v z)) ^ ((x ^ y) v (x ^ z))').

--(x ^ y) v (x ^ z) = (x ^ (y + z)) v (((x ^ y) v (x ^ z)) ^ R00).

--false: (x /\ y) /\ z = x /\ (y /\ z).

--false: x /\ (x /\ y) = x /\ y.

x * (x * y) = x * y.

--false:x * (y * z) = y * (x * z).

x + y = y -> x * y = x.

x v R11 = x -> (x ^ y) v ((x v y) ^ R00) = (x^R00) v y.

x * y = (x ^ y) v ((x v y) ^ R00).


a v x = b &
a^R00=x^R00 &
(a^x)v R00 = R00 -> x = b ^ a'.

*/

--(R11 /\ x) ^ x = R11 ^ x.
--x \/ y = (x ^ y) v ((x /\ y)^R00).
x` ^ x = x ^ R11.
x` v x = x v R00. 
