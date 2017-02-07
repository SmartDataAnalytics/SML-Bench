!- mode(animal(+)).

!- mode(has_milk(+)).
!- mode(has_gills(+)).
!- mode(has_covering(+,-)).
!- mode(has_legs(+,-)).
!- mode(homeothermic(+)).
!- mode(has_eggs(+)).
!- mode(habitat(+,-)).

!- determination(animal/1,has_milk/1).
!- determination(animal/1,has_gills/1).
!- determination(animal/1,has_covering/2).
!- determination(animal/1,has_legs/2).
!- determination(animal/1,homeothermic/1).
!- determination(animal/1,has_eggs/1).
!- determination(animal/1,habitat/2).
