-- http://class2go.stanford.edu/db/Winter2013/interactive_exercises/RelationalAlgebraExercisescoreset

Eats = [name, pizza]
Amy	mushroom
Amy	pepperoni
Ben	cheese
Ben	pepperoni
Cal	supreme
Dan	cheese
Dan	mushroom
Dan	pepperoni
Dan	sausage
Dan	supreme
Eli	cheese
Eli	supreme
Fay	mushroom
Gus	cheese
Gus	mushroom
Gus	supreme
Hil	cheese
Hil	supreme
Ian	pepperoni
Ian	supreme
;

Person = [name, age, gender]
Amy	16	female
Ben	21	male
Cal	33	male
Dan	13	male
Eli	45	male
Fay	21	female
Gus	24	male
Hil	30	female
Ian	18	male
;

project pizza 
select "20 <= age"^ "gender='female'" (Eats join Person);