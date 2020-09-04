package com.comodide.editor.model;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;

public class ModuleCell extends mxCell
{
	/** Bookkeeping */
	private static final long serialVersionUID = 1L;

	private static final String unfoldedModuleStyle = "unfoldedModuleVertex";
	private static final String foldedModuleStyle   = "foldedModuleVertex";

	/** 
	 * cells is included for informative purposes
	 * mxGeometry is required, but overwritten later in the call-chain
	 * @param cells
	 */
	public ModuleCell(Object[] cells)
	{
		super("Unnamed Module", new mxGeometry(), ModuleCell.unfoldedModuleStyle);
	}

	public void switchStyle()
	{
		String currentStyle = super.getStyle();

		if (currentStyle.contentEquals(ModuleCell.unfoldedModuleStyle))
		{
			super.setStyle(ModuleCell.foldedModuleStyle);
		}
		else
		{
			super.setStyle(ModuleCell.unfoldedModuleStyle);
		}
	}
}
