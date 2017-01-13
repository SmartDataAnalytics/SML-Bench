:- module(convert_results, [convert_results/2,
	printlist/1]).

convert_results([],[]).

convert_results([P- (\+ _)|T], [(P-neg)|TR]) :- !,
	%writeln((P-neg)),
	convert_results(T,TR).

convert_results([P- _|T], [(P-pos)|TR]) :- !,
	%writeln((P-pos)),
	convert_results(T,TR).

printlist([]).
    
printlist([X|List]) :-
    write(X),nl,
    printlist(List).
