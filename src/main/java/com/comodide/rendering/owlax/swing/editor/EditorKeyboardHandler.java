/**
 * Copyright (c) 2008, Gaudenz Alder
 */
package com.comodide.rendering.owlax.swing.editor;

import java.awt.event.KeyEvent;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxKeyboardHandler;
import com.mxgraph.swing.util.mxGraphActions;

/**
 * @author Administrator
 * 
 */
public class EditorKeyboardHandler extends mxKeyboardHandler {

	/**
	 * 
	 * @param graphComponent
	 */
	public EditorKeyboardHandler(mxGraphComponent graphComponent) {
		super(graphComponent);
	}

	/**
	 * Return JTree's input map.
	 */
	protected InputMap getInputMap(int condition) {
		InputMap map = super.getInputMap(condition);

		if (condition == JComponent.WHEN_FOCUSED && map != null) {
			String OSNAME = System.getProperty("os.name").toLowerCase();

			if (OSNAME.indexOf("mac") != -1) {

				map.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.META_DOWN_MASK), "save");
				map.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.META_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),
						"saveAs");
				map.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.META_DOWN_MASK), "new");
				map.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.META_DOWN_MASK), "open");

				map.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.META_DOWN_MASK), "undo");
				map.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.META_DOWN_MASK), "redo");
				map.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),
						"selectVertices");
				map.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.META_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),
						"selectEdges");
			} // if (OSNAME.indexOf("windows") != -1)
				// windows or linux then similar keyboard
			else {

				map.put(KeyStroke.getKeyStroke("control S"), "save");
				map.put(KeyStroke.getKeyStroke("control shift S"), "saveAs");
				map.put(KeyStroke.getKeyStroke("control N"), "new");
				map.put(KeyStroke.getKeyStroke("control O"), "open");

				map.put(KeyStroke.getKeyStroke("control Z"), "undo");
				map.put(KeyStroke.getKeyStroke("control Y"), "redo");
				map.put(KeyStroke.getKeyStroke("control shift V"), "selectVertices");
				map.put(KeyStroke.getKeyStroke("control shift E"), "selectEdges");

			}

		}

		return map;
	}

	/**
	 * Return the mapping between JTree's input map and JGraph's actions.
	 */
	protected ActionMap createActionMap() {
		ActionMap map = super.createActionMap();

		map.put("save", new EditorActions.SaveAction(false));
		map.put("saveAs", new EditorActions.SaveAction(true));
		map.put("new", new EditorActions.NewAction());
		map.put("open", new EditorActions.OpenAction());
		map.put("undo", new EditorActions.HistoryAction(true));
		map.put("redo", new EditorActions.HistoryAction(false));
		map.put("selectVertices", mxGraphActions.getSelectVerticesAction());
		map.put("selectEdges", mxGraphActions.getSelectEdgesAction());

		return map;
	}

}
