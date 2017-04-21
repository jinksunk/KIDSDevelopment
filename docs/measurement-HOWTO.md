Measurement with S-MAIDS - HOWTO
================================

The main purpose of S-MAIDS is to allow measurement of detectors across data domains and considering possible correlation between them. This HOWTO walks through the process of setting up and executing such an experiment using some sample data from DARPA.

Overview
--------

**Data Requirements**

In order to execute a test successfully, some assumptions about the data must be true:

1. All data sources used for the test must include an overlapping time period.
2. Data must be in a format that is parseable by modules in S-MAIDS (or those modules need to be added).
3. For the event(s) of interest, all data instances related to those events must be labelled uniquely (in a single dataset) in a label file (also parsable by label reader modules in S-MAIDS)
4. It is assumed that each data instance is timestamped, and that the timestamps are synchronized across data files.

**Ontology Concepts**

This section gives a brief description of the core ontology concepts and how they relate to the test execution. Each ontology element in the A-BOX is defined, the explicit properties required are listed, and an example of the real-world interpretation is given.

Note: this section *only* covers the ontology elements needed for the A-BOX, and assumes the T-BOX already contains the complete set of axioms needed to support the experiment. For more information, see the ontology.md document.

-----

* *IDS* An IDS instance represents an actual IDS executable implementation. Examples include Snort IDS, or BroIDS. An instance needs the following explicitly defined:

    * *[OP] IDS Can Execute Detectorn* -> the IDS is able to execute the 
indicated detector. This should be inferred by the class definition of the
IDS.

    * *[DP] detectorExecutionCommand* -> The path to the executable which launches the detector on the system the tests will be run on.

**Example:** A Snort IDS installation

-----

* *Detector Instance* A detector instance represents a filter, expressed in a 
specific syntax, that is able to produce sets of instances according to a 
DatasetView.

    * *[OP] Detector Can See SignalManifestation* -> the manifestation individual from the TBOX which represents the view and representation combinations this detector can evaluate.

    * *[OP] Detector Has Syntax DetectorSyntax* -> the detector syntax individual from the TBOX which represents the specific syntax version/definition that the detector uses. This impacts both how to produce signal specifications and what types of signals can be represented by the detector.

    * *[DP] hasImplementationClass* -> a package reference to the java class which manages the detector. This class is responsible for executing and processing the results produced by the detector.

**Example:** A Snort Rule

-----

* *Dataset Instance* A dataset instance represents a corpus of data instances which will be included in the evaluation of a detector. 

    * *[DP] datasetParserImplementation* -> a package reference to the java class which is able to parse the dataset. Generally should point to `net.strasnet.kids.measurement.ViewLabelDataset`
    
    * *[DP] datasetLocation* -> A file path pointing to the location of the dataset.
    
**Example:** A libpcap file

-----

* *Dataset View Instance* An instance that represents a particular view of a given dataset. A dataset might be a pcap file; 

    * *[OP] DatasetView Provides View Of Dataset* -> The dataset this provides a view of.

**Example:**  The output of the command `tcpdump -nn -l -r <pcap file>`

-----

* *DatasetLabel Instance* An instance that represents a label file which identifies event-related data instances in a given dataset. 

    * *[OP] Dataset Label Is Label For Event* -> The event(s) that are identified in the given label file.
    
    * *[DP] hasLabelFunction* -> A java class path identifying the class which can parse and instantiate the label file.
    
    * *[DP] hasLabelDataLocation* -> A file path pointing to the location of the label file.
    
**Example:** A file on disk which identifies events for a specific dataset

-----

* *TimePeriod Instance* An instance which represents the time period in each dataset to evaluate the IDRS over. The time period is assumed to represent an intersection of the time ranges from each file. 

    * *[OP] TimePeriod Coincides with Dataset Label* -> Dataset Label instance(s) which include the represented time period.

**Example:** Feb. 17th, 2015 from midnight to 11:59 PM CST

-----

* *Event Instance* An instance which represents the event we are trying to detect.

    * *[OP] Event Is Producer of Signal* -> The Signal(s) the event produces when it occurs in the environment.

**Example:** A CodeRed propagation attempt.

-----

* *Signal Value Instance* Instances which represent specific signal values.

    * *[DP] hasValue* -> The actual value of the signal which is given as a constraint definition over the domain.

**Example:** The string: "/default.ida"

-----

* *Signal Instance* Instances which represent the signals produced by an Event. A signal has a value (SignalValue), exists within an observation domain (SignalDomain), and is expressed through a constraint language (SignalConstraint)

    * *[OP] Signal Has SignalDomain* -> The field or observation domain in which the signal is expressed.
    * *[OP] Signal Has Signal Value* -> The value of the signal, parameter for the signal constraint.
    * *[OP] Signal Has Constraint SignalConstraint* -> The constraint language that defines the signal.

**Example:** Matching the string "/default.ida" in an HTTP Get Request packet.

Data Preparation
----------------
In order for automated testing to occur, there are some data preparation steps necessary to ensure that detectors see all data in a sufficient overlapping time range, and that labeled instances are properly identified.

Ontology Population
-------------------
In this section we walk through the required elements for a measurement run, and how to ensure they are defined correctly to support general inference.

Configuration and Execution
---------------------------
** The S-MAIDS Configuration File **

** Executing a Test **

** Interpreting the Results **
