include udf.def;  


Cars = [make seats mileage]
    Roadster     2      20
  "Ferrari F1"     1      10
      Hammer     4       5
      Camry     4       15
;

winnow=
Cars
^ <NOT> (
((Cars ^ (Cars /^ "seats=s2"/^"make=ma2"/^"mileage=mi2") ^ "seats<s2" ^ "mileage<=mi2")
 v
(Cars ^ (Cars /^ "seats=s2"/^"make=ma2"/^"mileage=mi2") ^ "seats<=s2" ^ "mileage<mi2")
) v [make seats mileage])
;

winnow;