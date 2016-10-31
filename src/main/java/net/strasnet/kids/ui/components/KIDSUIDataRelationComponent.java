/**
 * 
 */
package net.strasnet.kids.ui.components;

import org.semanticweb.owlapi.model.IRI;

import net.strasnet.kids.ui.components.KIDSUIAbstractComponent.KIDSDatatypeClass;

/**
 * @author cstras
 *
 */
public class KIDSUIDataRelationComponent extends KIDSUIAbstractRelation implements KIDSUIRelation {
	
	KIDSDatatypeClass mytype;

	public KIDSUIDataRelationComponent(IRI subjectIRI, IRI relationIRI, KIDSDatatypeClass dc) {
		super(subjectIRI, relationIRI, KIDSUIRelation.RelationType.Data);
		mytype = dc;
	}
	
	public KIDSDatatypeClass getDatatypeClass(){
		return mytype;
	}

}
