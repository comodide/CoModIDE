package com.comodide.rendering.sdont.ui;

import javax.swing.JPanel;

import org.protege.editor.owl.model.OWLModelManager;

import com.comodide.rendering.sdont.viz.SDManager;
import com.mxgraph.swing.mxGraphComponent;

public class SDontViewPanel extends JPanel
{
    /** Bookkeeping */
    private static final long serialVersionUID = 1L;

    /** Managers */
    private OWLModelManager modelManager;
    private SDManager sdManager;
    
    /** Rendering stuff */
    private mxGraphComponent mxgc;

    /** Empty Constructor */
    public SDontViewPanel()
    {

    }

    public SDontViewPanel(OWLModelManager manager)
    {
        this.modelManager = manager;
        this.sdManager = new SDManager(this.modelManager);
        this.initialize();
    }

    public SDontViewPanel(OWLModelManager modelManager, SDManager sdManager)
    {
        this.modelManager = modelManager;
        this.sdManager = new SDManager(this.modelManager);
        this.initialize();
    }
    
    private void initialize()
    {
        this.mxgc = sdManager.initialize();
        add(mxgc);
        validate();
    }

    public void update() throws UpdateFailureException
    {
        this.mxgc = sdManager.update();
        validate();
    }
}
