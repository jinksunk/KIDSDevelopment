/**
 * 
 */
package net.strasnet.kids.ui.components;

import org.semanticweb.owlapi.model.IRI;

/**
 * @author cstras
 *
 */
public class KIDSUIObjectRelationComponent extends KIDSUIAbstractRelation implements KIDSUIRelation {
	
	private IRI objectClass;

	public KIDSUIObjectRelationComponent(IRI subjectIRI, IRI relationIRI, IRI objectClassIRI) {
		super(subjectIRI, relationIRI, KIDSUIRelation.RelationType.Object);
		objectClass = objectClassIRI;
	}
	
	public IRI getObjectClass(){
		return objectClass;
	}


}
