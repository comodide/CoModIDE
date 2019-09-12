package com.comodide.editor.model;

import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import com.comodide.rendering.editor.SDConstants;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;

public class SubClassEdgeCell extends mxCell {

	private static final long serialVersionUID = -967537018367040076L;
	
	private static final String STYLE = SDConstants.subclassEdgeStyle;
	
	public SubClassEdgeCell() {
		this.value = OWLRDFVocabulary.RDFS_SUBCLASS_OF;
		
		this.geometry = new mxGeometry();
		this.geometry.setRelative(true);
		
		this.id = "subClassOf";
		
		this.style = STYLE;
		this.setEdge(true);
	}
}
