package com.comodide.rendering.editor;

import java.util.ArrayList;
import java.util.List;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.rendering.PositioningOperations;
import com.comodide.rendering.sdont.model.SDEdge;
import com.comodide.rendering.sdont.model.SDNode;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.view.mxGraph;

/**
 * A graph that creates new edges from a given template edge. Modified from
 * jGraphx example code.
 */
public class SchemaDiagram extends mxGraph
{
	/** Logging */
	private static final Logger log = LoggerFactory.getLogger(SchemaDiagram.class);

	/** Holds the edge to be used as a template for inserting new edges. */
	protected Object edgeTemplate;

	/** Used for handling the changes to cell lables. i.e. add/remove axioms */
	private LabelChangeHandler labelChangeHandler;

	/** Used for handling changes originating in the ontology */
	private UpdateFromOntologyHandler updateFromOntologyHandler;

	/** Used to interoperate with loaded ontologies. */
	private final OWLModelManager modelManager;

	/** Used to prevent loopback from adding a class via tab */
	private boolean lock = false;

	
	/**
	 * Listener for mxEvent.CELLS_MOVED event; retrieves the new X/Y coordinates for
	 * each moved cell and (through {@link PositioningOperations}) updates the
	 * OPLa-SD positioning annotations for the corresponding OWL entities.
	 */
	protected mxIEventListener cellsMovedHandler = new mxIEventListener()
	{
		public void invoke(Object source, mxEventObject evt)
		{
			Object[] cells = (Object[]) evt.getProperty("cells");
			for (Object c : cells)
			{
				// Unpack data from cells
				mxICell cell = (mxICell) c;
				Double  newX = cell.getGeometry().getX();
				Double  newY = cell.getGeometry().getY();

				// We want to update the position only if the cell is a "proper node"
				// i.e. that the node is actually representing a class in the ontology
				if (cell.getValue() instanceof SDNode)
				{
					// Set the positions inside the node
					SDNode node = (SDNode) cell.getValue();
					node.setPositionX(newX);
					node.setPositionY(newY);
					
					List<OWLEntity> positioningEntities = new ArrayList<OWLEntity>();
					// If this is a datatype cell, put the OPLa-SD positioning annotations on the (implicitly ingoing) attached edges 
					if (node.isDatatype()) {
						for (int i = 0; i < cell.getEdgeCount(); i++) {
							mxICell candidateEdge = cell.getEdgeAt(i);
							if (candidateEdge.isEdge()) {
								log.warn((String)candidateEdge.getValue());
								SDEdge edge = (SDEdge)candidateEdge.getValue();
								positioningEntities.add(edge.getOwlProperty());
							}
						}
					}
					// Else, if it is a class, just put them on the class
					else {
						positioningEntities.add(node.getOwlEntity());
					}
					
					// Check which of the loaded ontologies that hosts the positioning entities
					// and update annotations in those ontologies
					for (OWLEntity positioningEntity: positioningEntities) {
						for (OWLOntology ontology : modelManager.getOntologies())
						{
							if (ontology.containsEntityInSignature(positioningEntity.getIRI()))
							{
								PositioningOperations.updateXYCoordinateAnnotations(positioningEntity, ontology, newX, newY);
								break;
							}
						}
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
		this.modelManager = modelManager;
		this.labelChangeHandler = new LabelChangeHandler(modelManager, this);
		this.updateFromOntologyHandler = new UpdateFromOntologyHandler(this, modelManager);
		this.allowDanglingEdges = false;
		this.addListener(mxEvent.CELLS_MOVED, cellsMovedHandler);
	}

	@Override
	public Object addEdge(Object edge, Object parent, Object source, Object target, Integer index)
	{
		Object o = super.addEdge(edge, parent, source, target, index);
		
		log.info("fired.");

		log.info("----");
		
		log.info(((mxCell) o).getValue().toString());
		
		log.info("----");
		
		log.info(o.toString());
		return o;
	}
	
	@Override
	public void cellLabelChanged(Object cell, Object newValue, boolean autoSize)
	{
		log.info("[CoModIDE:SchemaDiagram] cellLabelChanged intercepted.");
		/* Process object into useful formats. */
		mxCell changedCell = (mxCell) cell;
		String newLabel    = (String) newValue;

		model.beginUpdate();
		this.lock = true; // prevent loopback during addaxiom
		try
		{
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
			this.lock = false; // always make sure unlocked at this stage
			model.endUpdate();
		}
	}

	public void updateSchemaDiagramFromOntology(OWLOntologyChange change)
	{
		updateFromOntologyHandler.handle(change);
	}

	/**
	 * Overriding mxGraph to make datatype nodes non-editable
	 */
	@Override
	public boolean isCellEditable(Object cell)
	{
		mxCell theCell = (mxCell)cell;
		
		if (theCell.getValue() instanceof SDNode) {
			SDNode node = (SDNode)theCell.getValue();
			if (node.isDatatype()) {
				return false;
			}
		}
		return super.isCellEditable(cell);
	}
	
	public boolean isLock()
	{
		return this.lock;
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
