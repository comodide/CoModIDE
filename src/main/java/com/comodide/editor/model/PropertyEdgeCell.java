package com.comodide.editor.model;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import com.comodide.rendering.editor.SDConstants;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;

public class PropertyEdgeCell extends mxCell {

	private static final long serialVersionUID = -8498156089004202454L;

	private static final String STYLE = SDConstants.standardEdgeStyle;
	private static final ShortFormProvider shortFormProvider = new SimpleShortFormProvider();
	
	public PropertyEdgeCell() {
		this(OWLManager.getOWLDataFactory().getOWLEntity(EntityType.OBJECT_PROPERTY, OWLRDFVocabulary.OWL_OBJECT_PROPERTY.getIRI()));
	}
	
	public PropertyEdgeCell(OWLProperty owlProperty) {
		this.id = shortFormProvider.getShortForm(owlProperty);
		
		this.value = owlProperty;
		
		this.geometry = new mxGeometry();
		this.geometry.setRelative(true);
		
		this.style = STYLE;
		this.setEdge(true);
	}
	
	@Override
	public OWLProperty getValue() {
		return (OWLProperty)this.value;
	}
	
	public boolean isNamed() {
		return !this.getValue().getIRI().toString().equalsIgnoreCase(OWLRDFVocabulary.OWL_OBJECT_PROPERTY.getIRI().toString());
	}
}
