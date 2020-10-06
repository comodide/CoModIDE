package com.comodide.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.editor.changehandlers.UpdateFromOntologyHandler;

public class ComodideEditorManager implements OWLOntologyChangeListener, OWLModelManagerListener
{
	private static final Logger log = LoggerFactory.getLogger(ComodideEditorManager.class);
	
	private OWLOntology                     presentlyRenderedOntology;
	private SchemaDiagram                   schemaDiagram;
	private final UpdateFromOntologyHandler updateFromOntologyHandler;
	private final OWLModelManager           modelManager;

	public SchemaDiagram getSchemaDiagram()
	{
		return schemaDiagram;
	}

	public ComodideEditorManager(OWLModelManager modelManager)
	{
		// Used to keep track of changes and clearing/re-rendering editor
		this.modelManager = modelManager;
		this.modelManager.addListener(this);
		this.presentlyRenderedOntology = modelManager.getActiveOntology();

		// Create a new schema diagram to work with
		this.schemaDiagram = new SchemaDiagram(modelManager);

		// Assign a handler that renders updates from the underlying ontology onto the
		// schema diagram
		this.updateFromOntologyHandler = new UpdateFromOntologyHandler(schemaDiagram, modelManager);

		// Register as listener to detect changes in the ontology that trigger the above
		// updates
		this.modelManager.addOntologyChangeListener(this);

		// Parse and render the active ontology initially
		this.renderActiveOntology();
	}

	/**
	 * Render the currently active ontology. Naïve implementation that simply pipes
	 * all ontology axioms through the UpdateFromOntologyHandler.
	 */
	private void renderActiveOntology()
	{
		OWLOntology ontology = modelManager.getActiveOntology();
		ArrayList<OWLAxiom> axioms = new ArrayList<>();
		ontology.getAxioms().forEach(axiom -> axioms.add(axiom));
		Collections.sort(axioms);
		// The annotation lock is to prevent the removal of OPLa isNativeTo annotations before
		// the rest of the ontology has been loaded.
		this.schemaDiagram.setAnnotationLock(true);
		axioms.forEach(axiom -> {
			this.updateFromOntologyHandler.handleAddAxiom(axiom, ontology);
		});
		this.schemaDiagram.setAnnotationLock(false);
	}

	/**
	 * Clear the schema diagram and redraw using the currently active ontology
	 */
	private void clearAndRedraw()
	{
		this.schemaDiagram.clear();
		this.renderActiveOntology();
		this.presentlyRenderedOntology = modelManager.getActiveOntology();
	}

	@Override
	public void ontologiesChanged(List<? extends OWLOntologyChange> changes) throws OWLException
	{
		changes.forEach(change -> {
			if (change.isAxiomChange())
			{
				this.updateFromOntologyHandler.handle(change);
			}
		});
	}

	@Override
	public void handleChange(OWLModelManagerChangeEvent event)
	{
		if (event.isType(EventType.ACTIVE_ONTOLOGY_CHANGED))
		{
			if (!modelManager.getActiveOntology().equals(presentlyRenderedOntology))
			{
				this.clearAndRedraw();
			}
		}
	}
}
