:- modeh(1,active(+drug)).

:- modeb(1,lumo(+drug,-energy)).
:- modeb(1,logp(+drug,-hydrophob)).

:- modeb(*,bond(+drug,-atomid,-atomid,#int)).
:- modeb(*,bond(+drug,+atomid,-atomid,#int)).
:- modeb(*,atm(+drug,-atomid,#element,#int,-charge)).

:- modeb(1,gteq(+charge,#float)).
:- modeb(1,gteq(+energy,#float)).
:- modeb(1,gteq(+hydrophob,#float)).
:- modeb(1,lteq(+charge,#float)).
:- modeb(1,lteq(+energy,#float)).
:- modeb(1,lteq(+hydrophob,#float)).

:- modeb(1,(+charge)=(#charge)).
:- modeb(1,(+energy)=(#energy)).
:- modeb(1,(+hydrophob)=(#hydrophob)).

:- modeb(*,benzene(+drug,-ring)).
:- modeb(*,carbon_5_aromatic_ring(+drug,-ring)).
:- modeb(*,carbon_6_ring(+drug,-ring)).
:- modeb(*,hetero_aromatic_6_ring(+drug,-ring)).
:- modeb(*,hetero_aromatic_5_ring(+drug,-ring)).
:- modeb(*,ring_size_6(+drug,-ring)).
:- modeb(*,ring_size_5(+drug,-ring)).
:- modeb(*,nitro(+drug,-ring)).
:- modeb(*,methyl(+drug,-ring)).
:- modeb(*,anthracene(+drug,-ringlist)).
:- modeb(*,phenanthrene(+drug,-ringlist)).
:- modeb(*,ball3(+drug,-ringlist)).

:- modeb(*,member(-ring,+ringlist)).
:- modeb(1,member(+ring,+ringlist)).
:- modeb(1,connected(+ring,+ring)).

% type information

drug(D):-
	name(D,[_|X]), name(Num,X), int(Num),
	Num >= 1, Num =< 230, !.

atomid(A):-
	name(A,[_|X]),
	appnd(Z,[95|Y],X),
	name(N1,Y),
	name(N2,Z),
	int(N1), int(N2),
	N2 >= 1, N2 =< 230,
	N1 =< 500, !.

% background knowledge 

gteq(X,Y):-
	not(var(X)), not(var(Y)),
	float(X), float(Y), 
	X >= Y, !.
gteq(X,X):-
	not(var(X)),
	float(X).

lteq(X,Y):-
	not(var(X)), not(var(Y)),
	float(X), float(Y),
	X =< Y, !.
lteq(X,X):-
	not(var(X)), 
	float(X).

