/*
X=[p  q  r]
   0  a  0
   0  a  1
   1  c  0
   1  c  1
   2  a  0
;

X # [p];
X # [q];
X # [p q];
*/

[_reflexivity_];
x^R00 < y^R00 -> r#x < r#y. 
[_augmentation_];
r#x < r#y -> r#(x^z) < r#(y^z).
[_transitivity_];
r#x < r#y & r#y < r#z -> r#x < r#z.