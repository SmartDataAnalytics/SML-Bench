target(animal).

type(animal).
type(covering).
type(int, continuous).
type(habitat).

predicate(animal/1,animal,'#').
predicate(has_milk/1,animal,'#').
predicate(has_gills/1,animal,'#').
predicate(has_covering/2,animal,covering,'#-').
predicate(has_legs/2,animal,int,'#-').
predicate(homeothermic/1,animal,'#').
predicate(has_eggs/1,animal,'#').
predicate(habitat/2,animal,habitat,'#-').

