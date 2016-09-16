# Learning Tasks

## Available Learning Tasks

The main datasets provided so far are the following:

- Carcinogenesis: Prediction of carcinogenic drugs
- Hepatitis: Prediction of the Hepatitis type based on patient data
- Lymphography: Prediction of diagnosis class based on lymphography patient data
- Mammographic: Prediction of breast cancer severity based on screening data
- Mutagenesis: Prediction of the mutagenicity of chemical compounds
- NCTRER: Prediction of a molecule’s estrogen receptor binding activity
- Premier League: Find a predictive description of goal keepers based on player statistics in soccer matches
- Pyrimidine: Prediction of the inhibition activity of pyrimidines and the DHFR enzyme
- Suramin: Find a predictive description of suramin analogues for cancer treatment

(Besides this there are further datasets mainly added for debugging purposes.)

Machine-readable descriptions of the actual datasets are provided as turtle file (`dataset.ttl`) in the corresponding `owl` and `prolog` directories. The turtle file for the Carcinogenesis OWL knowledge base resides for example at [`SML-Bench/learningtasks/carcinogenesis/owl/dataset.ttl`](carcinogenesis/owl/dataset.ttl).

## How to Add an Own Learning Task and Learning Problem

The SML-Bench benchmark runner expects learning tasks to reside under the `learningtasks` directory.
To add a new learning task, e.g. `mytask`, a folder `SML-Bench/learningtasks/mytask` needs to be created.
Additionally there should be a sub-folder for each supported KR language (currently `owl` and `prolog`).
The actual datasets in the given KR languages should then be put into the folder `SML-Bench/learningtasks/mytask/<krlanguage>/data/`.
Accordingly a Prolog representation `mydataset.pl` of our learning task should then reside in the folder `SML-Bench/learningtasks/mytask/prolog/data/mydataset.pl`.

A concrete *learning problem* defines the positive and negative examples and optional configurations for the learning systems.
Learning problems are usually put into the directories `SML-Bench/learningtasks/<learningtask>/lp/<learningproblem>`.
Given we want to define a learning problem `firstlearningproblem` for the Prolog representation of our learning task `mytask` it will reside at `SML-Bench/learningtasks/mytask/prolog/lp/firstlearningproblem`.
Inside this directory there will be files called `pos.pl` and `neg.pl` containing Prolog expressions of the positive and negative examples, respectively.
In case of a learning problem for an OWL representation the files should be named `pos.txt` and `neg.txt`.
Dedicated learning system configurations are declared in files named as the learning system with a file suffix `.conf`.
Accordingly an Aleph configuration file for the given learning problem could be found at `SML-Bench/learningtasks/mytask/prolog/lp/firstlearningproblem/aleph.conf`.


## Candidate Datasets

- DBPCAN [Rui Camacho, Ruy Ramos and Nuno Fonseca: 'AND Parallelism for ILP: the APIS system', 2013]
- Crime statistics dataset [Dalal Alrajeh, Paul Gill and Duangtida Athakravi: 'Learning Characteristics and Antecedent Behaviours of Lone-Actor Terrorists', 2014]
- SVO tensor datase [Alberto Garcia-Duran, Antoine Bordes, Nicolas Usunier: 'Effective Blending of Two and Three-way Interactions for Modeling Multi-relational data', 2014]
- Ecocyc glycolysis [Nicola Fanizzi, Claudia d’Amato, and Floriana Esposito: 'DL-FOIL: Concept Learning in Description Logics', 2008]
- Swiss Prod data [Thierry Mamer, Christopher H. Bryant, and John McCall: ' L-Modified ILP Evaluation Functions for Positive-Only Biological Grammar Learning', 2008]
- [Kristian Kersting, and Weng-Keen Wong: 'Learning with Kernels in Description Logics', 2008]
- Cactus DB [Hitoshi Yamasaki, Yosuke Sasaki, Takayoshi  Shoudai: 'Learning Block-Preserving Outerplanar Graph Patterns and Its Application to Data Mining', 2008]
- HIV data [Ashwin Srinivasan, Tanveer A. Faruquie, and Sachindra Joshi: 'Exact Data Parallel Computation for Very Large ILP Datasets', 2010]
- [Stephen H. Muggleton, Jianzhong Chen, Hiroaki Watanabe, Stuart J. Dunbar, Charles Baxter, Richard Currie, Jose Domingo Salazar, Jan Taubert and Michael J.E. Sternberg: 'Variation of background knowledge in an industrial application of ILP', 2010]
- PTC, HIA [Brahim Douar, Michel Liquiere, Chiraz Latiri, and Yahya Slimani: 'Graph-Based Relational Learning with a Polynomial Time Projection Algorithm', 2011]
- Toxicology [Dianhuan Lin et al.: 'Does Multi-Clause Learning Help in Real-World Applications?', 2011]
- Alzheimers [Cristiano Grijó Pitangui and Gerson Zaverucha: 'Learning Theories Using Estimation Distribution Algorithms and (Reduced) Bottom Clauses', 2011]
- [Sebastian Fröhler, Stefan Kramer: 'Inductive logic programming for gene regulation prediction', 2008]
- [Fabrizio Riguzzi: 'ALLPAD: approximate learning of logic programs with annotated disjunctions', 2008]
- [PTE](https://relational.fit.cvut.cz/dataset/PTE) (Relational Dataset Repository)
- [StudentLoan](https://relational.fit.cvut.cz/dataset/StudentLoan) (Relational Dataset Repository)
- [UW-CSE](https://relational.fit.cvut.cz/dataset/UW-CSE) (Relational Dataset Repository)
- [elti](https://relational.fit.cvut.cz/dataset/Elti) (Relational Dataset Repository)
- [Genes](https://relational.fit.cvut.cz/dataset/Genes) (Relational Dataset Repository)
- [NBA](https://relational.fit.cvut.cz/dataset/NBA) (Relational Dataset Repository)

