# Aleph (SWI Prolog Port)

## Description

[Aleph](http://www.cs.ox.ac.uk/activities/machinelearning/Aleph/) ILP system [ported](https://github.com/friguzzi/aleph) to [SWI Prolog](https://www.swi-prolog.org/).

## Installation

To be able to use this Aleph port an installed [SWI Prolog](https://www.swi-prolog.org/) system is required.
There are SWI Prolog software packages for the most common Linux distributions as well als binary and source distributions on the [SWI Prolog website](https://www.swi-prolog.org/Download.html).
In any case you should make sure that the SWI Prolog executable `swipl` is in your `$PATH`.

The actual Aleph port is provided on [GitHub](https://github.com/friguzzi/aleph) or on the [SWI Prolog packs repository](https://www.swi-prolog.org/pack/list?p=aleph).
To install Aleph run either

```
?- pack_install('https://github.com/friguzzi/aleph.git').
```

or

```
?- pack_install(aleph).
```
Afterwards Aleph should be loadable in an interactive Prolog session like so:
```
?- use_module(library(aleph)).
true.
```
