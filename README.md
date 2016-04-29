# SML-Bench

**Description**

SML-Bench (Structured Machine Learning Benchmark) is a benchmark for machine learning from structured data. It provides datasets, which contain structured knowledge (beyond plain feature vectors) in languages such as the Web Ontology Language (OWL) or the logic programming language Prolog. For those datasets, SML-Bench defines a number of machine learning tasks, e.g. the prediction of diseases. 

**Mission**

The ultimate goal of SML-Bench is to foster research in machine learning from structured data as well as increase the reproducibility and comparability of algorithms in that area. This is important, since a) the preparation of machine learning tasks in that area involves a significant amount of work and b) there are hardly any cross comparisions across languages as this requires data conversion processes.

**Requirements**

For Golem:
- golem binary in PATH or `learningsystems/golem/Linux-x86_64`
- SWI Prolog as `swipl`
- Python 2 as `python`

For Aleph:
- yap in PATH or `learningsystems/aleph/Linux-x86_64`
- aleph.pl in `learningsystems/aleph`
- Python 2 as `python`

For DL-Learner:
- Version 1.3-SNAPSHOT or higher

**Supported Tools & Adding your own Tool**

An overview of the currently supported tools and a brief description of how to add additional tools is given [here](learningsystems/README.md).

Quick intro for running the base framework is given in [Getting Started](GettingStarted.md) document.
