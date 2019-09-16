package com.comodide.editor.model;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

import com.comodide.rendering.editor.SDConstants;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;

public class DatatypeCell extends mxCell {

	private static final long serialVersionUID = 2351081035519057130L;

	private static final int WIDTH = 75;
	private static final int HEIGHT = 30;
	private static final String STYLE = SDConstants.datatypeStyle;
	private static final ShortFormProvider shortFormProvider = new SimpleShortFormProvider();
	
	public DatatypeCell(OWLEntity owlEntity, double positionX, double positionY) {
		this.id = shortFormProvider.getShortForm(owlEntity);
		
		this.value = owlEntity;
		
		this.geometry = new mxGeometry(positionX, positionY, WIDTH, HEIGHT);
		this.geometry.setRelative(false);
		
		this.style = STYLE;
		
		this.vertex = true;
		this.connectable = true;
	}
	
	@Override
	public OWLEntity getValue() {
		return (OWLEntity)this.value;
	}
}
