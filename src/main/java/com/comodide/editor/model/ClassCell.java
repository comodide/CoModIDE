package com.comodide.editor.model;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import com.comodide.editor.SDConstants;
import com.mxgraph.model.mxGeometry;

public class ClassCell extends ComodideCell {
	
	private static final long serialVersionUID = 4458786422295695195L;
	private static final int WIDTH = 75;
	private static final int HEIGHT = 30;
	private static final String STYLE = SDConstants.classStyle;
	private static IRI DEFAULT_IRI =  OWLRDFVocabulary.OWL_CLASS.getIRI();
	private static EntityType<OWLClass> DEFAULT_TYPE = EntityType.CLASS;
	
	public ClassCell() {
		super(OWLManager.getOWLDataFactory().getOWLEntity(DEFAULT_TYPE, DEFAULT_IRI));
	}
	
	public ClassCell(OWLEntity owlEntity, double positionX, double positionY) {
		super(owlEntity);

		this.geometry = new mxGeometry(positionX, positionY, WIDTH, HEIGHT);
		this.geometry.setRelative(false);
		
		this.style = STYLE;
		
		this.vertex = true;
		this.connectable = true;
	}

	public IRI defaultIRI() {
		return DEFAULT_IRI;
	}
}
