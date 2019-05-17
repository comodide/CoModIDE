package com.comodide.patterns;

import java.util.Optional;

import org.semanticweb.owlapi.model.IRI;

/**
 * POJO representing an OPLa pattern.
 * @author Karl Hammar <karl@karlhammar.com>
 *
 */
public class Pattern implements Comparable<Pattern> {
	private String label;
	private IRI iri;
	private String schemaDiagramPath;
	private String htmlDocumentation;
	private String owlRepresentationPath;
	
	public Pattern(String label, IRI iri, String owlRepresentationPath) {
		super();
		this.label = label;
		this.iri = iri;
		this.owlRepresentationPath = owlRepresentationPath;
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

	public Optional<String> getSchemaDiagramPath() {
		return Optional.ofNullable(schemaDiagramPath);
	}

	public void setSchemaDiagramPath(String renderedSchemaDiagram) {
		this.schemaDiagramPath = renderedSchemaDiagram;
	}

	public Optional<String> getHtmlDocumentation() {
		return Optional.ofNullable(htmlDocumentation);
	}

	public void setHtmlDocumentation(String htmlDocumentation) {
		this.htmlDocumentation = htmlDocumentation;
	}

	public String getOwlRepresentationPath() {
		return owlRepresentationPath;
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
		Pattern other = (Pattern) obj;
		if (iri == null) {
			if (other.iri != null)
				return false;
		} else if (!iri.equals(other.iri))
			return false;
		return true;
	}

	@Override
	public int compareTo(Pattern other) {
		return label.compareTo(other.label);
	}
}
