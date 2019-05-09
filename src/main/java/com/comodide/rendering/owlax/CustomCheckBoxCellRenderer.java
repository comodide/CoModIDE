package com.comodide.rendering.owlax;

import java.awt.Component;
import java.awt.Rectangle;

import javax.swing.JCheckBox;

import org.protege.editor.owl.OWLEditorKit;


/**
 * Not implemented till now
 * @author sarker
 *
 */

public class CustomCheckBoxCellRenderer extends OWLCellRenderer {

	public CustomCheckBoxCellRenderer(OWLEditorKit editorKit) {
		super(editorKit);
	}

	public Component getCheckBoxCellRendererComponent(JCheckBox checkBox, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		componentBeingRendered = checkBox;
		Rectangle cellBounds = new Rectangle();
		// We need to prevent infinite recursion here!
		if (!gettingCellBounds) {
			gettingCellBounds = true;
			cellBounds = checkBox.getBounds();
			gettingCellBounds = false;
		}
		minTextHeight = 12;
		if (checkBox.getParent() != null) {
			preferredWidth = checkBox.getParent().getWidth();
		}

		setupLinkedObjectComponent(checkBox, cellBounds);
		Component c = prepareRenderer(value, isSelected, cellHasFocus);
		reset();
		return c;
	}

}
