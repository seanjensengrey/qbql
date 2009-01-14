
/*
transitivity:

x v R00 = R00 &
y v R00 = R00 &
z v R00 = R00 &
(r v (x ^ y)) ^ (r v (x ^ ((r^R00) v (x^y)`))) = r &  
(r v (y ^ z)) ^ (r v (y ^ ((r^R00) v (y^z)`))) = r ->
( r v (x ^ (z v y`)) ) ^ ( r v (x ^ ((r^R00) v (x ^ (z v y`))`))) = r.

augmentation:
x v R00 = R00 &
y v R00 = R00 &
z v R00 = R00 &
(r v (x ^ y)) ^ (r v (x ^ (r v (x^y)`))) = r ->  
( r v (x ^ (y ^ z)) ) ^ ( r v ((x ^ z) ^ (r v (x ^ (y ^ z))`))) = r.

x v R00 = R00 &
y v R00 = R00 &
z v R00 = R00 &
(r v (x ^ y)) ^ (r v (x ^ ((r^R00) v (x^y)`))) = r ->  
( r v (x ^ (y ^ z)) ) ^ ( r v ((x ^ z) ^ ((r^R00) v (x ^ (y ^ z))`))) = r.
*/