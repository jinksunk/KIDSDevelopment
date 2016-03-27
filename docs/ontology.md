Working with the S-MAIDS Ontology
========

The S-MAIDS ontology provides the semantic structure required to model detectors, events, responses, and related elements of an (Intrusion Detection and Response System) IDRS deployment.
The ontology is expressed in the Web Ontology Language (OWL), and was developed using the Protege ontology editor. Many features of OWL are used to allow inference engines, in particular the Pellet reasoner, to build a complete model from minimal information.

This document describes the concepts, relations, and rules present in the ontology, and gives examples of how to add, modify, and test changes to ensure that relevant components are accurate described.


Ontology Structure
------------------

The ontology is divided into two main components: the T-BOX (terminology), and the A-BOX (assertion). The T-BOX is where the structural components are defined, roughly analagous to the schema in a relational database. It defines the classess, relations, and instances which are required to model an IDRS deployment in *any* scenario, regardless of the application. In contrast, the A-BOX is where application specific components are expressed. Examples of axioms present in each include:

<table border=1>
    <tr>
        <th> Axiom </th>
        <th> Type </th>
        <th> Component </th>
    </tr>
    <tr>
        <td> Class Hierarchy </td>
        <td> Class Definition </td>
        <td> T-BOX </td>
    </tr>
    <tr>
        <td> Object Property Hierarchy </td>
        <td> Object Property Definition </td>
        <td> T-BOX </td>
    </tr>
    <tr>
        <td> Correlation Implementations </td>
        <td> Correlation Relation</td>
        <td> T-BOX </td>
    </tr>
    <tr>
        <td> Dataset Classes </td>
        <td> Class Definition (Dataset Subclasses) </td>
        <td> T-BOX </td>
    </tr>
    <tr>
        <td> Dataset Classes </td>
        <td> Class Definition (Dataset Subclasses) </td>
        <td> T-BOX </td>
    </tr>
    <tr>
        <td> Dataset View Classes </td>
        <td> Class Definition (DatasetView Subclasses) </td>
        <td> T-BOX </td>
    </tr>
    <tr>
        <td> Detector Classes </td>
        <td> Class Definition (Detector Subclasses) </td>
        <td> T-BOX </td>
    </tr>
    <tr>
        <td> Detector Syntax Classes </td>
        <td> Class Definition (DetectorSyntax Subclasses) </td>
        <td> T-BOX </td>
    </tr>
    <tr>
        <td> Event Classes </td>
        <td> Class Definition (Event Subclasses) </td>
        <td> T-BOX </td>
    </tr>
    <tr>
        <td> IDS Classes </td>
        <td> Class Definition (IDS Subclasses) </td>
        <td> T-BOX </td>
    </tr>
    <tr>
        <td> IRS Classes </td>
        <td> Class Definition (IRS Subclasses) </td>
        <td> T-BOX </td>
    </tr>
    <tr>
        <td> Resource Classes </td>
        <td> Class Definition (Resource Subclasses) </td>
        <td> T-BOX </td>
    </tr>
    <tr>
        <td> Signal Domain Classes </td>
        <td> Class Definition (Signal Domain Subclasses) </td>
        <td> T-BOX </td>
    </tr>
    <tr>
        <td> Signal Domain Implementations </td>
        <td> Instances of Signal Domain Subclasses </td>
        <td> T-BOX </td>
    </tr>
    <tr>
        <td> Response Classes </td>
        <td> Class Definition (Response Subclasses) </td>
        <td> T-BOX </td>
    </tr>
    <tr>
        <td> Signal Classes </td>
        <td> Class Definition (Signal Subclasses) </td>
        <td> T-BOX </td>
    </tr>
    <tr>
        <td> Signal Constraint Classes </td>
        <td> Class Definition (SignalConstraint Subclasses) </td>
        <td> T-BOX </td>
    </tr>
    <tr>
        <td> Supported Signal Constraints </td>
        <td> Instances of SignalConstraint Subclasses </td>
        <td> T-BOX </td>
    </tr>
    <tr>
        <td> Signal Domain Context Classes </td>
        <td> Class Definition (SignalDomainContext Subclasses) </td>
        <td> T-BOX </td>
    </tr>
    <tr>
        <td> Supported Signal Domain Context Instances </td>
        <td> Instances of SignalDomainContext Subclasses</td>
        <td> T-BOX </td>
    </tr>
    <tr>
        <td> Signal Domain Representation Classes </td>
        <td> Class Definition (SignalDomainRepresentation Subclasses)</td>
        <td> T-BOX </td>
    </tr>
    <tr>
        <td> Signal Domain Representation Instances </td>
        <td> Instances of SignalDomainRepresentation Subclasses</td>
        <td> T-BOX </td>
    </tr>
    <tr>
        <td> Signal Manifestation Classes </td>
        <td> Class Definition (SignalManifestation Subclasses)</td>
        <td> T-BOX </td>
    </tr>
    <tr>
        <td> Signal Value Classes </td>
        <td> Class Definition (SignalValue Subclasses)</td>
        <td> T-BOX </td>
    </tr>
</table>

Key Inferences
--------------
The inferences in S-MAIDS are enabled through several OWL constructs: Class equivalence and subclass definitions, property restrictions, property chains, and rules. Each are discussed below, and examples of the design goals are provided.

**Class Definitions**

**Property Restrictions**

**Property Chains**

**Rules**