:- module(extract_facts,[extract/4]).


/**
*	extract(+KBPath:path_background_knowledge,+PosPath:path_positive_examples,+NegPath:path_negative_examples,+OutPath:path_extactet_facts) is det
*
*	this predicate extracts all the facts in the file BKPath and puts them in the file OutPath. 
*	Moreover it adds into OutPath the positive (PosPath) and negative (NegPath) examples. 
*	However before adding the negative examples it transform them from 'example(...)' into 'neg(example(...))'.
*	In addition it prints the background knowledge base without facts
*
*	@param KBPath path of the file containing the background knowledge base
*	@param PosPath path of the file containing the positive examples
*	@param NegPath path of the file containing the negetive examples
*	@param OutPath path of the file that will contain the extracted facts and the examples
*/
extract(KBPath,PosPath,NegPath,OutPath) :-
	open(KBPath,read,InputKB),
	% read the file and get the set of all facts
	gtrace,
	extract_facts_from_KB(InputKB,Facts),
	% close the InputKB
	close(InputKB),
	% opent the output file
	open(OutPath,write,Output),
	% write all the extracted facts
	write_facts(Facts,Output),
	% open file containg positive examplse
	open(PosPath,read,PosExStream),
	% write all positive examples
	write_pos_examples(PosExStream,Output),
	close(PosExStream),
	% open file containg negative examplse
	open(NegPath,read,NegExStream),
	% write all negative examples
	write_neg_examples(NegExStream,Output),
	close(NegExStream),
	% finaliy close output file
	close(Output).

/**
*	extract(+InputKB:knowledge_base_stream,-Facts:list_facts) is det
*
*	This predicate is used to extract the facts from a file containing a knowledge base.
*/
extract_facts_from_KB(InputKB,Facts) :-
	extract_facts_from_KB(InputKB,[],Facts).


extract_facts_from_KB(InputKB,Facts,Facts) :-
	at_end_of_stream(InputKB),!.

extract_facts_from_KB(InputKB,Facts,ExtractedFacts) :-
	\+ at_end_of_stream(InputKB),!,
	read(InputKB,Predicate),
	(Predicate \== end_of_file ->
		(ground(Predicate) ->
			append(Facts,[Predicate],NewFacts),
			extract_facts_from_KB(InputKB,NewFacts,ExtractedFacts)
		;
			portray_clause(Predicate),nl,
			extract_facts_from_KB(InputKB,Facts,ExtractedFacts)
		)
	;
		extract_facts_from_KB(InputKB,Facts,ExtractedFacts)	
	).

% write all the extracted facts
write_facts([],_).

write_facts([HFact|TFacts],Output) :-
	write(Output,HFact),writeln(Output,'.'),
	write_facts(TFacts,Output).


/**
* Simple append the positive examples
*
*/
write_pos_examples(PosExStream,_Output) :-
	at_end_of_stream(PosExStream),!.

write_pos_examples(PosExStream,Output) :-
	\+ at_end_of_stream(PosExStream),!,
	read(PosExStream,Example),
	(Example \== end_of_file ->
		write(Output,Example),writeln(Output,'.'),
		write_pos_examples(PosExStream,Output)
	;
		!
	).


write_neg_examples(NegExStream,_Output) :-
	at_end_of_stream(NegExStream),!.

write_neg_examples(NegExStream,Output) :-
	\+ at_end_of_stream(NegExStream),!,
	read(NegExStream,Example),
	(Example \== end_of_file ->
		write(Output,neg(Example)),writeln(Output,'.'),
		write_neg_examples(NegExStream,Output)
	;
		!
	).

/*
% Useless
extract_facts_from_KB(InputKB,Facts,ExtractedFacts) :-
	\+ at_end_of_stream(InputKB),!,
	read(InputKB,Predicate),
	Predicate =.. [PName|Terms],
	length(Terms,NTerms),
	(Nterms > 0 ->
		% I have to check if all the terms are ground
		check_all
	;
		% the predicate has no terms, so it is a fact
		% It is necessary to append the character '.'
		atom_concat(PName,.,Fact),
		append(Facts,Fact,NewFacts1),
		extract_facts_from_KB(InputKB,NewFacts,ExtractedFacts)
	).
*/

