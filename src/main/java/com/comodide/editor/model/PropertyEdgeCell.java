package com.comodide.editor.model;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import com.comodide.editor.SDConstants;
import com.mxgraph.model.mxGeometry;

public class PropertyEdgeCell extends ComodideCell {

	private static final long serialVersionUID = -8498156089004202454L;

	private static final String STYLE = SDConstants.standardEdgeStyle;
	private static IRI DEFAULT_IRI =  OWLRDFVocabulary.RDF_PROPERTY.getIRI();
	private static EntityType<OWLObjectProperty> DEFAULT_TYPE = EntityType.OBJECT_PROPERTY;
	
	public PropertyEdgeCell() {
		this(OWLManager.getOWLDataFactory().getOWLEntity(DEFAULT_TYPE, DEFAULT_IRI));
	}
	
	public PropertyEdgeCell(OWLProperty owlProperty) {
		super(owlProperty);
		
		this.geometry = new mxGeometry();
		this.geometry.setRelative(true);
		
		this.style = STYLE;
		this.setEdge(true);
	}

	@Override
	public IRI defaultIRI() {
		return DEFAULT_IRI;
	}
}
