# Progol

## Description

[Progol](http://www.doc.ic.ac.uk/~shm/progol.html) is an ILP tool developed by
[Stephen Muggleton](http://wp.doc.ic.ac.uk/shm/) which applies the idea of [reverse
entailment](http://www.doc.ic.ac.uk/~shm/Papers/InvEnt.pdf).
It is written in C and the source code is provided 'free of charge for academic research and teaching only.'

## Installation

To use Progol within SML-Bench download the latest source code at http://www.doc.ic.ac.uk/~shm/Software/ .
After unpacking the tar archive you should find a `source/` folder containing a make file (`Makefile`).
To compile Progol enter the `source` directory and call make:

```bash
$ cd source
source $ make
gcc  -O2    -c -o main.o main.c
main.c: In function ‘checkargs’:
main.c:85:29: warning: incompatible implicit declaration of built-in function ‘malloc’
         if (!(stack=(char *)malloc(stack_size*sizeof(char))))
                             ^
gcc  -O2    -c -o plg.o plg.c
gcc  -O2    -c -o command.o command.c
[...]
```

When the compilation finished there should be an executable file named `progol` in the `source` directory.
To test if the compilation was successful, you can execute it via

```
$ ./progol 
CProgol Version 5.0

|- 
```

A promt (`|-`) should be shown then, which can be exited pressing `Cntl` + `d`.

The `progol` executable should be copied to `SML-Bench/learningsystems/progol/` to be found by the provided `run` script.
