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
import com.comodide.rendering.sdont.viz.SDManager;
import com.comodide.rendering.sdont.viz.UpdateFailureException;

public class RendererView extends AbstractOWLViewComponent
{
    /* Book keeping */
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(RendererView.class);

    /* managers */
    private OWLModelManager manager;
    private SDManager sdManager;

    /* some sort of listener needs to be added here */
    private RenderingViewOntologyListener renderingViewOntologyListener;

    /* UI objects */
    private JPanel rendererPanel;

    @Override
    protected void initialiseOWLView()
    {
        // Initialize stuff
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.manager = getOWLModelManager();

        if (this.manager != null)
        {
            // Add Listener to the model manager
            this.renderingViewOntologyListener = new RenderingViewOntologyListener();
            this.manager.addOntologyChangeListener(renderingViewOntologyListener);

            // Renderer Panel
            this.sdManager = new SDManager(manager);
            this.rendererPanel = new GraphEditor(this.sdManager.initialSchemaDiagram(), manager);
            add(rendererPanel);

            // Finish and Log
            log.info("[CoModIDE:RenderingView] Initialized");
        } else
        {
            log.error("[CoModIDE:RenderingView] Manager does not exist.");
        }

    }

    public void update() throws UpdateFailureException
    {
//        this.rendererPanel.update();
    }

    @Override
    protected void disposeOWLView()
    {
        this.manager.removeOntologyChangeListener(this.renderingViewOntologyListener);
        log.info("[CoModIDE:RenderingView] Disposed");
    }

    private class RenderingViewOntologyListener implements OWLOntologyChangeListener
    {
        @Override
        public void ontologiesChanged(List<? extends OWLOntologyChange> changes) throws OWLException
        {
            log.info("[CoModIDE:RenderingView] Change in Ontology detected.");
            changes.forEach(c -> sdManager.updateNaive());
        }
    }
}
