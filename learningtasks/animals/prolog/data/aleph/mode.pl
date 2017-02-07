:- modeh(1,animal(+animal)).

:- modeb(1,has_milk(+animal)).
:- modeb(1,has_gills(+animal)).
:- modeb(1,has_covering(+animal,#covering)).
:- modeb(1,has_legs(+animal,#integer)).
:- modeb(1,homeothermic(+animal)).
:- modeb(1,has_eggs(+animal)).
:- modeb(*,habitat(+animal,#habitat)).

:- determination(animal/1,has_milk/1).
:- determination(animal/1,has_gills/1).
:- determination(animal/1,has_covering/2).
:- determination(animal/1,has_legs/2).
:- determination(animal/1,homeothermic/1).
:- determination(animal/1,has_eggs/1).
:- determination(animal/1,habitat/2).
