
---

## Programmatic Environment ##

QBQL is implemented in java. Therefore, for java person the most natural way to get it running is to install _Eclipse_, _Subversion_ plugin, and then check out the source from _google_ _code_ svn. All these steps are standard and are documented elsewhere.

Alternatively, you can download [qbql.jar](http://qbql.googlecode.com/svn/trunk/dist/qbql.jar) and run it from the command line as follows:

```
C:\> java -jar C:\qbql_trunk\dist\qbql.jar C:\qbql_trunk\src\qbql\program\current.prg
```

Before proceeding to the next section (which is written in cookbook style) a reader is advised to read QBQL introductory articles
  * [Part 1](http://vadimtropashko.wordpress.com/relational-programming-with-qbql/)
  * [Part 2](http://vadimtropashko.wordpress.com/relational-programming-with-qbql/manipulating-strings-in-qbql/)
  * [Part 3](http://vadimtropashko.wordpress.com/relational-programming-with-qbql/aggregation-and-set-joins/)

## Database ##

Database is a set of named relations. Relations are typically created with assigment statements like this

```
A=[p]
   1
;
```

which creates new unary relation `A` with the header `{p}`, and containing one tuple `<1>`. A reader is encouraged to familiarize yourself with a [typical database initialization file](http://code.google.com/p/qbql/source/browse/trunk/src/qbql/program/Figure1.db). Database initialization is optional, a user is free to run a probgram over the empty database, and initilalize all the auxiliary relations on the fly. If one needs a database initilized in adedicated file, one uses `include` directive, for example

```
include "C:/eclipse/qbql_trunk/src/qbql/program/Figure1.db";
```


## Assertions/Program ##

A program is a sequence of _assignment operations_, queries, and _assertions_ mixed together. Assignment operations introduce new relation variables (and record their names and values in the database). For example,

```
N=[p]
   1
   2
^ A
;
```

creates a new anonymous unary relation with the header `{p}` containing two tuples `{<1>,<2>}`, then joins it with the relation `A`, which is assumed to exist in the database and, finally, assigns the resulting value to new relation variable `N`.

The query

```
N;
```

outputs the value of `N` to the console.

Assertions are used to verify relational lattice laws, or just establish some propositions against certain database. They are either equalities

```
expr1 = expr2.
expr3 = expr4.
...
```

or inequalities

```
expr1 < expr2.
expr3 > expr4.
...
```

or complex assertions built with boolean operations out of primitive equalities and inequalities. Each expression on left-hand and right-hand side of an equality or inequality is either a relation/predicate variable, or a term built via operations of join `^`, inner union `v`, complement `'`, set join, or _user-defined_ operation applied to either relations or predicates. See [QBQL grammar](http://code.google.com/p/qbql/source/browse/trunk/src/qbql/lattice/lattice.grammar) for the details.

## Run the program ##

Now you are set. Just run the Run.main() and watch the console output. It would print out all the query outputs and all the offending assertion (together with the values of free variables that make counterexample). This variables assigment can be literally copied and pasted into back to the program file, and you can also split the invalid assertion into smaller pieces to evaluate them individulally. Let's check the inner join associativity.

The inner join `x <and> y` is classic relational algebra natural join projected onto the common set of attributes. It is user-defined operation in QBQL:

```
x <and> y = (x ^ y) v ((x v y) ^ []).
```

User defined operations can be introduced as assertions at any point; the inner join is happened to be defined in the file `udf.def`. Our goal here is to verify inner join associativity

```
x <and> (y <and> z) = y <and> (x <and> z).
```

against the database defined in Figure1.db. Therefore, just paste this assertion into `current.prg`, then run

```
C:\> java -jar C:\qbql_trunk\dist\qbql.jar C:\qbql_trunk\src\qbql\program\current.prg
```

and witness the output

```
z = [q]
     c
;
y = [p  q]
     1  a
     1  b
     2  a
;
x = [p]
     1
;
```

Then, replace the assertion with the following program:

```
z = [q]
     c
;
y = [p  q]
     1  a
     1  b
     2  a
;
x = [p]
     1
;
y <and> z;
x <and> z;
x <and> (y <and> z);
y <and> (x <and> z);
```

to see the last four queries evaluated step-by-step.