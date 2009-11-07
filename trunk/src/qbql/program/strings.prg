/*
Cat ^ [source] "Hello World";

Cat ^ [source from] Hello 3;

Cat ^ [source] Hello World ^ [from] 3;

Cat ^ [] = [from  prefix  postfix   source].

[source from] Hello 3 /^ [from=f] /^ [source=s];

(Cat /^ [source=src]) ^ [src] Hello World ^ [from] 3
~
Cat ^ [source] Hello World ^ [from] 3
.
*/

Substr =
(Cat /^ [from=to] /^ [prefix=_1])
/^
(Cat /^ [source=_1] /^ [postfix=fragment])
;
Substr^[]=[fragment  from  postfix  prefix  source  to].

(Substr ^ [source] Hello World ^ [from to] 1 3) v [fragment]=[fragment]
                                                   el
                                                   or
.
(Substr ^ [source] Hello World ^ [fragment] "o" l) v [from fragment]=[fragment  from]
                                                           l  2
                                                           l  3
                                                           o  1
                                                           o  4
.

Substr1 = (Substr /^ [source=src1] /^ [from=from1]) v [src1 from1 fragment];
Substr2 = (Substr /^ [source=src2] /^ [from=from2]) v [src2 from2 fragment];
Substr1^[]=[src1 from1 fragment].
Substr2^[]=[src2 from2 fragment].

((Substr1 ^ [src1] Hello) ^ (Substr2 ^ [src2] World) ^ ([fragment]"")') 
v [from1 from2 fragment]=[fragment  from1  from2]
                          l  2  3
                          l  3  3
                          o  4  1
.


HelloFgmts = ([from=from1] /^ Substr /^ [source] Hello) v [from1 fragment];
WorldFgmts = ([source] World /^ Substr /^ [from=from2]) v [from2 fragment];
HelloFgmts ^ WorldFgmts ^ ([fragment]"")'=[fragment  from1  from2]
                                       l  2  3
                                       l  3  3
                                       o  4  1
.

SubstrSrc = (Substr /^ [source] "Hello World" /^ [fragment]o) v [prefix postfix];
SubstrSrc;
(Substr /^ (SubstrSrc /^ [fragment]"***")) v [source] ;

/*

Sum /= [summands] 1 2 3 4;

Emp = [EMPNO ENAME JOB MGR SAL DEPTNO]
 7369 SMITH  CLERK 7902 800 20 
 7499 ALLEN SALESMAN 7698 1600 30 
 7521 WARD SALESMAN 7698 1250 30 
 7566 JONES MANAGER 7839 2975  20 
 7654 MARTIN SALESMAN 7698 1250 30 
 7698 BLAKE MANAGER 7839 2850  30 
 7782 CLARK MANAGER 7839 2450  10 
 7788 SCOTT ANALYST 7566 3000  20 
 7839 KING PRESIDENT 7839 5000  10 
 7844 TURNER SALESMAN 7698 1500 30 
 7876 ADAMS CLERK 7788 1100  20 
 7900 JAMES CLERK 7698 950  30 
 7902 FORD ANALYST 7566 3000  20 
 7934 MILLER CLERK 7782 1300  10 
;

Dept = [DEPTNO DNAME LOC]
10 ACCOUNTING LONDON
20 SALES PARIS
30 RESEARCH ROME
40 OPERATIONS LONDON
;

((Emp v [DEPTNO SAL]) /^ [SAL=summands]) /= Sum;

MySum = Sum /^ [result=sum];

((Emp v [DEPTNO SAL]) /^ [SAL=summands]) /= MySum;

(Emp v [ENAME SAL]) /^ [SAL=x] /^ [y]10000 /^Plus;

(Emp v [ENAME SAL]) ^ [SAL=lft] /^ [rgt]1000 /^ LE;
*/
Neg = Plus /^ [z]0;
--Positive = LE /^ [rgt]0 /^ [lft=x];

--Abs = ([x=abs] v ([z]0 /^ Plus /^ [y=abs])) ^ LE /^ [rgt]0 /^ [lft=abs];

--[x] 1 -1 0 ^ Abs;

R = [x] 1 -3 0 ^ [x=rgt] ^ [lft]0 /^ LE ^ [x=abs]; 
R;
R = [x] 1 -3 0 ^ [z]0 ^ Plus ^ [y=rgt] ^ [lft]0 /^ LE /^ [y=abs]; 
R;