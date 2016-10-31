/**
 * 
 */
package net.strasnet.kids.ui.components;

import java.util.List;

import org.semanticweb.owlapi.model.IRI;

/**
 * @author cstras
 *
 */
public interface KIDSUIRelation {

	enum RelationType {
		Object,
		Data
	}

	IRI getSubjectIRI();

	IRI getRelationIRI();
	
	RelationType getType();

}
