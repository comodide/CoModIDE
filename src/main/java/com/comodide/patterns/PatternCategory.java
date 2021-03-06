package com.comodide.patterns;

import org.semanticweb.owlapi.model.IRI;

/**
 * POJO representing a category of OPLa patterns.
 * @author Karl Hammar <karl@karlhammar.com>
 *
 */
public class PatternCategory implements Comparable<PatternCategory> {

	private String label;
	private IRI iri;
	
	public PatternCategory(String label, IRI iri) {
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((iri == null) ? 0 : iri.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PatternCategory other = (PatternCategory) obj;
		if (iri == null) {
			if (other.iri != null)
				return false;
		} else if (!iri.equals(other.iri))
			return false;
		return true;
	}

	@Override
	public int compareTo(PatternCategory other) {
		return label.compareTo(other.label);
	}
}
