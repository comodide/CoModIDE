package com.comodide.views;

import javax.swing.BoxLayout;

import com.comodide.editor.model.ComodideCell;
import com.mxgraph.view.mxGraph;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.OWLEntity;
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

			// Listen for selection events from the other tabs in Protégé to also select cells in the editor
			this.getOWLWorkspace().getOWLSelectionModel().addListener(() -> {

				// The currently selected OWLEntity (like when the user selects an entity in Protégé)
				OWLEntity selectedEntity = this.getOWLWorkspace().getOWLSelectionModel().getSelectedEntity();

				// Get all the cells in the editor graph
				mxGraph graph = schemaDiagramComponent.getGraph();
				Object parent = graph.getDefaultParent();
				Object[] cells = graph.getChildCells(parent);

				// Search the graph cells to find the one corresponding to the selected entity
                for (Object cell : cells) {
					if (cell instanceof ComodideCell) {
						ComodideCell comodideCell = (ComodideCell) cell;
						// Check if the entity's cell is found
						if (comodideCell.getEntity().equals(selectedEntity)) {
							// Select the cell in the editor
							schemaDiagramComponent.selectCellForEvent(cell, null);
							return; // stop searching
						}
					}
				}

			});

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
		this.modelManager.removeListener(this.comodideEditorManager);
		log.info("[CoModIDE:RenderingView] Disposed");
	}
}