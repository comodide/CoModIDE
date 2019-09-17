package com.comodide.editor;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxConnectPreview;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.view.mxGraph;

public class SDConnectPreview extends mxConnectPreview
{

	public SDConnectPreview(mxGraphComponent graphComponent)
	{
		super(graphComponent);
	}
	
	public Object stop(boolean commit, MouseEvent e)
	{
		Object result = (sourceState != null) ? sourceState.getCell() : null;

		if (previewState != null)
		{
			mxGraph graph = graphComponent.getGraph();

			graph.getModel().beginUpdate();
			try
			{
				mxICell cell = (mxICell) previewState.getCell();
				Object src = cell.getTerminal(true);
				Object trg = cell.getTerminal(false);

				if (src != null)
				{
					((mxICell) src).removeEdge(cell, true);
				}

				if (trg != null)
				{
					((mxICell) trg).removeEdge(cell, false);
				}

				if (commit)
				{
					result = graph.addCell(cell, null, null, src, trg);
				}

				fireEvent(new mxEventObject(mxEvent.STOP, "event", e, "commit",
						commit, "cell", (commit) ? result : null));

				// Clears the state before the model commits
				if (previewState != null)
				{
					Rectangle dirty = getDirtyRect();
					graph.getView().clear(cell, false, true);
					previewState = null;

					if (!commit && dirty != null)
					{
						graphComponent.getGraphControl().repaint(dirty);
					}
				}
			}
			finally
			{
				graph.getModel().endUpdate();
			}
		}

		sourceState = null;
		startPoint = null;

		return result;
	}
}
