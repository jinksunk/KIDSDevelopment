package net.strasnet.kids.ui;

import org.semanticweb.owlapi.model.IRI;

import net.strasnet.kids.ui.components.KIDSUIAbstractComponent.KIDSDatatypeClass;

public class KIDSUIRequiredDataProperty extends KIDSUIConstraint {

	IRI PropertyIRI;
	KIDSDatatypeClass DatatypeClass;
	
	public KIDSUIRequiredDataProperty(IRI PropertyIRI, KIDSDatatypeClass kidsDatatypeClass){
		this.PropertyIRI = PropertyIRI;
		this.DatatypeClass = kidsDatatypeClass;
	}

	public IRI getProperty(){
		return PropertyIRI;
	}

	public KIDSDatatypeClass getObjectClass(){
		return DatatypeClass;
	}
}
