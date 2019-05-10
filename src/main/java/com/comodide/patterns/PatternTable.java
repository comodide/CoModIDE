package com.comodide.patterns;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

/**
 * A specialization of JTable specifically intended to list ontology design
 * patterns in the CoModIDE pattern selector view.
 * 
 * @author karlh
 *
 */
public class PatternTable extends JTable {

	private static final long serialVersionUID = -6533182826250657204L;

	public PatternTable(PatternTableModel patternTableModel) {
		super(patternTableModel);
		setColumnSelectionAllowed(false);
		setRowSelectionAllowed(true);
		getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		columnModel.getColumn(2).setCellRenderer(new ButtonRenderer());
		columnModel.getColumn(2).setCellEditor(new ButtonEditor(new JCheckBox()));
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
			if (isSelected) {
				setForeground(table.getSelectionForeground());
				setBackground(table.getSelectionBackground());
			} else {
				setForeground(table.getForeground());
				setBackground(UIManager.getColor("Button.background"));
			}
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
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					fireEditingStopped();
				}
			});
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
				PatternDocumentationFrame docFrame = new PatternDocumentationFrame(pattern);
				docFrame.setVisible(true);
			}
			isPushed = false;
			return new String(label);
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
