package com.comodide.views;

import java.awt.Font;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.editor.model.InterfaceSlotCell;
import com.comodide.messaging.ComodideMessage;
import com.comodide.messaging.ComodideMessageBus;
import com.comodide.messaging.ComodideMessageHandler;
import com.comodide.patterns.Pattern;
import com.comodide.patterns.PatternLibrary;
import com.comodide.patterns.PatternTable;
import com.comodide.patterns.PatternTableModel;

public class CompatiblePatternsView extends AbstractOWLViewComponent  implements ComodideMessageHandler {

	private static final long serialVersionUID = 2486619247656600305L;
	private static final Logger log = LoggerFactory.getLogger(CompatiblePatternsView.class);
	
	// Private members
	private PatternLibrary patternLibrary = PatternLibrary.getInstance();
	private PatternTable patternsTable;
	private PatternTableModel patternsTableModel = new PatternTableModel(new ArrayList<Pattern>());
	
	@Override
	protected void initialiseOWLView() throws Exception {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
        JLabel patternsLabel = new JLabel("Patterns:");
        Font f = patternsLabel.getFont();
        patternsLabel.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
        this.add(patternsLabel);

        patternsTable = new PatternTable(patternsTableModel, this.getOWLModelManager());
		JScrollPane patternsTableScrollPane = new JScrollPane(patternsTable);
		patternsTable.setFillsViewportHeight(true);
		patternsTableScrollPane.setAlignmentX(LEFT_ALIGNMENT);
        this.add(patternsTableScrollPane);
        
        ComodideMessageBus.getSingleton().registerHandler(ComodideMessage.CELL_SELECTED, this);

        log.info("Compatible patterns view initialized");
		
	}

	@Override
	protected void disposeOWLView() {
		log.info("Compatible patterns view disposed");
	}

	@Override
	public boolean handleComodideMessage(ComodideMessage message, Object payload) {
		if (message == ComodideMessage.CELL_SELECTED) {
			if (payload instanceof InterfaceSlotCell) {
				InterfaceSlotCell interfaceSlot = (InterfaceSlotCell)payload;
				IRI interfaceIri = interfaceSlot.getInterfaceIri();
				patternsTableModel.update(patternLibrary.getPatternsThatImplementInterface(interfaceIri));
			}
			else {
				patternsTableModel.update(new ArrayList<Pattern>());
			}
			return true;
		}
		return false;
	}
}
