package com.comodide.editor.model;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import com.mxgraph.model.mxCell;

public abstract class ComodideCell extends mxCell {

	private static final long serialVersionUID = 8089893998863898138L;	

	public ComodideCell(OWLEntity owlEntity) {
		this.id = owlEntity.toString();
		this.value = new WrappedOWLEntity(owlEntity);
	}

	@Override
	public WrappedOWLEntity getValue() {
		return (WrappedOWLEntity)this.value;
	}
	
	public OWLEntity getOWLEntity() {
		return this.getValue().getEntity();
	}
	
	public boolean isNamed() {
		return !this.getValue().getEntity().getIRI().toString().equalsIgnoreCase(this.defaultIRI().toString());
	}
	
	public abstract IRI defaultIRI();
	
}
