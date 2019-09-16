package com.comodide.editor.model;

import org.semanticweb.owlapi.model.OWLProperty;

import com.comodide.rendering.editor.SDConstants;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;

public class PropertyEdgeCell extends mxCell {

	private static final long serialVersionUID = -8498156089004202454L;

	private static final String STYLE = SDConstants.standardEdgeStyle;
	
	public PropertyEdgeCell(OWLProperty owlProperty) {
		this.value = owlProperty;
		
		this.geometry = new mxGeometry();
		this.geometry.setRelative(true);
		
		this.id = "subClassOf";
		
		this.style = STYLE;
		this.setEdge(true);
	}
	
	@Override
	public OWLProperty getValue() {
		return (OWLProperty)this.value;
	}
}
