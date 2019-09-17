package com.comodide.editor.model;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

public class WrappedOWLEntity {

	private final OWLEntity entity;
	private static final ShortFormProvider shortFormProvider = new SimpleShortFormProvider();
	
	public WrappedOWLEntity(OWLEntity entity) {
		this.entity = entity;
	}
	
	public String toString() {
		return shortFormProvider.getShortForm(this.entity);
	}
	
	public OWLEntity getEntity() {
		return this.entity;
	}
}
