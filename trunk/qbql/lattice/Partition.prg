/*
X=[p  q  r]
   0  a  0
   0  a  1
   1  c  0
   1  c  1
   2  a  0
;

X # [p];
X # [r];
X#[p] v X#[r];
(X#[p]) ^ (X#[r]);
*/
/*
[_reflexivity_];
x^R00 < y^R00 -> r#x < r#y. 
[_augmentation_];
r#x < r#y -> r#(x^z) < r#(y^z).
[_transitivity_];
r#x < r#y & r#y < r#z -> r#x < r#z.


r#(x ^ y) = (r#x) ^ (r#y). %^H
r#(x v y) = (r#x) v (r#y). %vH

/*Prove reflexivity:*/ x < y -> r#x < r#y.
/*Given:*/x v y = y. %<g
--"Proof:" r#x v r#y =(vH)= r#(x v y) =(<g)= r#y.
-- Ditto augmentation
-- Transitivity is transitivity of partition lattice order
*/
