package com.comodide.editor.model;

import org.semanticweb.owlapi.model.OWLEntity;

import com.comodide.editor.SDConstants;

public class InterfaceSlotCell extends ClassCell {

	private static final String STYLE = SDConstants.interfaceSlotStyle;
	
	private static final long serialVersionUID = -8116238798134844439L;

	
	public InterfaceSlotCell(OWLEntity owlEntity, double positionX, double positionY) {
		super(owlEntity, positionX, positionY);
		
		this.style = STYLE;
	}
}
