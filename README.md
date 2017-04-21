S-MAIDS (Semantic Model for Assessing Intrusion Detection Systems) [aka KIDS - Knowledge-based Intrusion Detection System]
===============

Code, ontology for signal-based IDRS modeling. 

This project implements a model of Intrusion Detection and Response Systems (IDRS) which can be used to represent detectors, attacks, and responses/mitigations in an environment specific way. In addition to the model, code is included which will assess detectors using an information-theory based measure called the Effectiveness of Intrusion Detection (E_{ID}). 

See the following works for more information on the model and measure:
*Add References*

The quick start section provides some high-level steps required to get things up and running with 
test data. More details are available below, and in the docs/ directory.

Quick Start
-----------

1. Install prerequisites (see below)
2. Checkout the latest version of S-MAIDs from GitHub
3. Execute self-tests; resolve issues
4. Define a model including attacks (events), detectors, and responses

Installation
------------

**Prerequisites**

* Java version
* Antlr
* Pellet
* OWL-API

**Installing S-MAIDS**

**Running Tests**

For Users
---------

This section covers basic details about the interface to the KIDS/S-MAIDS system. Interface points include:

* The CLI entry point to perform an assessment of an IDS profile;
* The CLI interface script to assist in building a model;
* Details on interacting with external data sources, e.g. CVE, Exploit DB, etc...

For Developers
--------------
The developers section provides more detail on how to get started modifying both the ontology and the code to support additional detectors, responses, and diagnose problems.

**Ontology**

*Inferred Property Summary*
Some of the inferred properties can be tricky to setup.  Examples of the required elements to support inference are provided here.

*Detector Can Apply Signal*
Detector hasSyntax DetectorSyntax -- class membership equivalence, set in TBOX
DetectorSyntax Can Represent Feature With Constraint Signal -- inverse class membership equivalence, set on Signal class

**Java Code**

*Package Structure*

`net.strasnet.kids` - The base package. 

This was some of the earliest code, and is generally related to representing ontology elements in java to support ingest from parsing Snort rules. Used by the `net.strasnet.kids.gui` code.

Classes in this package include basic representations of concepts from the ontology model (e.g. KIDSAbstractContext), common library code (e.g. KIDSOracle) for working with aspects of the model, and specific implementations of features used to ingest data from various sources (e.g. the KIDSIPDataFeature from Snort).

`net.strasnet.kids.bro` - This package contains classes which support presenting data from signals in the ontology (which are all represented as text values) as the proper type to provide to bro-ids scripts. 

`net.strasnet.kids.constraint` - Provides java implementations of specific constraint types from the ontology. This is used primarily to perform operations on those constraint types in java such as iterating over the integer ranges in a set of integer ranges, or providing the start and end values of a range.

`net.strasnet.kids.datasources` - This package contains the code which allows S-MAIDs to parse a Snort rule and extract signals related to a specific event from it. It includes an Antlr grammar file and supporting classes to work with specific data types.

`net.strasnet.kids.detectors` - This package represents the various detectors implemented by S-MAIDs. To be implemented, the detector code must be capable of running the detector over a compatible set of signals, and also parse the results. Each class **should**:

* Define the version(s) of detector it is written to work with;
* Extend the KIDSAbstractDetector class;

`net.strasnet.kids.detectorsyntaxproducers` - This package contains java classes which interface with detectors to produce signal representations in detector-specific syntax. For example, given a set of signals, the BroEventDetectorSyntax class will attempt to represent the signals as a Bro-IDS script.

`net.strasnet.kids.gui` - This package was an early GUI to allow users to more easily build ABOXes, defining the signals, signal contexts, signal domains, events, and detectors, and adding the required relations between them. This code is deprecated and likely no longer works at all. It will be removed in a future release.

`net.strasnet.kids.lib` - This package just provides some common utilities used throughout S-MAIDs including IPv4Address methods and general Range operations.

`net.strasnet.kids.measurement` - The measurement package contains sub-packages that support assessment over the set of signals, events, detectors, and responses within an ABOX.

`net.strasnet.kids.resources` - The resources package provides classes that represent resources, that is, all signal domain values for any signal context, within the ontology. Resources are primarily used to support response selection, but may also support efforts to identify other signal values correlated with specific events in an environment.

`net.strasnet.kids.responses` - This package implements specific responses available for deployment in an environment. **CAUTION** - If supported, the S-MAIDs code can trigger actual responses, possibly causing network disruption etc... 

`net.strasnet.kids.signalRepresentations` - Signal representations in S-MAIDs refer to specific ways a signal value can be instantiated. For example, a dotted-quad IPv4 address vs. a 32-bit integer value.

`net.strasnet.kids.snort` - This package contains code which, given a set of signal values, will produce a snort rule that matches that set of signal values. It is used to dynamically build snort rules from signals associated with an event in the ABOX.

`net.strasnet.kids.nfa` - The NFA package provides an implementation of a non-deterministic finite automata. The purpose of this implementation is to support the regular expression syntax used in Snort rules (which differs from the semantics of the Java regex syntax) to reconstitute signals. It is used (the GNFA class) by the net.strasnet.kids.snort.SnortRuleContentComponent class.

`net.strasnet.kids.ui` - The UI components for KIDS - main classes, GUI entry points are under this package.