package com.comodide.rendering.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.rendering.PositioningOperations;
import com.comodide.rendering.sdont.model.SDEdge;
import com.comodide.rendering.sdont.model.SDNode;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGraphModel;
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
	 * Listener for mxEvents.CELLS_ADDED event; used to add subClassOf axiom to the model when
	 * a new subClassOf edge is drawn on the canvas.
	 */
	protected mxIEventListener cellsAddedHandler =  new mxIEventListener() {
		@Override
		public void invoke(Object sender, mxEventObject evt) {
			Object[] cells = (Object[]) evt.getProperty("cells");
			if (cells.length == 1) {
				if (cells[0] instanceof mxCell) {
					mxCell edgeCell = (mxCell)cells[0];
					if (edgeCell.getValue() instanceof SDEdge) {
						SDEdge edge = (SDEdge)edgeCell.getValue();
						if (edge.isSubclass()) {

							// Retrieve the terminals of this cell
							mxICell sourceCell = edgeCell.getTerminal(true);
							mxICell targetCell = edgeCell.getTerminal(false);
							
							// Update the SDEdge user object
							if (sourceCell.getValue() instanceof SDNode && targetCell.getValue() instanceof SDNode) {								
								model.beginUpdate();
								lock = true; // prevent loopback during addaxiom
								try
								{
									SDNode sourceNode = (SDNode)sourceCell.getValue();
									SDNode targetNode = (SDNode)targetCell.getValue();
									edge.setSource(sourceNode);
									edge.setTarget(targetNode);
									model.setValue(edgeCell, edge);
									
									// Generate and add the subClassOf axioms to the active ontology
									OWLClass subClass = sourceNode.getOwlEntity().asOWLClass();
									OWLClass superClass = targetNode.getOwlEntity().asOWLClass();
									OWLOntology ontology = modelManager.getActiveOntology();
									OWLOntologyManager ontologyManager = ontology.getOWLOntologyManager();
									OWLSubClassOfAxiom newAxiom = ontologyManager.getOWLDataFactory().getOWLSubClassOfAxiom(subClass, superClass);
									ontologyManager.addAxiom(ontology, newAxiom);
								}
								finally
								{
									lock = false; // always make sure unlocked at this stage
									model.endUpdate();
								}
							}
						}
					}
				}
			}
		}
	};
	
	
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
		//setAlternateEdgeStyle("edgeStyle=mxEdgeStyle.ElbowConnector;elbow=vertical");
		this.modelManager = modelManager;
		this.labelChangeHandler = new LabelChangeHandler(modelManager, this);
		this.updateFromOntologyHandler = new UpdateFromOntologyHandler(this, modelManager);
		this.allowDanglingEdges = false;
		this.addListener(mxEvent.CELLS_MOVED, cellsMovedHandler);
		this.addListener(mxEvent.CELLS_ADDED, cellsAddedHandler);
	}
	
	@Override
	public boolean isCellConnectable(Object cell) {
		// If this is a vertex but there is no SDNode attached to it, then it means
		// it was just dropped and has not been relabelled yet; if so, it should NOT be connectable.
		if (cell instanceof mxCell) {
			boolean isVertex = ((mxCell)cell).isVertex();
			boolean hasSdNode = ((mxCell)cell).getValue() instanceof SDNode;
			if (isVertex && !hasSdNode) {
				return false;
			}
		}
		// In all other cases, defer to parent method
		return super.isCellConnectable(cell);
	}

	@Override
	public boolean isValidSource(Object cell) {
		// If this is a cell with an attached SDNode, and that SDNode is a datatype, 
		// then it should NOT be a valid source of outgoing edges, return false.
		if (cell instanceof mxCell) {
			if (((mxCell)cell).getValue() instanceof SDNode) {
				SDNode node = (SDNode)((mxCell)cell).getValue();
				if (node.isDatatype()) {
					return false;
				}
			}
		}
		// In all other cases, defer to parent method
		return super.isValidSource(cell);
	}
	
	
	@Override
	public boolean isValidTarget(Object cell) {
		// If this is a cell with an attached SDNode, and that SDNode is a datatype, 
		// then it should have at most one incoming edge; if existing edge count > 0, return false.
		if (cell instanceof mxCell) {
			if (((mxCell)cell).getValue() instanceof SDNode) {
				SDNode node = (SDNode)((mxCell)cell).getValue();
				if (node.isDatatype()) {
					if (((mxCell)cell).getEdgeCount() > 0) {
						return false;
					}
				}
			}
		}
		// Do not defer to the parent method because it is broken by our 
		// isValidSource customization above -- instead, copied from super.isValidSource
		return (cell == null && allowDanglingEdges) || (cell != null
				&& (!model.isEdge(cell) || isConnectableEdges())
				&& isCellConnectable(cell));
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
			Object updatedSdEdgeOrNode = labelChangeHandler.handle(changedCell, newLabel);
			// Set the new value
			model.setValue(cell, updatedSdEdgeOrNode);
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

	/** This method is a convenience (but necessary) method for finding a cell in the schema diagram.
	 * Due to the creation mechanism of nodes via drag-and-drop ids are added before the id is assigned
	 * via class/property creation, therefore we must iterate through cells and match their current id
	 * instead of the id that they had when they were dragged and dropped (an arbitrary integer).
	 * @param cellID
	 */
	public mxCell getCell(String cellID)
	{
		Map<String, Object> cells = ((mxGraphModel) model).getCells();
		
		for(Object o : cells.values())
		{
			mxCell cell = (mxCell) o;
			
			if(cell.getId().equals(cellID))
			{
				return cell;
			}
		}
	
		return null;
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
		if (theCell.getValue() instanceof SDEdge) {
			SDEdge edge = (SDEdge)theCell.getValue();
			if (edge.isSubclass()) {
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
