package com.comodide.editor.model;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import com.comodide.rendering.editor.SDConstants;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;

public class ClassCell extends mxCell {

	private static final long serialVersionUID = -7150369287823549102L;
	
	private static final int WIDTH = 75;
	private static final int HEIGHT = 30;
	private static final String STYLE = SDConstants.classStyle;
	private static final ShortFormProvider shortFormProvider = new SimpleShortFormProvider();
	
	public ClassCell() {
		this(OWLManager.getOWLDataFactory().getOWLEntity(EntityType.CLASS, OWLRDFVocabulary.OWL_CLASS.getIRI()),
				0.0, 0.0);
	}
	
	public ClassCell(OWLEntity owlEntity, double positionX, double positionY) {
		this.value = owlEntity;
		
		this.geometry = new mxGeometry(positionX, positionY, WIDTH, HEIGHT);
		this.geometry.setRelative(false);
		
		this.id = shortFormProvider.getShortForm(owlEntity);
		
		this.style = STYLE;
		
		this.vertex = true;
		this.connectable = true;
	}

	@Override
	public OWLEntity getValue() {
		return (OWLEntity)this.value;
	}
}
