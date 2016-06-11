output(active/0).

input(ames/0).
input(has_property/2).
input(ashby_alert/2).
input(mutagenic/0).
input(ind/2).
input(atm/4).
input(symbond/3).
input(bond/3).
input(nitro/1).
input(sulfo/1).
input(methyl/1).
input(methoxy/1).
input(amine/1).
input(ketone/1).
input(ether/1).
input(sulfide/1).
input(alcohol/1).
input(phenol/1).
input(ester/1).
input(imine/1).
input(alkyl_halide/1).
input(ar_halide/1).
input(non_ar_6c_ring/1).
input(non_ar_hetero_6_ring/1).
input(six_ring/1).
input(non_ar_5c_ring/1).
input(non_ar_hetero_5_ring/1).
input(five_ring/1).
input(connected/2).


modeh(1,active).

modeb(*,ames).
modeb(*,mutagenic).
modeb(*,has_property(-#property,-#propval)).
modeb(*,ashby_alert(-#alert,-ring)).
modeb(*,ind(-#alert,-nalerts)).

modeb(*,atm(-atomid,-#element,-#integer,-charge)).
modeb(*,symbond(+atomid,-atomid,-#integer)).

modeb(1,(+charge) >= (#charge)).
modeb(1,(+charge) =< (#charge)).
modeb(1,(+charge) = (#charge)).
modeb(1,(+nalerts) >= (#nalerts)).
modeb(1,(+nalerts) =< (#nalerts)).
modeb(1,(+nalerts) = (#nalerts)).

modeb(*,nitro(-ring)).
modeb(*,sulfo(-ring)).
modeb(*,methyl(-ring)).
modeb(*,methoxy(-ring)).
modeb(*,amine(-ring)).
modeb(*,ketone(-ring)).
modeb(*,ether(-ring)).
modeb(*,sulfide(-ring)).
modeb(*,alcohol(-ring)).
modeb(*,phenol(-ring)).
modeb(*,ester(-ring)).
modeb(*,imine(-ring)).
modeb(*,alkyl_halide(-ring)).
modeb(*,ar_halide(-ring)).
modeb(*,non_ar_6c_ring(-ring)).
modeb(*,non_ar_hetero_6_ring(-ring)).
modeb(*,six_ring(-ring)).
modeb(*,non_ar_5c_ring(-ring)).
modeb(*,non_ar_hetero_5_ring(-ring)).
modeb(*,five_ring(-ring)).
modeb(1,connected(+ring,+ring)).

determination(active/0,ames/0).
determination(active/0,has_property/2).
determination(active/0,ashby_alert/2).
determination(active/0,mutagenic/0).
determination(active/0,ind/2).
determination(active/0,atm/4).
determination(active/0,symbond/3).
determination(active/0,nitro/1).
determination(active/0,nitro/1).
determination(active/0,sulfo/1).
determination(active/0,methyl/1).
determination(active/0,methoxy/1).
determination(active/0,amine/1).
determination(active/0,ketone/1).
determination(active/0,ether/1).
determination(active/0,sulfide/1).
determination(active/0,alcohol/1).
determination(active/0,phenol/1).
determination(active/0,ester/1).
determination(active/0,imine/1).
determination(active/0,alkyl_halide/1).
determination(active/0,ar_halide/1).
determination(active/0,non_ar_6c_ring/1).
determination(active/0,non_ar_hetero_6_ring/1).
determination(active/0,six_ring/1).
determination(active/0,non_ar_5c_ring/1).
determination(active/0,non_ar_hetero_5_ring/1).
determination(active/0,five_ring/1).
determination(active/0,connected/2).
determination(active/0,'=<'/2).
determination(active/0,'>='/2).
determination(active/0,'='/2).
