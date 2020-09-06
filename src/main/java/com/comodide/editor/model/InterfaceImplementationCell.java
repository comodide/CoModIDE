package com.comodide.editor.model;

import org.semanticweb.owlapi.model.OWLEntity;

import com.comodide.editor.SDConstants;

public class InterfaceImplementationCell extends ClassCell {

	// The interface implementation cell will look like a normal class cell
	private static final String STYLE = SDConstants.classStyle;
	
	private static final long serialVersionUID = -8116238798134844439L;

	
	public InterfaceImplementationCell(OWLEntity owlEntity, double positionX, double positionY) {
		super(owlEntity, positionX, positionY);
		
		this.style = STYLE;
	}
}
