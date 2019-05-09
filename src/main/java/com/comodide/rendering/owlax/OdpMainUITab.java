package com.comodide.rendering.owlax;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.find.OWLEntityFinder;
import org.protege.editor.owl.ui.OWLWorkspaceViewsTab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.rendering.owlax.swing.editor.EditorMenuBar;

public class OdpMainUITab extends OWLWorkspaceViewsTab {

	private static final long serialVersionUID = 1L;
	private OWLModelManager protegeOWLModelManager;
	private static final Logger log = LoggerFactory.getLogger(OdpMainUITab.class);
	OWLEditorKit owlEditorKit;
	GraphEditor editor;
	OWLEntityFinder owlEntityFinder;
	private final ODPTabListener listener = new ODPTabListener();

	@Override
	public void initialise() {

		super.initialise();

		setToolTipText("OWLAx");

		if (getOWLModelManager() != null) {

			// first set protege informations
			this.protegeOWLModelManager = getOWLModelManager();
			this.protegeOWLModelManager.addListener(listener);

			this.owlEditorKit = getOWLEditorKit();

			setLayout(new BorderLayout());

			editor = new GraphEditor(this.protegeOWLModelManager);

			add(new EditorMenuBar(editor), BorderLayout.NORTH);

			add(editor, BorderLayout.CENTER);

			JFrame mainWindow = (javax.swing.JFrame) SwingUtilities.windowForComponent(this);
			editor.setProtegeMainWindow(mainWindow);

			update();
		} else
			log.warn("OWLAx initialization failed - no model manager");

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		//super.dispose();
		getOWLModelManager().removeListener(this.listener);
	}

	private void update() {

		this.protegeOWLModelManager = getOWLModelManager();
		this.owlEditorKit = getOWLEditorKit();
		this.owlEntityFinder = this.protegeOWLModelManager.getOWLEntityFinder();

		if (this.protegeOWLModelManager != null) {
			editor.setProtegeOWLModelManager(this.protegeOWLModelManager);
		}
		if (this.owlEditorKit != null) {
			editor.setProtegeOWLEditorKit(this.owlEditorKit);
		}
		if (this.owlEntityFinder != null) {
			editor.setProtegeEntityFinder(this.owlEntityFinder);

		}

	}

	private class ODPTabListener implements OWLModelManagerListener {
		@Override
		public void handleChange(OWLModelManagerChangeEvent event) {

			if (event.getType() == EventType.ACTIVE_ONTOLOGY_CHANGED) {

				update();
			}
		}
	}

}
