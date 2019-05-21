package com.comodide.rendering.editor;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;

/**
 * A graph that creates new edges from a given template edge. Modified from
 * jGraphx example code.
 */
public class SchemaDiagram extends mxGraph
{
	/** Logging */
	private static final Logger log = LoggerFactory.getLogger(SchemaDiagram.class);

	/**
	 * Holds the edge to be used as a template for inserting new edges.
	 */
	protected Object edgeTemplate;

	/** Used for handling the changes to cell lables. i.e. add/remove axioms */
	private LabelChangeHandler labelChangeHandler;

	/**
	 * Custom graph that defines the alternate edge style to be used when the middle
	 * control point of edges is double clicked (flipped).
	 */
	public SchemaDiagram(OWLModelManager modelManager)
	{
		setAlternateEdgeStyle("edgeStyle=mxEdgeStyle.ElbowConnector;elbow=vertical");
		this.labelChangeHandler = new LabelChangeHandler(modelManager);
		this.allowDanglingEdges = false;
	}

	@Override
	public void cellLabelChanged(Object cell, Object newValue, boolean autoSize)
	{
		model.beginUpdate();

		try
		{
			log.info("[CoModIDE:SchemaDiagram] cellLabelChanged intercepted.");
			/* Process object into useful formats. */
			mxCell changedCell = (mxCell) cell;
			String newLabel    = (String) newValue;
			// Handle the label change
			newValue = labelChangeHandler.handle(changedCell, newLabel);
			// Set the new value
			model.setValue(cell, newValue);
			// Autosize, if necessary.
			if (autoSize)
			{
				cellSizeUpdated(changedCell, false);
			}
		}
		finally
		{
			model.endUpdate();
		}
	}

	public void updateSchemaDiagramFromOntology(OWLOntologyChange change)
	{
		log.info("\t\t[CoModIDE:SchemaDiagram] Cascading Ontology Change.");

		// Unpack the OntologyChange
		OWLAxiom axiom = change.getAxiom();
		// Add or remove from graph? Might not be necessary.
		if (change.isAddAxiom())
		{
			log.info(axiom.getAxiomType().toString());
		}
		else
		{
			// TODO remove axiom
		}
	}

	/** Sets the edge template to be used to inserting edges. */
	public void setEdgeTemplate(Object template)
	{
		edgeTemplate = template;
	}

	/**
	 * Overrides the method to use the currently selected edge template for new
	 * edges.
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
