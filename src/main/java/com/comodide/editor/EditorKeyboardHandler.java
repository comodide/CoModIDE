package com.comodide.editor;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxKeyboardHandler;
import com.mxgraph.swing.util.mxGraphActions;

/**
 * @author Administrator
 * 
 */
public class EditorKeyboardHandler extends mxKeyboardHandler
{

	/**
	 * 
	 * @param graphComponent
	 */
	public EditorKeyboardHandler(mxGraphComponent graphComponent)
	{
		super(graphComponent);
	}

	/**
	 * Return minimal input map binding delete and backspace keys to ActionMapKey "delete".
	 */
	protected InputMap getInputMap(int condition)
	{
		InputMap map = null;

		if (condition == JComponent.WHEN_FOCUSED)
		{
			map = new InputMap();
			
			map.put(KeyStroke.getKeyStroke("DELETE"), "delete");
			map.put(KeyStroke.getKeyStroke("BACK_SPACE"), "delete");
			map.put(KeyStroke.getKeyStroke("g"), "group");
			map.put(KeyStroke.getKeyStroke("G"), "group");
		}

		return map;
	}
	
	
	/**
	 * Return minimal mapping between ActionMapKeys and actions.
	 */
	protected ActionMap createActionMap()
	{
		ActionMap map = (ActionMap) UIManager.get("ScrollPane.actionMap");

		map.put("delete", mxGraphActions.getDeleteAction());
		map.put("group", mxGraphActions.getGroupAction());
		
		return map;
	}

}