package com.comodide.views;

import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.rendering.editor.GraphEditor;
import com.comodide.rendering.editor.SDontComponent;
import com.comodide.rendering.editor.SchemaDiagram;
import com.comodide.rendering.sdont.viz.SDManager;

public class RendererView extends AbstractOWLViewComponent
{
	/** Book keeping */
	private static final long   serialVersionUID = 1L;
	
	/** Logging */
	private static final Logger log              = LoggerFactory.getLogger(RendererView.class);

	/** Managers */
	private OWLModelManager manager;
	private SDManager       sdManager;

	/** Listener for detecting changes in the underlying ontology */
	private RenderingViewOntologyListener renderingViewOntologyListener;

	/** UI */
	private JPanel rendererPanel;

	/** To be called on set up */
	@Override
	protected void initialiseOWLView()
	{
		// Initialize stuff
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.manager = getOWLModelManager();

		if (this.manager != null)
		{
			/* Construct the RendererPanel */
			// The SDManager is a wrapper class for the ontology loading
			this.sdManager = new SDManager(manager);
			// Create the initial schema diagram. If there is a loaded ontology, it will
			// render it.
			SchemaDiagram initialSchemaDiagram = this.sdManager.initialSchemaDiagram();
			// Create visualization component
			SDontComponent sdComponent = new SDontComponent(initialSchemaDiagram, manager);
			// Show to user
			this.rendererPanel = new GraphEditor(sdComponent);
			add(rendererPanel);

			/* Register listener to detect changes in the ontology */
			// Create the Listener
			this.renderingViewOntologyListener = new RenderingViewOntologyListener();
			// Add to the manager
			this.manager.addOntologyChangeListener(renderingViewOntologyListener);
			
			// Finish and Log
			log.info("[CoModIDE:RenderingView] Successfully initialized");
		}
		else
		{
			log.error("[CoModIDE:RenderingView] Manager does not exist.");
		}

	}

	/** To be called when exiting. */
	@Override
	protected void disposeOWLView()
	{
		this.manager.removeOntologyChangeListener(this.renderingViewOntologyListener);
		log.info("[CoModIDE:RenderingView] Disposed");
	}

	/**
	 * This ontology listener detects changes in the underlying ontology. It will
	 * in, turn attempt to apply the changes in the overlaying visualization.
	 */
	private class RenderingViewOntologyListener implements OWLOntologyChangeListener
	{
		@Override
		public void ontologiesChanged(List<? extends OWLOntologyChange> changes) throws OWLException
		{
			changes.forEach(change -> {if(change.isAxiomChange()) sdManager.updateSchemaDiagramFromOntology(change);});
		}
	}
}