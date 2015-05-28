## Cluster labelling code

The code is written in scala and uses the Scala Build Tool (SBT) for building
and managing dependencies.

### To build:
```
$ sbt compile
```

### To load data in to redis
```
sbt 'run parse -f clusterdump.txt'
```

### To print cluster labels
```
$ sbt 'run --help'
[info] Set current project to clustering (in build file:/Users/felix/EDAN70/code/)
[info] Running Labels --help
labels 0.1
Usage: labels [parse|labels] [options]

Command: parse [options]
Parse clusterdump file.
  -f <file> | --file <file>
        path to clusterdump file
Command: labels [options]
Compute cluster labels.
  -n <value> | --num_labels <value>
        number of labels to print
  -c <value> | --cluster <value>
        cluster to calculate labels for
  -a <value> | --artist <value>
        calculate labels for cluster which includes artist
  --help
        prints this usage text
```

Print the top 3 labels for all clusters:
```
$ sbt 'run labels -n 3'
[info] Set current project to clustering (in build file:/Users/felix/EDAN70/code/)
[info] Running Labels labels -n 3
VL-14677
--------------------------------
country              | country
My Country           | Hawaiian
My Country Selection | Audiobook
--------------------------------
[...]
```

Print the top 3 labels for the cluster a specific artist is assigned to:
```
$ sbt 'run labels --artist "Bob Dylan" -n 3'
[info] Set current project to clustering (in build file:/Users/felix/EDAN70/code/)
[info] Running Labels labels --artist Bob Dylan -n 3
VL-3601
-------------------------------------
singer-songwriter | singer-songwriter
folk              | folk
acoustic          | acoustic
-------------------------------------
[success] Total time: 3 s, completed May 31, 2015 4:13:16 AM
```
Print the top 3 labels for a specific cluster:
```
$ sbt 'run labels --cluster "VL-17400" -n 3'
[info] Set current project to clustering (in build file:/Users/felix/EDAN70/code/)
[info] Running Labels labels --cluster VL-17400 -n 3
VL-17400
-------------------------
Garage Rock | Garage Rock
garage      | garage
Garage Punk | Garage Punk
-------------------------
[success] Total time: 2 s, completed May 31, 2015 4:18:26 AM
```
