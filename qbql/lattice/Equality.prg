/*
x ~ x.
x ~ y -> y ~ x.
x ~ y & y ~ z -> x ~ z.
x ~ y & x ^ R00 = y ^ R00 -> x = y.
(x v y) ^ R00 = (x v z) ^ R00 & y ~ z -> x ^ y ~ x ^ z.
(x v y) ^ R00 = (x v z) ^ R00 & y ~ z -> x v y ~ x v z.

x@x = R01.
x/|\x = R01.
x@y = y@x.
(x@y) ^ R00 = ((x` v y) ^ (x v y`)) ^ R00.
*/

--(x@y)@z = x@(y@z).

x ~ y -> ((x@y) ^ x) v (y ^ R00) = y.
x ~ y -> ((x/|\y) ^ x) v (y ^ R00) = y.

--(x@y)~z -> x~(y@z).
x ~ y -> (x@y) ^ x = (x@y) ^ y. 
x ~ y -> (x/|\y) ^ x = (x/|\y) ^ y. 





