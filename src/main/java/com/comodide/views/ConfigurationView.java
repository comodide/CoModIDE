package com.comodide.views;

import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;

import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.ComodideConfiguration;
import com.comodide.ComodideConfiguration.EdgeCreationAxiom;

/**
 * CoModIDE configuration view. Provides an interface through which users may select how CoModIDE should instantiate ontology design patterns
 * into their target ontology.
 * @author Karl Hammar <karl@karlhammar.com>
 *
 */
public class ConfigurationView extends AbstractOWLViewComponent {

	private static final long serialVersionUID = -9035122006146275611L;
	private static final Logger log = LoggerFactory.getLogger(ConfigurationView.class);
	
	private static final String USE_TARGET_NAMESPACE_ACTION = "useTarget";
	private static final String KEEP_PATTERN_NAMESPACE_ACTION = "keepPattern";
	private static final String METADATA_EXTERNAL_ACTION = "metadataExternal";
	private static final String METADATA_INTERNAL_ACTION = "metadataInternal";
	private static final String DELETE_PROPERTY_DECLARATIONS_ACTION = "deleteDeclarations";
	private static final String KEEP_PROPERTY_DECLARATIONS_ACTION = "keepDeclarations";
	
	private static final ActionListener namespaceButtonsListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			Boolean useTarget = (e.getActionCommand().equals(USE_TARGET_NAMESPACE_ACTION));
			ComodideConfiguration.setUseTargetNamespace(useTarget);
		}
	};
	private static final ActionListener metadataPlacementListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			Boolean metadataExternal = (e.getActionCommand().equals(METADATA_EXTERNAL_ACTION));
			ComodideConfiguration.setModuleMetadataExternal(metadataExternal);
		}
	};
	private static final ActionListener edgeDeletionPolicyListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			Boolean deletePropertyDeclarations = (e.getActionCommand().equals(DELETE_PROPERTY_DECLARATIONS_ACTION));
			ComodideConfiguration.setDeletePropertyDeclarations( deletePropertyDeclarations);
		}
	};

	@Override
	protected void initialiseOWLView() throws Exception {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JLabel entityNamingLabel = new JLabel("Entity naming:");
		Font f = entityNamingLabel.getFont();
		entityNamingLabel.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
		this.add(entityNamingLabel);
		
		Boolean useTargetNamespace = ComodideConfiguration.getUseTargetNamespace();
		ButtonGroup namespaceGroup = new ButtonGroup();
		JRadioButton useTargetNamespaceButton = new JRadioButton("Use target namespace");
		useTargetNamespaceButton.setActionCommand(USE_TARGET_NAMESPACE_ACTION);
		useTargetNamespaceButton.addActionListener(namespaceButtonsListener);
		namespaceGroup.add(useTargetNamespaceButton);
		useTargetNamespaceButton.setSelected(useTargetNamespace);
		this.add(useTargetNamespaceButton);
		
		JRadioButton keepPatternNamespaceButton = new JRadioButton("Keep pattern namespace");
		keepPatternNamespaceButton.setActionCommand(KEEP_PATTERN_NAMESPACE_ACTION);
		keepPatternNamespaceButton.addActionListener(namespaceButtonsListener);
		namespaceGroup.add(keepPatternNamespaceButton);
		keepPatternNamespaceButton.setSelected(!useTargetNamespaceButton.isSelected());
		this.add(keepPatternNamespaceButton);
		
		JLabel moduleMetadataLabel = new JLabel("Module annotations placement:");
		moduleMetadataLabel.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
		this.add(moduleMetadataLabel);
		
		Boolean moduleMetadataExternal = ComodideConfiguration.getModuleMetadataExternal();
		ButtonGroup metadataGroup = new ButtonGroup();
		JRadioButton metadataExternalButton = new JRadioButton("External (in importing parent ontology)");
		metadataExternalButton.setActionCommand(METADATA_EXTERNAL_ACTION);
		metadataExternalButton.addActionListener(metadataPlacementListener);
		metadataGroup.add(metadataExternalButton);
		metadataExternalButton.setSelected(moduleMetadataExternal);
		this.add(metadataExternalButton);
		
		JRadioButton metadataInternalButton = new JRadioButton("Internal (in target ontology)");
		metadataInternalButton.setActionCommand(METADATA_INTERNAL_ACTION);
		metadataInternalButton.addActionListener(metadataPlacementListener);
		metadataGroup.add(metadataInternalButton);
		metadataInternalButton.setSelected(!metadataExternalButton.isSelected());
		this.add(metadataInternalButton);
		
		JLabel edgeCreationAxiomsLabel = new JLabel("Edge creation axioms:");
		edgeCreationAxiomsLabel.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
		this.add(edgeCreationAxiomsLabel);
		
		Set<EdgeCreationAxiom> selectedEdgeCreationAxioms = ComodideConfiguration.getSelectedEdgeCreationAxioms();
		for (ComodideConfiguration.EdgeCreationAxiom eca: ComodideConfiguration.EdgeCreationAxiom.values()) {
			JCheckBox box = new JCheckBox(eca.toString(),selectedEdgeCreationAxioms.contains(eca));
			box.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent itemEvent) {
					int state = itemEvent.getStateChange();
					if (state == ItemEvent.SELECTED) {
						ComodideConfiguration.addSelectedEdgeCreationAxiom(eca);
					}
					else {
						ComodideConfiguration.removeSelectedEdgeCreationAxiom(eca);
					}
				}
			});
			this.add(box);
		}
		
		JLabel edgeDeletionLabel = new JLabel("Edge deletion policy:");
		edgeDeletionLabel.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
		this.add(edgeDeletionLabel);
		
		Boolean deletePropertyDeclarations = ComodideConfiguration.getDeletePropertyDeclarations();
		ButtonGroup edgeDeletionPolicyGroup = new ButtonGroup();
		JRadioButton deletePropertyDeclarationButton = new JRadioButton("Delete property declarations");
		deletePropertyDeclarationButton.setActionCommand(DELETE_PROPERTY_DECLARATIONS_ACTION);
		deletePropertyDeclarationButton.addActionListener(edgeDeletionPolicyListener);
		edgeDeletionPolicyGroup.add(deletePropertyDeclarationButton);
		deletePropertyDeclarationButton.setSelected(deletePropertyDeclarations);
		this.add(deletePropertyDeclarationButton);
		
		JRadioButton keepPropertyDeclarationsButton = new JRadioButton("Keep property declarations");
		keepPropertyDeclarationsButton.setActionCommand(KEEP_PROPERTY_DECLARATIONS_ACTION);
		keepPropertyDeclarationsButton.addActionListener(edgeDeletionPolicyListener);
		edgeDeletionPolicyGroup.add(keepPropertyDeclarationsButton);
		keepPropertyDeclarationsButton.setSelected(!deletePropertyDeclarationButton.isSelected());
		this.add(keepPropertyDeclarationsButton);
		
		JLabel sendTelemetryLabel = new JLabel("Send usage telemetry:");
		sendTelemetryLabel.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
		this.add(sendTelemetryLabel);
		
		JCheckBox sendTelemetryButton = new JCheckBox("Send anonymous usage telemetry");
		sendTelemetryButton.setSelected(ComodideConfiguration.getSendTelemetry());
		sendTelemetryButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean sendTelemetry = e.getStateChange() == ItemEvent.SELECTED ? true : false;
				ComodideConfiguration.setSendTelemetry(sendTelemetry);
			}
		});
		this.add(sendTelemetryButton);
		
		Container topLevelContainer = this.getTopLevelAncestor();		
		boolean telemetryPreferenceChecked = ComodideConfiguration.getTelemetryPreferenceChecked();
		if (!telemetryPreferenceChecked) {
			int sendTelemetryResponse = JOptionPane.showConfirmDialog(topLevelContainer, 
					"CoModIDE collects some anonymous usage data, to aid its developer's in understanding which features to \n"
					+ "work on and how they might be improved. This data includes, e.g., how many clicks it takes the \n"
					+ "user to find and instantiate a design pattern, how long time it takes to instantiate patterns, \n"
					+ "what the user edge creation preferences are, etc. By clicking yes below, you accept this this \n"
					+ "telemetry gathering. If you click no, the feature will be disabled. You can change your preference \n"
					+ "later on through the CoModIDE configuration view. You can inspect our source code for this and other \n"
					+ "features through our open GitHub repository, linked from https://comodide.com.", 
					"Telemetry acceptance", 
					JOptionPane.YES_NO_OPTION, 
					JOptionPane.QUESTION_MESSAGE);
			boolean sendTelemetry = sendTelemetryResponse == JOptionPane.YES_OPTION ? true : false;
			ComodideConfiguration.setTelemetryPreferenceChecked(true);
			sendTelemetryButton.setSelected(sendTelemetry);
			ComodideConfiguration.setSendTelemetry(sendTelemetry);
		}
		
		log.info("Configuration view initialized");
	}

	@Override
	protected void disposeOWLView() {
		log.info("Configuration view disposed");
	}

}
