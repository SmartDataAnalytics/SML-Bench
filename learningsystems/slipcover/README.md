# SLIPCOVER

## Description

[SLIPCOVER](https://sites.google.com/a/unife.it/ml/slipcover) -- **S**tructure **L**earn**I**ng of **P**robabilistic logic programs by sear**C**hing **OVER**
the clause space is an ILP system written in Prolog that learns the structures of Logic Programs with Annotated Disjunctions (LPADs) by searching first the clause space and then the theory space. 
SLIPCOVER  is part of the [cplint](https://sites.google.com/a/unife.it/ml/cplint) suite, which contains several algorithms for inference and learning and requires an installed [SWI Prolog](http://www.swi-prolog.org/Download.html) system.

## Installation

To be able to use SLIPCOVER an installed [SWI Prolog](http://www.swi-prolog.org/Download.html) is required. SWI Prolog is available for Windows, MacOS X and several Linux distributions. 
SLIPCOVER is part of the [cplint](https://sites.google.com/a/unife.it/ml/cplint) suite. cplint is distributed as a pack of SWI-Prolog. To install it, use the following commands in the SWI Prolog shell
```
	?- pack_install(cplint).
```
Moreover, in order to make sure you have a foreign library that matches your architecture, run
```
	?- pack_rebuild(cplint).
```
<!--Finally `slipcover.pl` should be moved to `SML-Bench/learningsystem/slipcover`:
```
	cp ~/lib/swipl/pack/cplint/prolog/slipcover.pl SML-Bench/learningsystem/slipcover
```-->
