% % type information
% 
% drug(D):-
% 	name(D,[_|X]), name(Num,X), int(Num),
% 	Num >= 1, Num =< 230, !.

% atomid(A):-
% 	name(A,[_|X]),
% 	append(Z,[95|Y],X),
% 	name(N1,Y),
% 	name(N2,Z),
% 	int(N1), int(N2),
% 	N2 >= 1, N2 =< 230,
% 	N1 =< 500, !.

% append([],A,A).
% append([H|T],A,[H|T1]):-
%         append(T,A,T1).
% 


% charge(X):-
%         float(X).
% energy(X):-
%         float(X).
% hydrophob(X):-
%         float(X).

% ring([_|_]).

% ring(X):-
% 	name(X,[114|_]).

% ringlist(X):- 
% 	name(X,[114|_]).
% 
% % ringlist([]).
% % ringlist([Ring|Rings]):-
%         % ring(Ring),
%         % ringlist(Ringlist).



element(br).
element(c).
element(cl).
element(f).
element(h).
element(i).
element(n).
element(o).
element(s).


% background knowledge 

% gteq(X,Y):-
% 	not(var(X)), not(var(Y)),
% 	float(X), float(Y), 
% 	X >= Y, !.
% gteq(X,X):-
% 	not(var(X)),
% 	float(X).

% lteq(X,Y):-
% 	not(var(X)), not(var(Y)),
% 	float(X), float(Y),
% 	X =< Y, !.
% lteq(X,X):-
% 	not(var(X)), 
% 	float(X).


:- set(i,2).
:- set(nodes,20000).
:- set(noise,5).
:- set(c,3).
:- set(verbose,0).
:- noreductive.
:- nosplit.
