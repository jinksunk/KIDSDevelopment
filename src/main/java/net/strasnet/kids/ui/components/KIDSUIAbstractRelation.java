/**
 * 
 */
package net.strasnet.kids.ui.components;

import org.semanticweb.owlapi.model.IRI;

/**
 * @author cstras
 *
 */
public abstract class KIDSUIAbstractRelation implements KIDSUIRelation {
	
	protected IRI subjectIRI;
	protected IRI relationIRI;
	protected RelationType myType;
	
	public KIDSUIAbstractRelation(IRI subjectIRI, IRI relationIRI, RelationType myType){
		this.subjectIRI = subjectIRI;
		this.relationIRI = relationIRI;
		this.myType = myType;
	}
	
	/* (non-Javadoc)
	 * @see net.strasnet.kids.ui.components.KIDSUIRelation#getIRI()
	 */
	@Override
	public IRI getRelationIRI() {
		// TODO Auto-generated method stub
		return relationIRI;
	}

	/* (non-Javadoc)
	 * @see net.strasnet.kids.ui.components.KIDSUIRelation#getObjectClass()
	 */
	@Override
	public IRI getSubjectIRI() {
		// TODO Auto-generated method stub
		return subjectIRI;
	}

	/* (non-Javadoc)
	 * @see net.strasnet.kids.ui.components.KIDSUIRelation#getSubjectClass()
	 */
	@Override
	public RelationType getType() {
		return myType;
	}

}
