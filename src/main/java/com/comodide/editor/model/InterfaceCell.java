package com.comodide.editor.model;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;

public abstract class InterfaceCell extends ClassCell {
	
	private static final long serialVersionUID = -3053055057345808357L;
	
	private IRI interfaceIri;
	
	public InterfaceCell(OWLEntity owlEntity, IRI interfaceIri, double positionX, double positionY) {
		super(owlEntity, positionX, positionY);
		this.interfaceIri = interfaceIri;
	}
	
	public IRI getInterfaceIri() {
		return interfaceIri;
	}
}
