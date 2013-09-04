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

Serves = [pizzeria, pizza, price]
"Chicago Pizza"	cheese	7.75
"Chicago Pizza"	supreme	8.5
Dominos	cheese	9.75
Dominos	mushroom	11
"Little Caesars"	cheese	7
"Little Caesars"	mushroom	9.25
"Little Caesars"	pepperoni	9.75
"Little Caesars"	sausage	9.5
"New York Pizza"	cheese	7
"New York Pizza"	pepperoni	8
"New York Pizza"	supreme	8.5
"Pizza Hut"	cheese	9
"Pizza Hut"	pepperoni	12
"Pizza Hut"	sausage	12
"Pizza Hut"	supreme	12
"Straw Hat"	cheese	9.25
"Straw Hat"	pepperoni	8
"Straw Hat"	sausage	9.75
;

/*
project pizza 
  select "20 <= age"^ "gender='female'" 
    (Eats join Person);
    
Eats /^ Person /^ "20 <= age" /^ "gender='female'"; 
*/

project pizzeria 
  select ([name] Amy Fay) ^"price<=10" 
    (Serves join Eats);
