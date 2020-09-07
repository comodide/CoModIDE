package com.comodide.editor;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;

import org.protege.editor.owl.model.OWLModelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.messaging.ComodideMessage;
import com.comodide.messaging.ComodideMessageBus;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxConnectionHandler;

public class SchemaDiagramComponent extends mxGraphComponent
{
	/** Bookkeeping */
	private static final long   serialVersionUID = -6833603133512882012L;
	private static final Logger log              = LoggerFactory.getLogger(SchemaDiagramComponent.class);

	public SchemaDiagramComponent(SchemaDiagram diagram, OWLModelManager modelManager)
	{
		super(diagram);
		// Overwrite super created transfer handler
		super.setTransferHandler(new SDTransferHandler(modelManager, diagram));

		// Sets switches typically used in an editor
		setPageVisible(true);
		setGridVisible(true);
		setToolTips(true);
		setEnterStopsCellEditing(true);
		super.connectionHandler.setCreateTarget(false);

		// Sets the background to white
		getViewport().setOpaque(true);
		getViewport().setBackground(Color.WHITE);
	}

	/** Override for injecting custom behavior into connection handling */
	public mxConnectionHandler createConnectionHandler()
	{
		return new SDConnectionHandler(this);
	}

	/**
	 * Overrides drop behaviour to set the cell style if the target is not a valid
	 * drop target and the cells are of the same type (eg. both vertices or both
	 * edges).
	 */
	public Object[] importCells(Object[] cells, double dx, double dy, Object target, Point location)
	{
		if (target == null && cells.length == 1 && location != null)
		{
			target = getCellAt(location.x, location.y);

			if (target instanceof mxICell && cells[0] instanceof mxICell)
			{
				mxICell targetCell = (mxICell) target;
				mxICell dropCell   = (mxICell) cells[0];

				if (targetCell.isVertex() == dropCell.isVertex() || targetCell.isEdge() == dropCell.isEdge())
				{
					mxIGraphModel model = graph.getModel();
					model.setStyle(target, model.getStyle(cells[0]));
					graph.setSelectionCell(target);

					return null;
				}
			}
		}

		return super.importCells(cells, dx, dy, target, location);
	}

	@Override
	public void selectCellForEvent(Object cell, MouseEvent e)
	{
		super.selectCellForEvent(cell, e);
		boolean result = ComodideMessageBus.getSingleton().sendMessage(ComodideMessage.CELL_SELECTED, cell);
//		log.info("[CoModIDE:SchemaDiagramComponent] " + result);
	}
}
