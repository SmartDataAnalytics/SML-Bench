## Compilation

run `mvn package`

A package will be built in `target` folder

## Execution

### Of Final package

run `smlbench configfile` script (provided in the `bin` folder of final built package)

### From the source tree

use `mvn -e exec:java -Dexec.mainClass=org.aksw.mlbenchmark.Benchmark -Dexec.args=configfile`

#### Config file

The config file describes the benchmark you want to run.

A sample config file is provided in [src/main/resources/test.plist](src/main/resources/test.plist)

Main features:
```
learningsystems = golem,dllearner
scenarios = hepatitis/1,premierleague/1
framework.crossValidationFolds = 5
```

- `learningsystems`: list of systems to benchmark
- `scenarios`: list of `learningtask`/`learningproblem` formatted learning problems to benchmark. you can use `*` to test all learningproblems of a given task (currently `*` is not supported for all __learningtasks__)
- `measures`: list of measures to take (fmeasure, ameasure, prec_acc)
- `framework.crossValidationFolds`: Number of folds to generate for cross validation
- `framework.threads`: number of parallel threads to use for execution (not yet implemented)
- `framework.seed`: use a fixed seed for random number generator (e.g. for fold splitting)
- `framework.maxExecutionTime`: maximum execution time in seconds which is enforced upon `run` scripts
  - default: 35 seconds
- `resultOutput`: filename wheree to store the result of the benchmark run
  - default: config file name + `.result`
- `deleteWorkDir`: if the temporary directory with all the intermediate outputs should be automatically removed after clean SML-Bench exit
  - default: false

##### Learning system config file

The learning systems can use a specific config file called `system`, see for example [learningsystems/dllearner/system.ini](learningsystems/dllearner/system.ini)

- `language`: language to use for input examples (owl, prolog)
- `families`: List of additional families config files, background knowledge bases, to load
- `configFormat`: format of configuration file passed to run script (prop, conf, plist, xml)

##### Program input config file

When executing the run scripts, important information is given in the config files passed to them:

```
data.workdir = working directory where the script can also place knowledge base etc.
learningtask = the learning task 
learningproblem = the learning problem inside the task
step = train
filename.pos = file name containing the positive examples sed for training
filename.neg = file name containing the negative examples sed for training
output = file name where the run script must place the output for the validation script
maxExecutionTime = current desired maxExecutionTime in seconds of the framework
```

for the validate script, additionally:

```
step = validate
input = file name where to read input from (as output by the run script)
output = file name where the validate script must place the output
```

##### validate output format

see [learningsystems](learningsystems)

Additional output may be optionally present in the file
