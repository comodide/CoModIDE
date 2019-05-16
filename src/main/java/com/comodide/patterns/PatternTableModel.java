package com.comodide.patterns;

import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * Custom AbstractTableModel implementation that holds Pattern objects. Supports
 * the PatternTable class.
 * 
 * @author Karl Hammar <karl@karlhammar.com>
 *
 */
public class PatternTableModel extends AbstractTableModel {

	public static final String[] COLUMN_NAMES = { "Name", "" };

	private static final long serialVersionUID = 5911927324627593760L;
	private List<Pattern> patterns;

	public PatternTableModel(List<Pattern> patterns) {
		super();
		this.patterns = patterns;
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		if (column == 1) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int getColumnCount() {
		return COLUMN_NAMES.length;
	}

	@Override
	public int getRowCount() {
		return patterns.size();
	}

	@Override
	public String getColumnName(int column) {
		return COLUMN_NAMES[column];
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Pattern pattern = patterns.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return pattern.getLabel();
		case 1:
			return "Documentation";
		default:
			return pattern.getIri().toString();
		}
	}

	public void update(List<Pattern> newPatterns) {
		this.patterns = newPatterns;
		fireTableDataChanged();
	}

	public Pattern getPatternAtRow(int rowIndex) {
		return patterns.get(rowIndex);
	}
}
