/*

r#(x ^ y) = (r#x) ^ (r#y). -- (^H)
r#(x v y) = (r#x) v (r#y). -- (vH)
r#x ^ (r#y v r#z) = (r#x ^ r#y) v (r#x ^ r#z). --follows from distributivity of headers
r^R00 = s^R00 -> (r^s)#x = (r#x)^(s#x).

(r ^ s)#x = (r#x) ^ (s#x).

[_reflexivity_];
x^R00 < y^R00 -> r#x < r#y. 
[_augmentation_];
r#x < r#y -> r#(x^z) < r#(y^z).
[_transitivity_];
r#x < r#y & r#y < r#z -> r#x < r#z.


x^R00=x & y^R00=y & r#x < r#y  ->
r = (r v (x^y)) ^ (r v (x^y`)).

x^R00=x & y^R00=y 
& r^R00<x & r^R00<y & x v y = R00 & (r^R00)v[s t]=R00 & x^y != r^R00 & x!=R00
& r = (r v (x^y)) ^ (r v (x^y`)) ->
r#x < r#y.
*/


r = [p  q  r]
     0  a  0
     0  a  1
     0  c  0
     0  c  1
     1  a  0
;
s = [p  q  r]
     0  a  0
     0  a  1
     0  c  0
     1  a  0
;
x = [p];
y = [q];
z = [r];

r#x > r#y ^ r#z.


--x^R00=x  ->
--r#(x ^ y) = (r#x) ^ (r#y). -- (^H)
--r#(x v y) = (r#x) v (r#y). -- (vH)
--(r^s)#x = (r#x)^(s#x).

--x#y = (R11^x)#y.
--R11#y = (R11 v y)#(R11 v y).

/*
(r ^ s)#x;
(r ^ s)'#(r ^ s);
r#x v r'#r;
s#x v s'#s;
*/
r^R00=s^R00 ->
(r ^ s)#x v (r ^ s)'#(r ^ s) = (r#x v r'#r) ^ (s#x v s'#s).
r^R00=s^R00 ->
(r v s)#x v (r v s)'#(r v s) = (r#x v r'#r) v (s#x v s'#s).
