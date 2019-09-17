package com.comodide.editor;

import java.util.List;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;

import com.comodide.editor.changehandlers.UpdateFromOntologyHandler;

public class ComodideEditorManager implements OWLOntologyChangeListener
{
	private final SchemaDiagram  schemaDiagram;
	private final UpdateFromOntologyHandler updateFromOntologyHandler;

	public SchemaDiagram getSchemaDiagram() {
		return schemaDiagram;
	}

	public ComodideEditorManager(OWLModelManager modelManager)
	{
		// Create a new schema diagram to work with
		this.schemaDiagram = new SchemaDiagram(modelManager);
		
		// Assign a handler that renders updates from the underlying ontology onto the schema diagram 
		this.updateFromOntologyHandler = new UpdateFromOntologyHandler(schemaDiagram, modelManager);
		
		// Register as listener to detect changes in the ontology that trigger the above updates
		modelManager.addOntologyChangeListener(this);
		
		// Parse and render the active ontology initially (naïve implementation)
		OWLOntology ontology = modelManager.getActiveOntology();
		ontology.getAxioms().forEach(axiom -> {
			this.updateFromOntologyHandler.handleAddAxiom(axiom, ontology);
		});
	}

	@Override
	public void ontologiesChanged(List<? extends OWLOntologyChange> changes) throws OWLException {
		changes.forEach(change -> {
			if (change.isAxiomChange()) {
				this.updateFromOntologyHandler.handle(change);
			}
		});
	}
}
