!- mode(active(+)).

!- mode(lumo(+,-)).
!- mode(logp(+,-)).

%!- mode(bond(+,-,-,-)).
%!- mode(bond(+,+,-,-)).
!- mode(atm(+,-,-,-,-)).

!- mode(gteq(+,+)).
!- mode(lteq(+,+)).

!- mode(benzene(+,-)).
!- mode(carbon_5_aromatic_ring(+,-)).
!- mode(carbon_6_ring(+,-)).
!- mode(hetero_aromatic_6_ring(+,-)).
!- mode(hetero_aromatic_5_ring(+,-)).
!- mode(ring_size_6(+,-)).
!- mode(ring_size_5(+,-)).
!- mode(nitro(+,-)).
!- mode(methyl(+,-)).
!- mode(anthracene(+,-)).
!- mode(phenanthrene(+,-)).
!- mode(ball3(+,-)).

%!- mode(member(-,+)).
!- mode(member(+,+)).
!- mode(connected(+,+)).

!- determination(active/1,atm/5).
!- determination(active/1,bond/4).
!- determination(active/1,gteq/2).
!- determination(active/1,lteq/2).
!- determination(active/1,'='/2).

!- determination(active/1,lumo/2).
!- determination(active/1,logp/2).

!- determination(active/1,benzene/2).
!- determination(active/1,carbon_5_aromatic_ring/2).
!- determination(active/1,carbon_6_ring/2).
!- determination(active/1,hetero_aromatic_6_ring/2).
!- determination(active/1,hetero_aromatic_5_ring/2).
!- determination(active/1,ring_size_6/2).
!- determination(active/1,ring_size_5/2).
!- determination(active/1,nitro/2).
!- determination(active/1,methyl/2).
!- determination(active/1,anthracene/2).
!- determination(active/1,phenanthrene/2).
!- determination(active/1,ball3/2).
!- determination(active/1,member/2).
!- determination(active/1,connected/2).

