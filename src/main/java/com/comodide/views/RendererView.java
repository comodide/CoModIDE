package com.comodide.views;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;

import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.sdont.model.SDGraph;
import com.comodide.sdont.parsing.OntologyParser;
import com.comodide.sdont.ui.SDontViewFrame;
import com.comodide.sdont.viz.SDMaker;

public class RendererView extends AbstractOWLViewComponent
{
    /* Book keeping */
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(RendererView.class);

    /* some sort of listener needs to be added here */
    // TODO listener ?
    
    /* ui objects */
    private JLabel tempLeft;
    private JLabel tempRight;

    @Override
    protected void initialiseOWLView()
    {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        // Renderer Frame
        // this is probably not going to work at the moment, as there is no ontology loaded at initialization
        // we will have to put some sort of update method written. 
        // I think there is something in the OPLa Plugin and OWLax that can help with this.
//        OntologyParser ontologyParser = new OntologyParser(getOWLModelManager());
//        SDGraph graph = ontologyParser.parseOntology();
//        SDMaker maker = new SDMaker(graph);
//        SDontViewFrame sdont = maker.visualize();
//        add(sdont);
        
        tempLeft = new JLabel("I am the Schema Diagram Rendering View Left");
        add(tempLeft);
        
        // Create Horizontal glue
        add(Box.createHorizontalGlue());
        
        // Palette Frame
        tempRight = new JLabel("I am the Schema Diagram Rendering View Right.");
        add(tempRight);

        // Finish and Log
        log.info("Rendering View initialized");
    }

    @Override
    protected void disposeOWLView()
    {
        log.info("Rendering View disposed");
    }
}
