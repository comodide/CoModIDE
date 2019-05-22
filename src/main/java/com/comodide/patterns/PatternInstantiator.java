package com.comodide.patterns;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.entity.EntityCreationPreferences;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.OWLEntityRenamer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Documentation for the whole class
public class PatternInstantiator {
	
	private static final Logger log = LoggerFactory.getLogger(PatternInstantiator.class);
	
	private OWLOntology pattern;
	private final Boolean useTargetNamespace;
	private final IRI createdModuleIri;
	private final IRI targetOntologyIri;
	private final String entitySeparator;
	
	public PatternInstantiator(OWLOntology pattern, OWLModelManager modelManager) {
		super();
		this.pattern = pattern;
		this.useTargetNamespace = PatternInstantiationConfiguration.getUseTargetNamespace();
		this.targetOntologyIri = modelManager.getActiveOntology().getOntologyID().getOntologyIRI().or(IRI.generateDocumentIRI());
		this.entitySeparator = EntityCreationPreferences.getDefaultSeparator();
		String moduleName = String.format("-modules/%s", UUID.randomUUID().toString());
		this.createdModuleIri = IRI.create(targetOntologyIri.toString(), moduleName);
	}

	public Set<OWLAxiom> getInstantiationAxioms() {
		if (useTargetNamespace) {
			OWLOntologyManager patternManager = pattern.getOWLOntologyManager();
			OWLEntityRenamer renamer = new OWLEntityRenamer(patternManager, Collections.singleton(pattern));
			for (OWLEntity entity: pattern.getSignature()) {
				if (!entity.isBuiltIn() && !entity.getIRI().toString().contains("http://ontologydesignpatterns.org/opla#")) {
					String entityShortName = this.entitySeparator + entity.getIRI().getShortForm();
					IRI newIRI = IRI.create(targetOntologyIri.toString(), entityShortName);
					List<OWLOntologyChange> changes = renamer.changeIRI(entity, newIRI);
					patternManager.applyChanges(changes);
				}
			}
		}
		return pattern.getAxioms();
	}
	
	public Set<OWLAxiom> getModuleAnnotationAxioms() {
		log.debug(String.format("The newly minted module will have the IRI: %s", createdModuleIri.toString()));
		// TODO: Implement this based on configuration.
		return new HashSet<OWLAxiom>();
	}

}
