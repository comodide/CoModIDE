package com.comodide.rendering.editor;

import java.awt.Color;
import java.awt.Point;

import org.protege.editor.owl.model.OWLModelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.mxgraph.io.mxCodec;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;

public class SDontComponent extends mxGraphComponent
{
	/** Bookkeeping */
	private static final long serialVersionUID = -6833603133512882012L;

	/** Logging */
	private static final Logger log = LoggerFactory.getLogger(SDontComponent.class);

	private OWLModelManager modelManager;

	public SDontComponent(mxGraph graph, OWLModelManager modelManager)
	{
		super(graph);
		// Set the model manager for OWLAPI integration
		this.modelManager = modelManager;
		// Overwrite super created transfer handler
		super.setTransferHandler(new SDontTransferHandler(modelManager));
		// Add an Event Handler for
		this.addListeners();

		// Sets switches typically used in an editor
		setPageVisible(true);
		setGridVisible(true);
		setToolTips(true);
		getConnectionHandler().setCreateTarget(true);

		// Loads the defalt stylesheet from an external file
		mxCodec  codec = new mxCodec();
		Document doc   = mxUtils.loadDocument(GraphEditor.class.getResource("/resources/default-style.xml").toString());
		codec.decode(doc.getDocumentElement(), graph.getStylesheet());

		// Sets the background to white
		getViewport().setOpaque(true);
		getViewport().setBackground(Color.WHITE);
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

	public void addListeners()
	{
		/** This listener will handle the addition of classes and properties. */
		this.addListener(mxEvent.LABEL_CHANGED, new mxIEventListener()
		{
			@Override
			public void invoke(Object sender, mxEventObject evtObj)
			{
				log.info("[CoModIDE:SDontComponent] Label Change Detected.");
				log.info("\t"+evtObj.getProperty("cell"));
			}
		});

		/**
		 * This listener will handle when edges change connects AND have already been
		 * instantiated. TODO this does not work at the moment.
		 */
		this.addListener(mxEvent.CELL_CONNECTED, new mxIEventListener()
		{

			@Override
			public void invoke(Object arg0, mxEventObject arg1)
			{
				log.info("[CoModIDE:SDontComponent] Cells Connected Detected.");
			}
		});
	}

}
