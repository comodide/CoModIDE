package com.comodide.views;

import javax.swing.BoxLayout;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.editor.ComodideEditor;
import com.comodide.editor.ComodideEditorManager;
import com.comodide.editor.SchemaDiagram;
import com.comodide.editor.SchemaDiagramComponent;

public class EditorView extends AbstractOWLViewComponent
{
	private static final long serialVersionUID = 965978729243157195L;

	/** Logging */
	private static final Logger log = LoggerFactory.getLogger(EditorView.class);

	/** Managers */
	private OWLModelManager       modelManager;
	private ComodideEditorManager comodideEditorManager;

	/** To be called on set up */
	@Override
	protected void initialiseOWLView()
	{
		// Initialize stuff
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.modelManager = getOWLModelManager();

		if (this.modelManager != null)
		{
			this.comodideEditorManager = new ComodideEditorManager(modelManager);
			SchemaDiagram          schemaDiagram          = this.comodideEditorManager.getSchemaDiagram();
			SchemaDiagramComponent schemaDiagramComponent = new SchemaDiagramComponent(schemaDiagram, modelManager);
			ComodideEditor         comodideEditor         = new ComodideEditor(schemaDiagramComponent);
			this.add(comodideEditor);

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
		this.modelManager.removeOntologyChangeListener(this.comodideEditorManager);
		log.info("[CoModIDE:RenderingView] Disposed");
	}
}