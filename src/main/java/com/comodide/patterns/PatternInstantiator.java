package com.comodide.patterns;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

public class PatternInstantiator {
	
	public static Set<OWLAxiom> getInstantiationAxioms(OWLOntology pattern) {
		//TODO: Implement this based on configuration.
		return pattern.getAxioms();
	}
	
	public static Set<OWLAxiom> getModuleAnnotationAxioms(OWLOntology pattern) {
		// TODO: Implement this based on configuration.
		return new HashSet<OWLAxiom>();
	}

}
