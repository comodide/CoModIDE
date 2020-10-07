package com.comodide.editor.model;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;

import com.comodide.editor.SDConstants;

public class InterfaceSlotCell extends ClassCell {

	private static final String STYLE = SDConstants.interfaceSlotStyle;
	
	private static final long serialVersionUID = -8116238798134844439L;

	private IRI interfaceIri;
	
	public InterfaceSlotCell(OWLEntity owlEntity, IRI interfaceIri, double positionX, double positionY) {
		super(owlEntity, positionX, positionY);
		this.interfaceIri = interfaceIri;
		this.style = STYLE;
	}
	
	public IRI getInterfaceIri() {
		return this.interfaceIri;
	}
}
