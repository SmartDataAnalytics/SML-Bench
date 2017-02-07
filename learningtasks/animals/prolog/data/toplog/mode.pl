:- dynamic(animal/1).

:- modeh(1,animal(+animal)).

:- modeb(1,has_milk(+animal)).
:- modeb(1,has_gills(+animal)).
:- modeb(1,has_covering(+animal,#covering)).
:- modeb(1,has_legs(+animal,#integer)).
:- modeb(1,homeothermic(+animal)).
:- modeb(1,has_eggs(+animal)).
:- modeb(*,habitat(+animal,#habitat)).
