# DL-Learner

## Description

[DL-Learner](http://dl-learner.org) is a framework for supervised machine learning in OWL, RDF and Description Logics.
It comprises [several algorithms](http://www.jmlr.org/papers/volume10/lehmann09a/lehmann09a.pdf) and can be configured
to learn on different learning problems.

## Installation

To run DL-Learner in SML-Bench you have to clone the Git repository available on GitHub as follows:

```
$ git clone https://github.com/AKSW/DL-Learner/
Cloning into 'DL-Learner'...
remote: Counting objects: 91096, done.
remote: Compressing objects: 100% (54/54), done.
remote: Total 91096 (delta 14), reused 0 (delta 0), pack-reused 91017
Receiving objects: 100% (91096/91096), 244.21 MiB | 653.00 KiB/s, done.
Resolving deltas: 100% (61131/61131), done.
Checking connectivity... done.
```

Afterwards the following steps have to be performed to build the DL-Learner:

```
$ cd DL-Learner/
DL-Learner $ ./buildRelease.sh
[INFO] Scanning for projects...

[...]

[INFO] Reading assembly descriptor: src/main/assemble/archive.xml
[INFO] Building zip: /home/user/DL-Learner/interfaces/target/dllearner-1.3-SNAPSHOT.zip
[INFO] Building tar : /home/user/DL-Learner/interfaces/target/dllearner-1.3-SNAPSHOT.tar.gz
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 11.357s
[INFO] Finished at: Tue May 17 21:50:48 CEST 2016
[INFO] Final Memory: 20M/501M
[INFO] ------------------------------------------------------------------------
```

The DL-Learner tar archive `dllearner-1.3-SNAPSHOT.tar.gz` now resides in the folder
`DL-Learner/interfaces/target/` and needs to be extracted to the directory `SML-Bench/learningsystems/dllearner`
such that the directory contains at least the following:

```sh
SML-Bench/learningsystems/dllearner $ ls -F
common.sh  dllearner-1.3-SNAPSHOT/  README.md  run*  system.ini  validate*
```

The provided `run` script will now be able to find the `dllearner-1.3-SNAPSHOT/bin/cli` executable to start the DL-Learner. 
