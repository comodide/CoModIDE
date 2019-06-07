package com.comodide.rendering.editor;

import org.apache.commons.lang3.tuple.Pair;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.find.OWLEntityFinder;
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
import com.mxgraph.model.mxICell;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxUndoableEdit;
import com.mxgraph.util.mxEventSource.mxIEventListener;
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

	/** Used to interoperate with loaded ontologies. */
	private final OWLModelManager modelManager;
	
	/**
	 * Listener for mxEvent.CELLS_MOVED event; retrieves the new X/Y coordinates for
	 * each moved cell and (through {@link PositioningOperations}) updates the OPLa-SD 
	 * positioning annotations for the corresponding OWL entities.
	 */
	protected mxIEventListener cellsMovedHandler = new mxIEventListener()
	{
		public void invoke(Object source, mxEventObject evt)
		{
			Object[] cells = (Object[])evt.getProperty("cells");
			for (Object c: cells) {
				mxICell cell = (mxICell)c;
				Double newX = cell.getGeometry().getX();
				Double newY = cell.getGeometry().getY();
				SDNode node = (SDNode)cell.getValue();
				node.setPositionX(newX);
				node.setPositionY(newY);
				OWLEntity entity = node.getOwlEntity();
				
				// Check which of the loaded ontologies that hosts this entity; 
				// update annotations in that ontology.
				for (OWLOntology ontology: modelManager.getOntologies()) {
					if (ontology.containsEntityInSignature(entity.getIRI())) {
						PositioningOperations.updateXYCoordinateAnnotations(entity, ontology ,newX, newY);
						break;
					}
				}
			}
		}
	};
	
	/**
	 * Custom graph that defines the alternate edge style to be used when the middle
	 * control point of edges is double clicked (flipped).
	 */
	public SchemaDiagram(OWLModelManager modelManager)
	{
		setAlternateEdgeStyle("edgeStyle=mxEdgeStyle.ElbowConnector;elbow=vertical");
		this.labelChangeHandler = new LabelChangeHandler(modelManager);
		this.allowDanglingEdges = false;
		this.addListener(mxEvent.CELLS_MOVED, cellsMovedHandler);
		this.modelManager = modelManager;
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
				// Unpack data from Declaration
				OWLDeclarationAxiom declaration = (OWLDeclarationAxiom) axiom;
				OWLEntity owlEntity = declaration.getEntity();
				OWLOntology ontology = change.getOntology();
				Pair<Double,Double> xyCoords = PositioningOperations.getXYCoordsForEntity(owlEntity, ontology);
				SDNode node = new SDNode(owlEntity, owlEntity instanceof OWLDatatype, xyCoords.getLeft(), xyCoords.getRight());
				Object cell = vertexMaker.makeNode(node);

				// Do update. Code above should be refactored into handler object, etc.
				model.beginUpdate();

				try
				{
					this.addCell(cell);
				}
				finally
				{
					model.endUpdate();
				}
			}
			else if(axiom.isOfType(AxiomType.SUBCLASS_OF))
			{
				
			}
			else
			{
				log.info(axiom.toString());
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
