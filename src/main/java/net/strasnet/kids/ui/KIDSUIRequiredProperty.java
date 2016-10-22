package net.strasnet.kids.ui;

import org.semanticweb.owlapi.model.IRI;

public class KIDSUIRequiredProperty extends KIDSUIConstraint {

	IRI PropertyIRI;
	IRI ObjectClassIRI;
	
	public KIDSUIRequiredProperty(IRI PropertyIRI, IRI ObjectClassIRI){
		this.PropertyIRI = PropertyIRI;
		this.ObjectClassIRI = ObjectClassIRI;
	}

	public IRI getProperty(){
		return PropertyIRI;
	}

	public IRI getObjectClass(){
		return ObjectClassIRI;
	}
}
