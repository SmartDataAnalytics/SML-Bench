:- dynamic fib/2.

:- modeh(1, fib(+int,-int)).
:- modeb(1, pred(+int,-int)).
:- modeb(1, fib(+int,-int)).
:- modeb(1, pls(+int,+int,-int)).
% :- commutative(pls/3).
% :- determination(fib/2, pred/2).
% :- determination(fib/2, fib/2).
% :- determination(fib/2, pls/3).

pred(A, B):- B is A-1.
pls(A, B, C):- C is A+B.
fib(0, 0):-!.
fib(1, 1):-!.
