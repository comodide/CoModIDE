package com.comodide.views;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.TransferHandler;

import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.patterns.Pattern;

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
    private JTextArea testDropArea;

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
        
        add(Box.createHorizontalGlue());
        testDropArea = new JTextArea();
        testDropArea.setTransferHandler(new TransferHandler() {
			private static final long serialVersionUID = 1324913211688855771L;
			private DataFlavor patternFlavor = new DataFlavor(Pattern.class, "Ontology Design Pattern");
			
			public boolean canImport(TransferHandler.TransferSupport info) {
				if (info.isDataFlavorSupported(patternFlavor)) {
					return true;
				}
				else {
					return false;
				}
			}
			
			public boolean importData(TransferHandler.TransferSupport info) {
				try {
					Transferable transfer = info.getTransferable();
					Pattern pattern = (Pattern)transfer.getTransferData(patternFlavor);
					testDropArea.setText(String.format("Pattern %s was dropped.", pattern.getLabel()));
					return true;
				}
				catch (Exception e) {
					return false;
				}
			}
        });
        add(testDropArea);

        // Finish and Log
        log.info("Rendering View initialized");
    }

    @Override
    protected void disposeOWLView()
    {
        log.info("Rendering View disposed");
    }
}
