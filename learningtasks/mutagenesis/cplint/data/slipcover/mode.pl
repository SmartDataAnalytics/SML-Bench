output(active/0).

input_cw(lumo/1).
input_cw(logp/2).
input_cw(bond/3).
input_cw(atm/4).
input_cw(benzene/1).
input_cw(carbon_5_aromatic_ring/1).
input_cw(carbon_6_ring/1).
input_cw(hetero_aromatic_6_ring/1).
input_cw(hetero_aromatic_5_ring/1).
input_cw(ring_size_6/1).
input_cw(ring_size_5/1).
input_cw(nitro/1).
input_cw(methyl/1).
input_cw(anthracene/1).
input_cw(phenanthrene/1).
input_cw(ball3/1).


modeh(1,active).

modeb(1,lumo(-energy)).
modeb(1,logp(-hydrophob)).
modeb(*,atm(-atomid,-#element,-#int,-charge)).
modeb(*,bond(-atomid,-atomid,-#int)).
modeb(1,(+charge) >= (#charge)).
modeb(1,(+charge) =< (#charge)).
modeb(1,(+charge)= #charge).
modeb(1,(+hydrophob) >= (#hydrophob)).
modeb(1,(+hydrophob) =< (#hydrophob)).
modeb(1,(+hydrophob)= #hydrophob).
modeb(1,(+energy) >= (#energy)).
modeb(1,(+energy) =< (#energy)).
modeb(1,(+energy)= #energy).

modeb(*,benzene(-ring)).
modeb(*,carbon_5_aromatic_ring(-ring)).
modeb(*,carbon_6_ring(-ring)).
modeb(*,hetero_aromatic_6_ring(-ring)).
modeb(*,hetero_aromatic_5_ring(-ring)).
modeb(*,ring_size_6(-ring)).
modeb(*,ring_size_5(-ring)).
modeb(*,nitro(-ring)).
modeb(*,methyl(-ring)).
modeb(*,anthracene(-ringlist)).
modeb(*,phenanthrene(-ringlist)).
modeb(*,ball3(-ringlist)).

modeb(*,member(-ring,+ringlist)).
modeb(1,member(+ring,+ringlist)).


lookahead(logp(B),[(B=_C)]).
lookahead(logp(B),[>=(B,_C)]).
lookahead(logp(B),[=<(B,_C)]).
lookahead(lumo(B),[(B=_C)]).
lookahead(lumo(B),[>=(B,_C)]).
lookahead(lumo(B),[=<(B,_C)]).
%lookahead(atm(_,_,_,_,C),[>=(C,_)]).
%lookahead(atm(_,_,_,_,C),[=<(C,_)]).
%lookahead(atm(_,_,_,_,C),[(C=_)]).

determination(active/0,lumo/1).
determination(active/0,logp/2).
determination(active/0,bond/3).
determination(active/0,atm/4).
determination(active/0,benzene/1).
determination(active/0,carbon_5_aromatic_ring/1).
determination(active/0,carbon_6_ring/1).
determination(active/0,hetero_aromatic_6_ring/1).
determination(active/0,hetero_aromatic_5_ring/1).
determination(active/0,ring_size_6/1).
determination(active/0,ring_size_5/1).
determination(active/0,nitro/1).
determination(active/0,methyl/1).
determination(active/0,anthracene/1).
determination(active/0,phenanthrene/1).
determination(active/0,ball3/1).
