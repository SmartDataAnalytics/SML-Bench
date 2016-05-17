# FuncLog

## Description

FuncLog is part of the [_General Inductive Logic Programming System_ (GILPS)](http://www.doc.ic.ac.uk/~jcs06/GILPS/)
developed by [José Carlos Almeida Santos](http://www.doc.ic.ac.uk/~jcs06/).
GILPS is written in Prolog and requires at least [YAP 6.0](http://www.dcc.fc.up.pt/~vsc/Yap/downloads.html).
FuncLog was designed to learn on [Head Output Connected learning problems](http://www.doc.ic.ac.uk/~shm/Papers/EPIA09.pdf).

## Installation

To make use of the GILPS an installed [YAP](http://www.dcc.fc.up.pt/~vsc/Yap/downloads.html) system is required.
There are YAP software packages for the most common Linux distributions as well als binary and source distributions
on the [YAP web page](http://www.dcc.fc.up.pt/~vsc/Yap/downloads.html). In any case you should make sure that the YAP
executable is in your `$PATH`.

Having installed YAP, the actual GILPS can be downloaded at http://www.doc.ic.ac.uk/~jcs06/GILPS/GILPS.tar.bz2 .
The tar archive contains the GILPS Prolog sources which need to be extracted to `SML-Bench/learningsystems/funclog`.
The directory should then contain at least the following files:

```bash
SML-Bench/learningsystems/funclog $ ls -F
funclog.patch  Makefile  README.md  run*  source/  validate*
```

To make FuncLog work correctly you will first have to apply the patch file `funclog.patch` (residing in the 
funclog directory) as follows:

```bash
SML-Bench/learningsystems/funclog $ patch source/engine/funclog.pl funclog.patch 
patching file source/engine/funclog.pl
```

No further steps are required. In particular there is **no** need to call `make`.
