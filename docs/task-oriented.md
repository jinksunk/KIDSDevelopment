The KIDS Framework How-To
========

This system is intended to be extensible and customizable, but it is a complex interconnected set of classes and concepts as well. This document is an attempt to identify the most common tasks that users and contributors might want to perform and provide step-by-step guidance on how to complete those tasks.

This document assumes familiarity with the concepts in the S-MAIDs system to start with. Those concepts are explained in the ontology guide at docs/ontology.md.

The following tasks are covered:

* Add support for a resource / signal domain
* Add support for a detector
* Add support for a new constraint language
* Add support for a new signal domain context
* Add support for a response
* Add support for a correlation relation
* Add support for a new data view type

Support for a resource / signal domain
------------------
Adding support for a resource / signal domain includes:

** Java Code **
The java code that needs to be updated / modified includes:

** Ontology Updates **
The ontology updates required include:

1. Define the resource in the ontology (create a class and individual)
2. Create subclasses of Signal to associate the signal domain with the constraint languages that can be applied to it
3. Add subclass definitions to SignalDomainContext subclasses to define the contexts that provide the resource
4. Add subclass definitionns of SignalDomainRepresentation to associate the domain with its representation in each context
5. Identify the responses that can use / require the resource and update the corresponding subclass definitions
6. Update the DetectorSyntax subclass definitions where they can support the new representation

Support for a detector
------------------
A detector is also a generator of a dataset view; however, often a single type of executable (e.g. Snort, tcpdump, or grep) is capable, given various arguments, of producing different views. Adding a detector to the java code is therefore slightly different, as it entails adding support for a group or class of views, whereas adding the concept of the detector to the ontology is more along the lines of adding support for a generator of a specific view.

** Java Code **
The java code that needs to be updated / modified includes:

1. Create an abstract parent class for the detector that handles the common elements of executing the generator, but allows subclasses to define view-specific arguments.


** Ontology Updates **
The ontology updates required include:

1. Define the detector by creating a subclass of 'Detector', with the implementation class and syntax as part of the definition.
2. Define subclass defintions of the deetector class that indicate the manifestations the detector is able to see.


Support for a detector syntax
------------------
A detector syntax provides the machinery to convert a specified set of signals into a specification format that can be applied to a detector.

** Java Code **
The java code that needs to be updated / modified includes:
1. Define an implementation of net.strasnet.kids.detectorsyntaxproducers.KIDSDetectorSyntax that will, given a set of signals, produce a specification in the desired syntax.

** Ontology Updates **
The ontology updates required include:

1. Define the syntax by adding a subclass of Detector Syntax and an individual as a member of that subclass.
2. Add subclass definitions for the syntax indicating the SignalDomainRepresentations it supports (NEEDS VERIFICATION)
3. Add subclass definitions to the SignalValue subclasses to identify which of them are representable in this syntax.
4. Add subclass definitions to the Signal subclasses to identify which of them are expressible in this syntax.
5. Add subclass definitions to the detector classes that use this syntax


Support for a new signal constraint language
------------------
** Java Code **
The java code that needs to be updated / modified includes:

** Ontology Updates **
The ontology updates required include:

1. Define the constraint by adding a subclass of SignalConstraint and an individual as a member of that subclass.
2. Define a subclass of Signal for each valid constraint + signal domain combination.


Support for a new signal domain context
------------------
** Java Code **
The java code that needs to be updated / modified includes:

** Ontology Updates **
The ontology updates required include:

1. Define the signal domain context by adding a subclass of SignalDomainContext, and a representative instance of that subclass
2. Add subclass definitions to identify the resources this signal domain context provides (isContextOfSignalDomain)
3. Add to signal domain representation class defintions where the resource representation is produced in this context
4. Add to subclass definitions of DatasetViews where the view exposes this context.


Support for a new data view type
------------------
The actual implementing class for all datasets in KIDS is the ViewLabelDataset. This makes the View and it's associate label function tightly coupled.

The actual dataset view is produced by a detector, and that is where the bulk of the Java coding resides.

** Java Code **
The java code that needs to be updated / modified includes:

1. Write a detector implementation class to parse the instances in a dataset and extract the specified resources into a set of view-specific new.strasnet.kids.measurement.datasetinstances.AbstractDataInstance instances. The view is produced by invoking the detector with an empty set of signals to apply as a filter.


** Ontology Updates **
The ontology updates required include:

1. Add both a dataset view implementation subclass (with implementation-based definition) and a conceptual subclass, defined by the detector-source + contexts exposed. Add a representative instance to the defined subclasses.
2. Add subclass deifnitions to implementation subclass for the correlation relations supported by the view
3. Add subclass definitions to the conceptual subclass specifying the contexts exposed by this view
4. Add relations to the individual identifying the representation + dataset manifestations this view brings into existence.

Support for a new dataset label type
------------------
** Java Code **
The java code that needs to be updated / modified includes:

** Ontology Updates **
The ontology updates required include:

1. Add a subclass definition of DatasetLabel that includes the java labelFunction class as part of the definition.
2. Add subclass definition that identifies the dataset view this is a label for as a subclass definition. 
