package com.comodide.views;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.patterns.PatternInstantiationConfiguration;
import com.comodide.patterns.PatternInstantiationConfiguration.EdgeCreationAxiom;

public class ConfigurationView extends AbstractOWLViewComponent {

	private static final long serialVersionUID = -9035122006146275611L;
	private static final Logger log = LoggerFactory.getLogger(ConfigurationView.class);
	
	private static final String USE_TARGET_NAMESPACE_ACTION = "useTarget";
	private static final String KEEP_PATTERN_NAMESPACE_ACTION = "keepPattern";
	
	private static final ActionListener namespaceButtonsListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			Boolean useTarget = (e.getActionCommand().equals(USE_TARGET_NAMESPACE_ACTION));
			PatternInstantiationConfiguration.setUseTargetNamespace(useTarget);
		}
	};

	@Override
	protected void initialiseOWLView() throws Exception {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		JLabel entityNamingLabel = new JLabel("Entity naming:");
		Font f = entityNamingLabel.getFont();
		entityNamingLabel.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
		this.add(entityNamingLabel);
		
		Boolean useTargetNamespace = PatternInstantiationConfiguration.getUseTargetNamespace();
		ButtonGroup group = new ButtonGroup();
		JRadioButton useTargetNamespaceButton = new JRadioButton("Use target namespace");
		useTargetNamespaceButton.setActionCommand(USE_TARGET_NAMESPACE_ACTION);
		useTargetNamespaceButton.addActionListener(namespaceButtonsListener);
		group.add(useTargetNamespaceButton);
		useTargetNamespaceButton.setSelected(useTargetNamespace);
		this.add(useTargetNamespaceButton);
		
		JRadioButton keepPatternNamespaceButton = new JRadioButton("Keep pattern namespace");
		keepPatternNamespaceButton.setActionCommand(KEEP_PATTERN_NAMESPACE_ACTION);
		keepPatternNamespaceButton.addActionListener(namespaceButtonsListener);
		group.add(keepPatternNamespaceButton);
		keepPatternNamespaceButton.setSelected(!useTargetNamespaceButton.isSelected());
		this.add(keepPatternNamespaceButton);
		
		// TODO: Add additional configuration options
		
		JLabel edgeCreationAxiomsLabel = new JLabel("Edge creation axioms:");
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
