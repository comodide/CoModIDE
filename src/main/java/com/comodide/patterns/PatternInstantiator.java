package com.comodide.patterns;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.patterns.PatternInstantiationConfiguration.EdgeCreationAxiom;

public class PatternInstantiator {
	
	private static final Logger log = LoggerFactory.getLogger(PatternInstantiator.class);
	
	private OWLOntology pattern;
	private final Boolean useTargetNamespace;
	private final Set<EdgeCreationAxiom> ecas;
	
	public PatternInstantiator(OWLOntology pattern) {
		super();
		this.pattern = pattern;
		useTargetNamespace = PatternInstantiationConfiguration.getUseTargetNamespace();
		ecas = PatternInstantiationConfiguration.getSelectedEdgeCreationAxioms();
	}

	public Set<OWLAxiom> getInstantiationAxioms() {
		log.debug(String.format("\n%s\n%s\n\n", useTargetNamespace, ecas.toString()));
		
		//TODO: Implement this based on configuration.
		return pattern.getAxioms();
	}
	
	public Set<OWLAxiom> getModuleAnnotationAxioms() {
		// TODO: Implement this based on configuration.
		return new HashSet<OWLAxiom>();
	}

}
