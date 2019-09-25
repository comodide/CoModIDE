package com.comodide.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.comodide.editor.changehandlers.LabelChangeHandler;
import com.comodide.editor.model.ClassCell;
import com.comodide.editor.model.ComodideCell;
import com.comodide.editor.model.DatatypeCell;
import com.comodide.editor.model.PropertyEdgeCell;
import com.comodide.editor.model.SubClassEdgeCell;
import com.comodide.rendering.PositioningOperations;
import com.mxgraph.io.mxCodec;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxICell;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxUtils;
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
				if (cells[0] instanceof SubClassEdgeCell) {
					SubClassEdgeCell subclassEdgeCell = (SubClassEdgeCell)cells[0];

							// Retrieve the terminals of this cell
							mxICell sourceCell = subclassEdgeCell.getTerminal(true);
							mxICell targetCell = subclassEdgeCell.getTerminal(false);
							
							if (sourceCell instanceof ClassCell && targetCell instanceof ClassCell) {								
								model.beginUpdate();
								lock = true; // prevent loopback during addaxiom
								try
								{
									// Generate and add the subClassOf axioms to the active ontology
									ClassCell sourceClassCell = (ClassCell)sourceCell;
									ClassCell targetClassCell = (ClassCell)targetCell;
									OWLClass subClass = sourceClassCell.getEntity().asOWLClass();
									OWLClass superClass = targetClassCell.getEntity().asOWLClass();
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
				if (cell instanceof ClassCell || cell instanceof DatatypeCell)
				{
					List<OWLEntity> positioningEntities = new ArrayList<OWLEntity>();
					// If this is a datatype cell, put the OPLa-SD positioning annotations on the (implicitly ingoing) attached edges 
					if (cell instanceof DatatypeCell) {
						for (int i = 0; i < cell.getEdgeCount(); i++) {
							mxICell candidateEdge = cell.getEdgeAt(i);
							if (candidateEdge instanceof PropertyEdgeCell) {
								PropertyEdgeCell propertyCell = (PropertyEdgeCell)candidateEdge;
								positioningEntities.add(propertyCell.getEntity());
							}
						}
					}
					// Else, if it is a class, just put them on the class
					else {
						ClassCell classCell = (ClassCell)cell;
						positioningEntities.add(classCell.getEntity());
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
		this.allowDanglingEdges = false;
		this.addListener(mxEvent.CELLS_MOVED, cellsMovedHandler);
		this.addListener(mxEvent.CELLS_ADDED, cellsAddedHandler);
		
		// Loads styling information from an external file.
		mxCodec  codec = new mxCodec();
		Document doc   = mxUtils.loadDocument(ComodideEditor.class.getResource("/resources/comodide-style.xml").toString());
		codec.decode(doc.getDocumentElement(), getStylesheet());
	}
	
	@Override
	public boolean isCellConnectable(Object cell) {
		// If this is a class cell but it has not yet been named, then it means
		// it was just dropped and if so, it should NOT be connectable.
		if (cell instanceof ClassCell) {
			if (!((ClassCell) cell).isNamed()) {
				return false;
			}
		}
		// In all other cases, defer to parent method
		return super.isCellConnectable(cell);
	}

	@Override
	public boolean isValidSource(Object cell) {
		// If this is a datatype cell then it should NOT be 
		// a valid source of outgoing edges, return false.
		if (cell instanceof DatatypeCell) {
			return false;
		}
		// In all other cases, defer to parent method
		return super.isValidSource(cell);
	}
	
	@Override
	public boolean isValidTarget(Object cell) {
		// If this is a datatype cell then it should have 
		// at most one incoming edge; if existing 
		// edge count > 0, return false.
		if (cell instanceof DatatypeCell) {
			if (((DatatypeCell)cell).getEdgeCount() > 0) {
				return false;
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
		ComodideCell changedCell = (ComodideCell) cell;
		String newLabel    = (String) newValue;

		model.beginUpdate();
		this.lock = true; // prevent loopback during addaxiom
		try
		{
			// Handle the label change
			OWLEntity entity = labelChangeHandler.handle(changedCell, newLabel);
			changedCell.setEntity(entity);
			model.setValue(cell, newLabel);
			
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
	
	public List<mxCell> findCellsById(String id) {
		List<mxCell> foundCells = new ArrayList<mxCell>();
		Map<String, Object> cellsOnDiagram = ((mxGraphModel) model).getCells();
		for(Object o : cellsOnDiagram.values())
		{
			mxCell candidateCell = (mxCell) o;
			if(candidateCell.getId().equals(id))
			{
				foundCells.add(candidateCell);
			}
		}
		return foundCells;
	}
	
	/**
	 * Overriding mxGraph to make datatype nodes and subclass edges non-editable
	 */
	@Override
	public boolean isCellEditable(Object cell)
	{
		mxCell theCell = (mxCell)cell;
		
		if (theCell instanceof DatatypeCell || theCell instanceof SubClassEdgeCell) {
			return false;
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
	
	public ClassCell addClass(OWLEntity owlEntity, double positionX, double positionY) {
		log.info("[CoModIDE:SchemaDiagram] Adding OWL Class" + owlEntity.toString());
		if (getCell(owlEntity.toString())!= null) {
			return (ClassCell)getCell(owlEntity.toString());
		}
		ClassCell cell = new ClassCell(owlEntity, positionX, positionY);
		this.addCell(cell);
		return cell;
	}

	public DatatypeCell addDatatype(OWLEntity owlEntity, double positionX, double positionY) {
		log.info("[CoModIDE:SchemaDiagram] Adding OWL Datatype " + owlEntity.toString());
		DatatypeCell cell = new DatatypeCell(owlEntity, positionX, positionY);
		this.addCell(cell);
		return cell;
	}
	
	public SubClassEdgeCell addSubClassEdge(ClassCell parentClass, ClassCell subClass) {
		SubClassEdgeCell edge = new SubClassEdgeCell();
		this.addEdge(edge, this.getDefaultParent(), parentClass, subClass, null);
		return edge;
	}
	
	public PropertyEdgeCell addPropertyEdge(OWLProperty owlProperty, ClassCell domainCell, mxCell rangeCell) {
		log.info("[CoModIDE:SchemaDiagram] Adding OWL Property " + owlProperty.toString());
		PropertyEdgeCell edge = new PropertyEdgeCell(owlProperty);
		this.addEdge(edge, this.getDefaultParent(), domainCell, rangeCell, null);
		return edge;
	}
	
	public void removeOwlEntity(OWLEntity owlEntity) {
		// Figure out which cells that need to be removed.
		List<mxCell> cellsToRemove = new ArrayList<mxCell>();
		if (owlEntity.isOWLClass() || owlEntity.isOWLObjectProperty())
		{
			log.info("[CoModIDE:SchemaDiagram] Removing class or object property cells for '" + owlEntity.toString() + "'");
			cellsToRemove.addAll(findCellsById(owlEntity.toString()));
		}
		else if (owlEntity.isOWLDataProperty()) {
			log.info("[CoModIDE:UFOH] Removing data property cells for '" + owlEntity.toString() + "'");
			List<mxCell> dataPropertyCellsToRemove = findCellsById(owlEntity.toString());
			List<mxCell> dataTypeCellsToRemove = new ArrayList<mxCell>();
			for (mxCell dataPropertyCell: dataPropertyCellsToRemove) {
				dataTypeCellsToRemove.add((mxCell)dataPropertyCell.getTarget());
			}
			cellsToRemove.addAll(dataTypeCellsToRemove);
			cellsToRemove.addAll(dataPropertyCellsToRemove);
		}

		// If diagram is unlocked, proceed with cell deletion.
		if (!isLock())
		{
			model.beginUpdate();
			try
			{
				removeCells(cellsToRemove.toArray());
			}
			finally
			{
				model.endUpdate();
			}
		}
	}
}
