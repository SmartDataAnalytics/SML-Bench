gteq(X,Y):-
	number(X), number(Y), 
	X >= Y, !.
gteq(X,X):-
	number(X).

lteq(X,Y):-
	number(X), number(Y),
	X =< Y, !.
lteq(X,X):-
	number(X).

connected(Ring1,Ring2):-
        Ring1 \= Ring2,
        element(A,Ring1),
        element(A,Ring2), !.

element(H,[H|_]):- !.
element(H,[_|T]):- element(H,T).

symbond(M,A,B,T):-
	var(T), !,
	sym_bond(M,A,B,T).
symbond(M,A,B,T):-
	sym_bond(M,A,B,T), !.

abandon:-
	hypothesis(_,Body,_),
	has_lit(symbond(_,A,B,_),Body,Rest),
	has_lit(symbond(_,C,D,_),Rest,_),
	A == D, B == C, !.
abandon:-
	hypothesis(_,Body,_),
	has_lit(lteq(A,B),Body,Rest),
	has_lit(gteq(C,D),Rest,_),
	A == C, B == D, !.

has_lit(L,(L1,L2),L2):-
      L = L1, !.
has_lit(L,(_,L2),Rest):-
      !,
      has_lit(L,L2,Rest).
has_lit(L,L,true).

