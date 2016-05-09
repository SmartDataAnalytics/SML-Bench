% :- dynamic(active/1).

:- modeh(1,active(+drug))?
:- modeb(*,atm(+drug,-atomid,-element,#integer,#real))?
:- modeb(*,bond(+drug,+atomid,-atomid,#integer))?
:- modeb(*,drug(+drug))?
:- modeb(*,element(+element))?

:- determination(active/1,atm/5)?
:- determination(active/1,bond/4)?
