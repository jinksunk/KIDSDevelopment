This file describes the KIDR-SM project (Knowledge-based Intrusion Detection and Response - Semantic Model).
This project is a set of code which provides interfaces and abstractions to intrusion detection and response
related concepts as described in a description-logic based ontology.

In this file the primary tasks this package is designed to enable are described, and how to perform / extend
this framework to perform those tasks in custom environments is also detailed.  The functionality of this 
package is roughly divided into four (somewhat overlapping) classes:
1) Measurement capabilities
2) Human-interface extensions
3) Representation abstraction
4) Response actions


== Add a New Data-source ==
The ontology can be automatically populated from existing data sources by implementing parsers and/or
converters to read data from the source and populate the ontology.  This will result in
signals, features, contexts, and classes being added to the ontology / knowledge base.

The code for data sources is in the package net.strasnet.kids.datasources.

1) Create a new package for the datasource, e.g. net.strasnet.kids.datasources.snortrules
2) Create a java class which reads data from the data source and produces "canonical forms"
of the signals within the data sources.

As new datasources are added and more signal types are represented, code reuse should become
increasingly efficient.

== Add a new Signal Domain Class to the Ontology ==
There are few restrictions on adding a new Signal Domain Class to the ontology.

1) Create the class in the ontology as a subclass of SignalDomain.

== Add a new Signal Canonical Representation Class to the Ontology == 
Members of Signal Canonical Representation classes signify a restriction language in which signals can be defined.
For example, a set of ranges of bytes might be used as a representation to express a signal
over IP protocols.

Since individuals of these classes are functional, in the sense that they represent real-world
algorithms, a new class should be defined when necessary to abstract the algorithm required to 
evaluate a signal.  

To do this:
0) Implement an un-represented algorithm for defining a signal over a signal domain.
1) Create the class in the ontology which will be the container of the specific individual implementing
the new signal evaluation algorithm. (Ensure that an appropriate superclass is chosen).  
2) Create an individual as a member of the class which represents the new evaluation
algorithm.  
3) Define the datatype relation "signalCanonicalRepresentationImplementation" on the individual, pointing to 
the implementation of the algorithm (currently this must be a Java class in the current 
classpath).

== Add a new Signal Class to the Ontology ==
Each signal class is the intersection of a signal domain and a signal canonical representation.  For example,
IPProtocolNumber_ByteValue is a signal class which is the intersection of signals defined over IPProtocolNumber
and those which utilize a ByteValue canonical representation.

New signal classes should be defined only when a new signal domain or new signal canonical representation
warrant.  The definition should follow the process:

1) Create the new class in the ontology as <SigDomainClass>_<SigCanRepClass>.
2) Define an equivalent class which is the intersection of SigDomainClass and SigCanRepClass.

== Add a new Context Type to the Ontology ==
Signal domain contexts represent units of observation in a domain.  For example, IP Packets,
NetFlow records, audit log records, a system call.  Individuals represent specific types of
contexts and can be related to other contexts by the isEncapsulatedInContext relation.

To express how the context is related to signal domains, the isContextOfSignalDomain
object property is defined.  Finally, contexts provide additional information to detection and 
response systems, and this is reflected by the isProvidedBy/isProviderOfResource object properties.

While subclasses of the SignalDomainContext class are not currently used, individuals are
organized into subclasses by an intuitive notion of generality.  For instance, IPSignalDomainContext
is a subclass of NetworkSignalDomainContext, and a super-class of TCPSignalDomainContext.

Currently, new sub/super-classes may be added as desired.

Follow these steps to add a new Context Type:

1) 

== Add a new Event Syntactic Form to the Ontology ==
An EventSyntacticForm represents a specific syntax, with an implementation capable of producing
the syntax, in which a set of signals may be represented.  For instance, a specific version
of a snort rule.

An EventSyntacticForm is added by creating an individual to represent the form, providing an
implementing java class, and associating the implementation with the individual via the 
eventSyntacticFormGeneratorImplementation data property.

The following steps should be taken to add a new EventSyntacticForm:

1) 

== Add a new DataSet Class to the Ontology ==
A DataSet represents an ordered (usually temporally) sequence of contexts.  Generally
all DataSet Individuals in a single class will share a parser implementation, and thus the
specification of the parser implementation should provide an equivalent class definition.

Follow these steps to add a new DataSet class:
1) Identify the DataSet schema and format.
2) Create the class as a subclass of DataSet
3) Implement a parser implementation for the DataSet
4) Specify the parser implementation, via datasetParserImplementation value, as an equivalent class.

== Add a new DataSet Individual to the Ontology ==
To be useful,
a DataSet individual must define the source (a location IRI), the events contained within the dataset, and the 
signalDomainContexts contained within the dataset in addition to the parser implementation.

Follow these steps to add a new DataSet individual:
1) Identify a single location via which to access the DataSet
2) Create the individual as a member of the subclass of DataSet defined by the parser implementation which
can read the dataset (create this class if necessary).
3) Associate the type of events which are contained within the dataset via the hasEventData object property.
4) Specify the dataset location (as an IRI) via the datasetLocation datatype property
5) Specify the contexts within the dataset via the isContainerOfContext object relation.
