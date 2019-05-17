package com.comodide.rendering.editor;

import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.axiomatization.AxiomManager;
import com.comodide.rendering.sdont.model.SDNode;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;

/**
 * A graph that creates new edges from a given template edge. Modified from
 * jGraphx example code.
 */
public class SchemaDiagram extends mxGraph
{
	/** Holds the shared number formatter */
	public static final NumberFormat numberFormat = NumberFormat.getInstance();

	/** Logging */
	private static final Logger log = LoggerFactory.getLogger(SchemaDiagram.class);

	/**
	 * Holds the edge to be used as a template for inserting new edges.
	 */
	protected Object edgeTemplate;

	/**
	 * Manages the axioms to be added or removed based on the nodes and connections
	 * inside of the graph
	 */
	private AxiomManager axiomManager;

	/**
	 * Custom graph that defines the alternate edge style to be used when the middle
	 * control point of edges is double clicked (flipped).
	 */
	public SchemaDiagram(OWLModelManager modelManager)
	{
		setAlternateEdgeStyle("edgeStyle=mxEdgeStyle.ElbowConnector;elbow=vertical");
		this.axiomManager = AxiomManager.getInstance(modelManager);
	}

	@Override
	public void cellLabelChanged(Object cell, Object newValue, boolean autoSize)
	{
		model.beginUpdate();

		try
		{
			log.info("[CoModIDE:SchemaDiagram] cellLabelChanged intercepted.");

			// Cast the object into a cell
			mxCell c = (mxCell) cell;
			log.info("[CoModIDE:SchemaDiagram]" + c.getStyle());
			// Get value of the object that is changing.
			Object v = c.getValue();
			// Typecast the received newValue to String.
			String newLabel = (String) newValue;
			// If v is a string, then that means it is a truly new node
			if (v instanceof String && c.getStyle().equals(SDConstants.classShape)) // New class
			{
				log.info("[CoModIDE:SchemaDiagram] New class detected.");
				// Add the new class to the ontology
				OWLClass clazz = this.axiomManager.addNewClass(newLabel);
				// Create an SDNode wrapper for the Axiom
				SDNode node = new SDNode(newLabel, false, (OWLEntity) clazz);
				// Set the value in the model.
				model.setValue(cell, node);
			}
			else if (v instanceof String && c.getStyle().equals(SDConstants.datatypeShape))
			{
				// TODO
				// FIXME
				log.info("[CoModIDE:SchemaDiagram] New datatype detected.");
				// Add the new class to the ontology
				List<OWLDatatype> datatypes = this.axiomManager.addDatatype(newLabel);
				// there should hopefully just be one...
				if (datatypes.size() == 1)
				{
					// Get the datatype
					OWLDatatype datatype = datatypes.get(0);
					// Create an SDNode wrapper for the Axiom
					SDNode node = new SDNode(newLabel, true, (OWLEntity) datatype);
					// Set the value in the model.
					model.setValue(cell, node);
				}
				else
				{
					// Don't do anything.
					// This will force them to type something as the label again
					// TODO Eventually we want a descriptive message
				}
			}
			else
			{
				// TODO
			}

			// End Adding //
			if (autoSize)
			{
				cellSizeUpdated(cell, false);
			}
		}
		finally
		{
			model.endUpdate();
		}
	}

	/**
	 * Sets the edge template to be used to inserting edges.
	 */
	public void setEdgeTemplate(Object template)
	{
		edgeTemplate = template;
	}

	/**
	 * Prints out some useful information about the cell in the tooltip.
	 */
	public String getToolTipForCell(Object cell)
	{
		String      tip   = "<html>";
		mxGeometry  geo   = getModel().getGeometry(cell);
		mxCellState state = getView().getState(cell);

		if (getModel().isEdge(cell))
		{
			tip += "points={";

			if (geo != null)
			{
				List<mxPoint> points = geo.getPoints();

				if (points != null)
				{
					Iterator<mxPoint> it = points.iterator();

					while (it.hasNext())
					{
						mxPoint point = it.next();
						tip += "[x=" + numberFormat.format(point.getX()) + ",y=" + numberFormat.format(point.getY())
								+ "],";
					}

					tip = tip.substring(0, tip.length() - 1);
				}
			}

			tip += "}<br>";
			tip += "absPoints={";

			if (state != null)
			{

				for (int i = 0; i < state.getAbsolutePointCount(); i++)
				{
					mxPoint point = state.getAbsolutePoint(i);
					tip += "[x=" + numberFormat.format(point.getX()) + ",y=" + numberFormat.format(point.getY()) + "],";
				}

				tip = tip.substring(0, tip.length() - 1);
			}

			tip += "}";
		}
		else
		{
			tip += "geo=[";

			if (geo != null)
			{
				tip += "x=" + numberFormat.format(geo.getX()) + ",y=" + numberFormat.format(geo.getY()) + ",width="
						+ numberFormat.format(geo.getWidth()) + ",height=" + numberFormat.format(geo.getHeight());
			}

			tip += "]<br>";
			tip += "state=[";

			if (state != null)
			{
				tip += "x=" + numberFormat.format(state.getX()) + ",y=" + numberFormat.format(state.getY()) + ",width="
						+ numberFormat.format(state.getWidth()) + ",height=" + numberFormat.format(state.getHeight());
			}

			tip += "]";
		}

		mxPoint trans = getView().getTranslate();

		tip += "<br>scale=" + numberFormat.format(getView().getScale()) + ", translate=[x="
				+ numberFormat.format(trans.getX()) + ",y=" + numberFormat.format(trans.getY()) + "]";
		tip += "</html>";

		return tip;
	}

	/**
	 * Overrides the method to use the currently selected edge template for new
	 * edges.
	 * 
	 * @param graph
	 * @param parent
	 * @param id
	 * @param value
	 * @param source
	 * @param target
	 * @param style
	 * @return
	 */
	public Object createEdge(Object parent, String id, Object value, Object source, Object target, String style)
	{
		if (edgeTemplate != null)
		{
			mxCell edge = (mxCell) cloneCells(new Object[] { edgeTemplate })[0];
			edge.setId(id);

			return edge;
		}

		return super.createEdge(parent, id, value, source, target, style);
	}
}
