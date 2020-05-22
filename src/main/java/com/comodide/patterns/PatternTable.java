package com.comodide.patterns;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.telemetry.TelemetryAgent;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.Set;

/**
 * A specialization of JTable specifically intended to list ontology design
 * patterns in the CoModIDE pattern selector view. Supported by the {@link PatternTableModel} class.
 * 
 * @author Karl Hammar <karl@karlhammar.com>
 *
 */
public class PatternTable extends JTable {

	private static final long serialVersionUID = -6533182826250657204L;
	
	private static final Logger log = LoggerFactory.getLogger(PatternTable.class);

	public PatternTable(PatternTableModel patternTableModel, OWLModelManager modelManager) {
		super(patternTableModel);
		setColumnSelectionAllowed(false);
		setRowSelectionAllowed(true);
		getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.setDragEnabled(true);
		

/*
* The larger prefDenom, the less space col1 takes up when CoModIDE starts up.
* Is not an accurate representative percentage of how much will take of table.
* Some sort of listener on Table Size Change or fancy JTable setting finagling
* would be needed for that. Current solution allows user to resize either column to whatever they wish.*/
		int prefDenom = 5;
		int totalWidth = this.getColumnModel().getTotalColumnWidth();

		TableColumn col0 = this.getColumnModel().getColumn(0);
		TableColumn col1 = this.getColumnModel().getColumn(1);

		col0.setPreferredWidth(totalWidth - totalWidth/prefDenom);
		col1.setPreferredWidth(totalWidth/prefDenom);


		/*
		log.info(String.format("\nPattern Table Column Info:\n" +
				"Total Columns Width:\t%s", totalWidth));

		log.info(String.format("\nColumn 0 Table Size Info:\n" +
				"\tColumn 0 Max Width:\t%s\n" +
				"\tColumn 0 Preferred Width:\t%s", col0.getMaxWidth(), col0.getPreferredWidth()));

		log.info(String.format("\nColumn 1 Table Size Info:\n" +
				"\tColumn 1 Max Width:\t%s\n" +
				"\tColumn 1 Preferred Width:\t%s\n", col1.getMaxWidth(), col1.getPreferredWidth()));
		*/


		// Transfer handler supporting dragging of patterns out of the table.
		this.setTransferHandler(new TransferHandler() {
			private static final long serialVersionUID = -4277997093361110983L;
			
			@Override
			public int getSourceActions(JComponent c) {
			    return COPY;
			}

			@Override
			public Transferable createTransferable(JComponent c) {
				Pattern selectedPattern = ((PatternTableModel) dataModel).getPatternAtRow(getSelectedRow());
				try {
					OWLOntology selectedPatternOntology = PatternLibrary.getInstance().getOwlRepresentation(selectedPattern);
					PatternInstantiator pi = new PatternInstantiator(selectedPatternOntology, selectedPattern.getLabel(), modelManager);
					Set<OWLAxiom> instantiationAxioms = pi.getInstantiationAxioms();
					Set<OWLAxiom> modularizationAxioms = pi.getModuleAnnotationAxioms();
					return new PatternTransferable(instantiationAxioms, modularizationAxioms);
				}
				catch (OWLOntologyCreationException ooce) {
					log.error("The pattern could not be loaded as an OWLAPI OWLOntology: " + ooce.getLocalizedMessage());
					return new StringSelection(String.format("%s: %s", selectedPattern.getLabel(), selectedPattern.getIri().toString()));
				}
			}
			
		});
		
		columnModel.getColumn(1).setCellRenderer(new ButtonRenderer());
		columnModel.getColumn(1).setCellEditor(new ButtonEditor(new JCheckBox()));
		
		this.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				int selectedRow = getSelectedRow();
				if ((selectedRow != -1) && !e.getValueIsAdjusting()) {
					Pattern selectedPattern = ((PatternTableModel) dataModel).getPatternAtRow(selectedRow);
					TelemetryAgent.logLibraryClick(String.format("Pattern: %s (%s)", selectedPattern.getLabel(), selectedPattern.getIri().toString()));
				}
			}
		});
	}

	/**
	 * Inner class for rendering buttons in a pattern table. Design from
	 * http://www.java2s.com/Code/Java/Swing-Components/ButtonTableExample.htm
	 * 
	 * @author Karl Hammar <karl@karlhammar.com>
	 *
	 */
	class ButtonRenderer extends JButton implements TableCellRenderer {

		private static final long serialVersionUID = 6502525250976663915L;

		public ButtonRenderer() {
			setOpaque(true);
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			setText((value == null) ? "" : value.toString());
			return this;
		}
	}

	/**
	 * Inner class that supports clicking on buttons in pattern tables. Design from
	 * http://www.java2s.com/Code/Java/Swing-Components/ButtonTableExample.htm
	 * 
	 * @author Karl Hammar <karl@karlhammar.com>
	 *
	 */
	class ButtonEditor extends DefaultCellEditor {

		private static final long serialVersionUID = -4417701226982861490L;

		protected JButton button;

		private String label;

		private boolean isPushed;

		public ButtonEditor(JCheckBox checkBox) {
			super(checkBox);
			button = new JButton();
			button.setOpaque(true);
			button.addActionListener(e -> fireEditingStopped());
		}

		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
				int column) {
			if (isSelected) {
				button.setForeground(table.getSelectionForeground());
				button.setBackground(table.getSelectionBackground());
			} else {
				button.setForeground(table.getForeground());
				button.setBackground(table.getBackground());
			}
			label = (value == null) ? "" : value.toString();
			button.setText(label);
			isPushed = true;
			return button;
		}

		public Object getCellEditorValue() {
			if (isPushed) {
				Pattern pattern = ((PatternTableModel) dataModel).getPatternAtRow(getSelectedRow());
				TelemetryAgent.logLibraryClick(String.format("Documentation: %s (%s)", pattern.getLabel(), pattern.getIri().toString()));
				PatternDocumentationFrame docFrame = new PatternDocumentationFrame(pattern);
				docFrame.setVisible(true);
			}
			isPushed = false;
			return label;
		}

		public boolean stopCellEditing() {
			isPushed = false;
			return super.stopCellEditing();
		}

		protected void fireEditingStopped() {
			super.fireEditingStopped();
		}
	}

}
