package com.comodide.editor.model;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import com.comodide.editor.SDConstants;
import com.mxgraph.model.mxGeometry;

public class DatatypeCell extends ComodideCell {

	private static final long serialVersionUID = 2351081035519057130L;

	private static final int WIDTH = 75;
	private static final int HEIGHT = 30;
	private static final String STYLE = SDConstants.datatypeStyle;
	private static IRI DEFAULT_IRI =  OWLRDFVocabulary.OWL_DATATYPE.getIRI();
	private static EntityType<OWLDatatype> DEFAULT_TYPE = EntityType.DATATYPE;
	
	public DatatypeCell() {
		super(OWLManager.getOWLDataFactory().getOWLEntity(DEFAULT_TYPE, DEFAULT_IRI));
	}
	
	public DatatypeCell(OWLEntity owlEntity, double positionX, double positionY) {
		super(owlEntity);
		
		this.geometry = new mxGeometry(positionX, positionY, WIDTH, HEIGHT);
		this.geometry.setRelative(false);
		
		this.style = STYLE;
		
		this.vertex = true;
		this.connectable = true;
	}

	@Override
	public IRI defaultIRI() {
		return DEFAULT_IRI;
	}
}
