package com.comodide.patterns;

import org.semanticweb.owlapi.model.IRI;

public class Pattern {
	private String label;
	private IRI iri;
	
	public Pattern(String label, IRI iri) {
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
