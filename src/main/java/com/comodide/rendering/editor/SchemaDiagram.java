package com.comodide.rendering.editor;

import org.apache.commons.lang3.tuple.Pair;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.rendering.PositioningOperations;
import com.comodide.rendering.sdont.model.SDNode;
import com.comodide.rendering.sdont.viz.mxEdgeMaker;
import com.comodide.rendering.sdont.viz.mxVertexMaker;
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

	/** Used for creating the styled mxcells for the graph */
	private mxVertexMaker vertexMaker;
	private mxEdgeMaker   edgeMaker;

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

	public void createCellMakers()
	{
		this.vertexMaker = new mxVertexMaker(this);
		this.edgeMaker = new mxEdgeMaker(this);
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
			if (axiom.isOfType(AxiomType.DECLARATION))
			{
				OWLDeclarationAxiom declaration = (OWLDeclarationAxiom) axiom;

				OWLEntity owlEntity = declaration.getEntity();
				OWLOntology ontology = change.getOntology();
				Pair<Double,Double> xyCoords = PositioningOperations.getXYCoordsForEntity(owlEntity, ontology);
				SDNode node = new SDNode(owlEntity, owlEntity instanceof OWLDatatype, xyCoords.getLeft(), xyCoords.getRight());
				Object cell = vertexMaker.makeNode(node);

				model.beginUpdate();

				try
				{
					log.info("cell added to graph.");
					this.addCell(cell);
				}
				finally
				{
					model.endUpdate();
				}
			}

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
