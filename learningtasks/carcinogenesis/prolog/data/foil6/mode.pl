target(active).

type(drug).
type(property).
type(propval).
type(alert).
%type(ring).
type(int,continuous).
type(atm).
type(element).
type(charge,continuous).
type(float,continuous).

predicate(active/1,drug).
predicate(ames/1,drug).
predicate(mutagenic/1,drug).
predicate(has_property/3,drug,property,propval).

% ashby_alert facts contain lists as e.g. ashby_alert(amino,d15,[d15_6,d15_24,d15_26,d15_27])
% This cannot be handled by FOIL and is thus ommitted.
%predicate(ashby_alert/3,alert,drug,ring).

predicate(ind/3,drug,alert,int).
predicate(atm/5,drug,atm,element,int,charge).
predicate(symbond/4,drug,atm,atm,int).

%predicate(gteq/2,charge,float).
%predicate(lteq/2,charge,float).
% TODO: =, lteq, gteq, ...

% Facts of the following predicates contain lists as e.g. methoxy(d11,[d11_13,d11_12,d11_14,d11_15,d11_16])
% This cannot be handled by FOIL and is thus ommitted.
%predicate(nitro/2,drug,ring).
%predicate(sulfo/2,drug,ring).
%predicate(methyl/2,drug,ring).
%predicate(methoxy/2,drug,ring).
%predicate(amine/2,drug,ring).
%predicate(aldehyde/2,drug,ring).
%predicate(ketone/2,drug,ring).
%predicate(ether/2,drug,ring).
%predicate(sulfide/2,drug,ring).
%predicate(alcohol/2,drug,ring).
%predicate(phenol/2,drug,ring).
%predicate(carboxylic_acid/2,drug,ring).
%predicate(ester/2,drug,ring).
%predicate(amide/2,drug,ring).
%predicate(deoxy_amide/2,drug,ring).
%predicate(imine/2,drug,ring).
%predicate(alkyl_halide/2,drug,ring).
%predicate(ar_halide/2,drug,ring).
%predicate(benzene/2,drug,ring).
%predicate(hetero_ar_6_ring/2,drug,ring).
%predicate(non_ar_6c_ring/2,drug,ring).
%predicate(non_ar_hetero_6_ring/2,drug,ring).
%predicate(six_ring/2,drug,ring).
%predicate(carbon_5_ar_ring/2,drug,ring).
%predicate(hetero_ar_5_ring/2,drug,ring).
%predicate(non_ar_5c_ring/2,drug,ring).
%predicate(non_ar_hetero_5_ring/2,drug,ring).
%predicate(five_ring/2,drug,ring).
%predicate(connected/2,ring,ring).
