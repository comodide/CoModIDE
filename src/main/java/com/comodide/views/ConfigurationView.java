package com.comodide.views;

import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.patterns.PatternInstantiationConfiguration;
import com.comodide.patterns.PatternInstantiationConfiguration.EdgeCreationAxiom;

public class ConfigurationView extends AbstractOWLViewComponent {

	private static final long serialVersionUID = -9035122006146275611L;
	private static final Logger log = LoggerFactory.getLogger(ConfigurationView.class);

	@Override
	protected void initialiseOWLView() throws Exception {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		// TODO: Add additional configuration options
		
		JLabel edgeCreationAxiomsLabel = new JLabel("Edge creation axioms:");
		Font f = edgeCreationAxiomsLabel.getFont();
		edgeCreationAxiomsLabel.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
		this.add(edgeCreationAxiomsLabel);
		
		List<EdgeCreationAxiom> selectedEdgeCreationAxioms = PatternInstantiationConfiguration.getSelectedEdgeCreationAxioms();
		for (PatternInstantiationConfiguration.EdgeCreationAxiom eca: PatternInstantiationConfiguration.EdgeCreationAxiom.values()) {
			JCheckBox box = new JCheckBox(eca.toString(),selectedEdgeCreationAxioms.contains(eca));
			box.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent itemEvent) {
					int state = itemEvent.getStateChange();
					if (state == ItemEvent.SELECTED) {
						PatternInstantiationConfiguration.addSelectedEdgeCreationAxiom(eca);
					}
					else {
						PatternInstantiationConfiguration.removeSelectedEdgeCreationAxiom(eca);
					}
				}
			});
			this.add(box);
		}
		
		log.info("Configuration view initialized");
	}

	@Override
	protected void disposeOWLView() {
		log.info("Configuration view disposed");
	}

}
