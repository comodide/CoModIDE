package com.comodide.patterns;

import org.semanticweb.owlapi.model.IRI;

public class Category {

	private String label;
	private IRI iri;
	
	public Category(String label, IRI iri) {
		super();
		this.label = label;
		this.iri = iri;
	}

	public String getLabel() {
		return label;
	}

	public IRI getIri() {
		return iri;
	}
	
	public String toString() {
		return label;
	}
}
