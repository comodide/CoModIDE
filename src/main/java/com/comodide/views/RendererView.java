package com.comodide.views;

import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.rendering.owlax.GraphEditor;
import com.comodide.rendering.sdont.ui.UpdateFailureException;
import com.comodide.rendering.sdont.viz.SDManager;

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
    
    /* ui objects */
//    private SDontViewPanel rendererPanel;
    private JPanel rendererPanel;
    private JLabel tempRight;

    @Override
    protected void initialiseOWLView()
    {
        // Initialise stuff
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.manager = getOWLModelManager();
        
        if(manager != null)
        {
            // Add Listener to the model manager
            this.renderingViewOntologyListener = new RenderingViewOntologyListener();
            this.manager.addOntologyChangeListener(renderingViewOntologyListener);
            
            /* Set up Structure */
            // Renderer Panel
            this.sdManager = new SDManager(manager);
//            this.rendererPanel = new SDontViewPanel(this.manager, this.sdManager);
            this.rendererPanel = new GraphEditor(this.manager);
            add(rendererPanel);
            
            // Create Horizontal glue
            add(Box.createHorizontalGlue());

            // Palette Panel
            this.tempRight = new JLabel("I am the Schema Diagram Rendering View Right.");
            add(this.tempRight);

            // Finish and Log
            log.info("[CoModIDE:RenderingView] Initialized");
        }
        else
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
            // TODO Auto-generated method stub
            log.info("[CoModIDE:RenderingView] Change in Ontology detected.");
            changes.forEach(c -> sdManager.updateNaive());
            
        }
        
    }
}
