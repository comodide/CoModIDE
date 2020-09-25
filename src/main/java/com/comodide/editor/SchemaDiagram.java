package com.comodide.editor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.comodide.axiomatization.OplaAnnotationManager;
import com.comodide.configuration.ComodideConfiguration;
import com.comodide.editor.changehandlers.LabelChangeHandler;
import com.comodide.editor.model.ClassCell;
import com.comodide.editor.model.ComodideCell;
import com.comodide.editor.model.DatatypeCell;
import com.comodide.editor.model.ModuleCell;
import com.comodide.editor.model.PropertyEdgeCell;
import com.comodide.editor.model.SubClassEdgeCell;
import com.comodide.exceptions.ComodideException;
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
	private static final String pf  = "[CoModIDE:SchemaDiagram] ";

	/** Holds the edge to be used as a template for inserting new edges. */
	protected Object edgeTemplate;

	/** Used for handling the changes to cell lables. i.e. add/remove axioms */
	private LabelChangeHandler labelChangeHandler;

	/** Used to interoperate with loaded ontologies. */
	private final OWLModelManager modelManager;

	/** Used to prevent loopback from adding a class via tab */
	private boolean lock = false;

	/**
	 * Used to clear out the schema diagram (needed when re-rendering a new
	 * ontology).
	 * 
	 * Turns off the CoModIDE listener for CELLS_REMOVED first, so no changes to the
	 * underlying ontology is carried out if this method is called.
	 */
	public void clear()
	{
		this.removeListener(cellsRemovedHandler);
		this.removeCells(this.getChildCells(this.getDefaultParent()));
		this.addListener(mxEvent.CELLS_REMOVED, cellsRemovedHandler);
	}

	/**
	 * Listener for mxEvents.CELLS_ADDED event; used to add subClassOf axiom to the
	 * model when a new subClassOf edge is drawn on the canvas.
	 */
	protected mxIEventListener cellsAddedHandler = new mxIEventListener()
	{
		@Override
		public void invoke(Object sender, mxEventObject evt)
		{
			Object[] cells = (Object[]) evt.getProperty("cells");
			if (cells.length == 1)
			{
				Object cell = cells[0];

				if (cell instanceof SubClassEdgeCell)
				{
					SubClassEdgeCell subclassEdgeCell = (SubClassEdgeCell) cells[0];

					// Retrieve the terminals of this cell
					mxICell sourceCell = subclassEdgeCell.getTerminal(true);
					mxICell targetCell = subclassEdgeCell.getTerminal(false);

					if (sourceCell instanceof ClassCell && targetCell instanceof ClassCell)
					{
						model.beginUpdate();
						lock = true; // prevent loopback during addaxiom
						try
						{
							// Generate and add the subClassOf axioms to the active ontology
							ClassCell          sourceClassCell = (ClassCell) sourceCell;
							ClassCell          targetClassCell = (ClassCell) targetCell;
							OWLClass           subClass        = sourceClassCell.getEntity().asOWLClass();
							OWLClass           superClass      = targetClassCell.getEntity().asOWLClass();
							OWLOntology        ontology        = modelManager.getActiveOntology();
							OWLOntologyManager ontologyManager = ontology.getOWLOntologyManager();
							OWLSubClassOfAxiom newAxiom        = ontologyManager.getOWLDataFactory()
									.getOWLSubClassOfAxiom(subClass, superClass);
							ontologyManager.addAxiom(ontology, newAxiom);
						}
						finally
						{
							lock = false; // always make sure unlocked at this stage
							model.endUpdate();
						}
					}
				}
				// if the comodide cell is not added to the default parent
				// Then it was added to a module.
				// This is generally fired when a single cell is
				else if (!evt.getProperty("parent").equals(getDefaultParent()) && ((ComodideCell) cell).isNamed())
				{
					// This is the cell that was added to the module
					ComodideCell comodideCell = (ComodideCell) cell;
					IRI          subject      = comodideCell.getEntity().getIRI();
					// This is what it was added to
					// It is also the case that this holds
					// comodideCell.getParent().equals(moduleCell)
					ModuleCell moduleCell = (ModuleCell) evt.getProperty("parent");
					// We also must provide an annotation for every super module, as well.
					ArrayList<IRI> values = new ArrayList<>();
					values.add(moduleCell.getEntity().getIRI()); // initially populate
					while (!moduleCell.getParent().equals(getDefaultParent()))
					{
						// This typecast should always succeed (nothing is nested into non-module cells
						// And default parent (while not a modulecell) should not even enter this loop
						moduleCell = (ModuleCell) moduleCell.getParent();
						// add the IRI
						values.add(moduleCell.getEntity().getIRI());
					}
					// Create the annotations for all "super modules"
					OplaAnnotationManager oplaAnnotationManager = OplaAnnotationManager.getInstance(modelManager);
					oplaAnnotationManager.createIsNativeToAnnotations(subject, values);
				}
				else if (cell instanceof ClassCell)
				{
					/*
					 * While the adding of the appropriate axioms and declarations are handled by
					 * the SDTransferHandler and PatternLibraryInstantiator. We still want to check
					 * the annotations on these cells in case that they are dropped directly into a
					 * module This also covers the case where a cell has been removed from a module
					 * into another.
					 */
					// This is the cell that was added
					ComodideCell comodideCell = (ComodideCell) cell;
					IRI          subject      = comodideCell.getEntity().getIRI();
					Object       parent       = comodideCell.getParent();
					// will hold the "allowed" annotations
					ArrayList<IRI> values = new ArrayList<>();

					while (!parent.equals(getDefaultParent()))
					{
						ComodideCell p = (ComodideCell) parent;

						values.add(p.getEntity().getIRI());

						parent = (ComodideCell) p.getParent();
					}
					// Create the annotations for all "super modules"
					// Also REMOVE all the other isNativeTo annotations
					OplaAnnotationManager oplaAnnotationManager = OplaAnnotationManager.getInstance(modelManager);
					oplaAnnotationManager.adjustIsNativeToAnnotations(subject, values);
				}
				else
				{
					// This else catches all other cells: propertyedges and class cells.
					// We don't really need to do anything here because the handling is done by the
					// changeFromOntologyHandler, instead of the other way around.
				}
			}
			else
			{
				// This is fired (at least) when a grouping (create a module) happens.
				// However, we don't want to take action on this until the module has been
				// named, so we defer to the cellLabelChanged eventhandler
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
				if (cell instanceof ClassCell || cell instanceof DatatypeCell || cell instanceof ModuleCell)
				{
					List<OWLEntity> positioningEntities = new ArrayList<OWLEntity>();
					// If this is a datatype cell, put the OPLa-SD positioning annotations on the
					// (implicitly ingoing) attached edges
					if (cell instanceof DatatypeCell)
					{
						for (int i = 0; i < cell.getEdgeCount(); i++)
						{
							mxICell candidateEdge = cell.getEdgeAt(i);
							if (candidateEdge instanceof PropertyEdgeCell)
							{
								PropertyEdgeCell propertyCell = (PropertyEdgeCell) candidateEdge;
								positioningEntities.add(propertyCell.getEntity());
							}
						}
					}
					// Else, if it is a class or module, just put them on the class or module
					else
					{
						// a cell that is a node, i.e. not an edge cell
						ComodideCell nodeCell = (ComodideCell) cell;
						positioningEntities.add(nodeCell.getEntity());
					}

					// Check which of the loaded ontologies that hosts the positioning entities
					// and update annotations in those ontologies
					lock = true; // prevent loopback during addaxiom
					for (OWLEntity positioningEntity : positioningEntities)
					{
						OWLOntology activeOntology = modelManager.getActiveOntology();
						if (activeOntology.containsEntityInSignature(positioningEntity.getIRI()))
						{
							PositioningOperations.updateXYCoordinateAnnotations(positioningEntity, activeOntology, newX,
									newY);
						}
					}
					// Unlock afterwards
					lock = false;
				}
			}
		}
	};

	protected mxIEventListener cellsRemovedHandler = new mxIEventListener()
	{

		@Override
		public void invoke(Object sender, mxEventObject evt)
		{
			Object[]           cells           = (Object[]) evt.getProperty("cells");
			OWLOntology        ontology        = modelManager.getActiveOntology();
			OWLOntologyManager ontologyManager = modelManager.getOWLOntologyManager();
			Set<OWLOntology>   ontologies      = new HashSet<OWLOntology>();
			ontologies.add(ontology);
			OWLEntityRemover remover = new OWLEntityRemover(ontologies);
			model.beginUpdate();
			lock = true; // prevent
			// loopback
			// during
			// addaxiom
			try
			{
				for (Object c : cells)
				{
					// Unpack data from cells
					mxCell cell = (mxCell) c;

					if (cell instanceof ClassCell)
					{
						ClassCell classCell     = (ClassCell) cell;
						OWLClass  classToRemove = classCell.getEntity().asOWLClass();
						classToRemove.accept(remover);
					}
					else if (cell instanceof SubClassEdgeCell)
					{
						SubClassEdgeCell   subClassEdgeCell      = (SubClassEdgeCell) cell;
						OWLClass           subClass              = ((ClassCell) subClassEdgeCell.getSource())
								.getEntity().asOWLClass();
						OWLClass           superClass            = ((ClassCell) subClassEdgeCell.getTarget())
								.getEntity().asOWLClass();
						OWLSubClassOfAxiom subClassAxiomToRemove = ontologyManager.getOWLDataFactory()
								.getOWLSubClassOfAxiom(subClass, superClass);
						ontologyManager.removeAxiom(ontology, subClassAxiomToRemove);
					}
					else if (cell instanceof DatatypeCell)
					{
						DatatypeCell cellToRemove     = (DatatypeCell) cell;
						OWLDatatype  datatypeToRemove = cellToRemove.getEntity().asOWLDatatype();
						// We only remove from the ontology if this is a custom datatype.
						if (!datatypeToRemove.isBuiltIn())
						{
							datatypeToRemove.accept(remover);
						}
					}
					else if (cell instanceof PropertyEdgeCell)
					{
						PropertyEdgeCell cellToRemove = (PropertyEdgeCell) cell;
						if (cellToRemove.getTarget() instanceof DatatypeCell)
						{
							// This is a data property; remove the dangling datatype cell also
							removeCells(new Object[] { cellToRemove.getTarget() });
						}

						// If we are configured to delete the property declarations, simply delete
						// the property; any other axioms will disappear along with it.
						if (ComodideConfiguration.getDeletePropertyDeclarations())
						{
							cellToRemove.getEntity().accept(remover);
						}
						else
						{
							Set<OWLAxiom> axiomsToRemove = new HashSet<OWLAxiom>();
							OWLEntity     targetEntity   = ((ComodideCell) cellToRemove.getTarget()).getEntity();
							if (cellToRemove.getEntity() instanceof OWLObjectProperty)
							{
								OWLObjectProperty property = (OWLObjectProperty) cellToRemove.getEntity();
								axiomsToRemove.addAll(ontology.getObjectPropertyDomainAxioms(property));
								axiomsToRemove.addAll(ontology.getObjectPropertyRangeAxioms(property));

								OWLObjectAllValuesFrom  allValuesFrom  = ontologyManager.getOWLDataFactory()
										.getOWLObjectAllValuesFrom(property, targetEntity.asOWLClass());
								OWLObjectSomeValuesFrom someValuesFrom = ontologyManager.getOWLDataFactory()
										.getOWLObjectSomeValuesFrom(property, targetEntity.asOWLClass());

								for (OWLSubClassOfAxiom subClassAxiom : ontology.getAxioms(AxiomType.SUBCLASS_OF))
								{
									if (subClassAxiom.getSubClass().equals(allValuesFrom)
											|| subClassAxiom.getSuperClass().equals(allValuesFrom)
											|| subClassAxiom.getSubClass().equals(someValuesFrom)
											|| subClassAxiom.getSuperClass().equals(someValuesFrom))
									{
										axiomsToRemove.add(subClassAxiom);
									}
								}

							}
							else if (cellToRemove.getEntity() instanceof OWLDataProperty)
							{
								OWLDataProperty property = (OWLDataProperty) cellToRemove.getEntity();
								axiomsToRemove.addAll(ontology.getDataPropertyDomainAxioms(property));
								axiomsToRemove.addAll(ontology.getDataPropertyRangeAxioms(property));

								OWLDataAllValuesFrom  allValuesFrom  = ontologyManager.getOWLDataFactory()
										.getOWLDataAllValuesFrom(property, targetEntity.asOWLDatatype());
								OWLDataSomeValuesFrom someValuesFrom = ontologyManager.getOWLDataFactory()
										.getOWLDataSomeValuesFrom(property, targetEntity.asOWLDatatype());

								for (OWLSubClassOfAxiom subClassAxiom : ontology.getAxioms(AxiomType.SUBCLASS_OF))
								{
									if (subClassAxiom.getSubClass().equals(allValuesFrom)
											|| subClassAxiom.getSuperClass().equals(allValuesFrom)
											|| subClassAxiom.getSubClass().equals(someValuesFrom)
											|| subClassAxiom.getSuperClass().equals(someValuesFrom))
									{
										axiomsToRemove.add(subClassAxiom);
									}
								}
							}
							else
							{
								// empty else
							}
							ontologyManager.removeAxioms(ontology, axiomsToRemove);
						}
					}
					else if (cell instanceof ModuleCell)
					{
						// Because edges are not added as children to modules (cell groups)
						// We don't have to remove them; it's handled by other parts of comodide
						this.removeModule(remover, cell);
					}
					else
					{
						// empty else
					}
				}
			}
			finally
			{
				modelManager.applyChanges(remover.getChanges());
				lock = false; // always make sure unlocked at this stage
				model.endUpdate();
			}
		}

		/**
		 * This method is needed to recursively delete nested modules.
		 */
		private void removeModule(OWLEntityRemover remover, Object cell)
		{
			Object[] children = getChildCells(cell);
			for (Object child : children)
			{
				if (child instanceof ClassCell)
				{
					ClassCell classCell     = (ClassCell) child;
					OWLClass  classToRemove = classCell.getEntity().asOWLClass();
					classToRemove.accept(remover);
				}
				else if (child instanceof ModuleCell)
				{
					removeModule(remover, child);
				}
				else
				{
					// empty else
				}
			}
		}
	};

	protected mxIEventListener groupCellsHandler = new mxIEventListener()
	{
		@Override
		public void invoke(Object sender, mxEventObject evt)
		{
			// This handle is included for future extensibility.
			// Functionality should instead be handled during the
			// CellsAdded and CellLabelChanged hooks
		}
	};

	/** This hook should be called when a module is deleted, probably */
	protected mxIEventListener ungroupCellsHandler = new mxIEventListener()
	{
		@Override
		public void invoke(Object sender, mxEventObject evt)
		{
			// This handle is included for future extensibility.
		}
	};

	private mxIEventListener cellsFoldedHandler = new mxIEventListener()
	{

		@Override
		public void invoke(Object sender, mxEventObject evt)
		{
			// Get the Cell from the event.
			Object[] cells = (Object[]) evt.getProperty("cells");
			// For each module that has been folded, switch their style
			for (Object o : cells)
			{
				// This should always be a module
				ModuleCell module = (ModuleCell) o;
				// Switch from grey box to "hidden complexity" glyph style.
				module.switchStyle();
			}
		}

	};

	/**
	 * Custom graph that defines the alternate edge style to be used when the middle
	 * control point of edges is double clicked (flipped).
	 */
	public SchemaDiagram(OWLModelManager modelManager)
	{
		// setAlternateEdgeStyle("edgeStyle=mxEdgeStyle.ElbowConnector;elbow=vertical");
		this.modelManager = modelManager;
		this.labelChangeHandler = new LabelChangeHandler(modelManager, this);
		this.allowDanglingEdges = false;
		this.cellsResizable = false;
		this.autoSizeCells = true;
		// Add all listeners for custom behavior
		this.addListener(mxEvent.CELLS_MOVED, cellsMovedHandler);
		this.addListener(mxEvent.CELLS_ADDED, cellsAddedHandler);
		this.addListener(mxEvent.CELLS_REMOVED, cellsRemovedHandler);
		this.addListener(mxEvent.GROUP_CELLS, groupCellsHandler);
		this.addListener(mxEvent.UNGROUP_CELLS, ungroupCellsHandler);
		this.addListener(mxEvent.CELLS_FOLDED, cellsFoldedHandler);
		// Loads styling information from an external file.
		mxCodec  codec = new mxCodec();
		Document doc   = mxUtils
				.loadDocument(ComodideEditor.class.getResource("/resources/comodide-style.xml").toString());
		codec.decode(doc.getDocumentElement(), getStylesheet());
	}

	@Override
	public boolean isCellConnectable(Object cell)
	{
		// If this is a class cell but it has not yet been named, then it means
		// it was just dropped and if so, it should NOT be connectable.
		if (cell instanceof ClassCell)
		{
			if (!((ClassCell) cell).isNamed())
			{
				return false;
			}
		}
		// In all other cases, defer to parent method
		return super.isCellConnectable(cell);
	}

	@Override
	public boolean isValidSource(Object cell)
	{
		// If this is a datatype cell then it should NOT be
		// a valid source of outgoing edges, return false.
		if (cell instanceof DatatypeCell)
		{
			return false;
		}
		// In all other cases, defer to parent method
		return super.isValidSource(cell);
	}

	@Override
	public boolean isValidTarget(Object cell)
	{
		// If this is a datatype cell then it should have
		// at most one incoming edge; if existing
		// edge count > 0, return false.
		if (cell instanceof DatatypeCell)
		{
			if (((DatatypeCell) cell).getEdgeCount() > 0)
			{
				return false;
			}
		}
		// Do not defer to the parent method because it is broken by our
		// isValidSource customization above -- instead, copied from super.isValidSource
		return (cell == null && allowDanglingEdges)
				|| (cell != null && (!model.isEdge(cell) || isConnectableEdges()) && isCellConnectable(cell));
	}

	@Override
	public void cellLabelChanged(Object cell, Object newValue, boolean autoSize)
	{
		log.info(pf + "cellLabelChanged intercepted.");
		/* Process object into useful formats. */
		ComodideCell changedCell = (ComodideCell) cell;
		String       newLabel    = (String) newValue;
		String       oldLabel    = (String) changedCell.getValue();

		// Ignore false positives, e.g. edit double-clicks that don't change anything
		if (newLabel.equals(oldLabel))
		{
			return;
		}

		model.beginUpdate();
		this.lock = true; // prevent loopback during addaxiom
		try
		{
			// Handle the label change
			// Children of cell (used for module changes)
			Object[]  childCells = this.getChildCells(changedCell, true, true);
			OWLEntity entity     = labelChangeHandler.handle(changedCell, newLabel, childCells);
			changedCell.setEntity(entity);
			model.setValue(cell, newLabel);

			// Autosize, if necessary.
			if (autoSize)
			{
				cellSizeUpdated(changedCell, false);
			}
		}
		catch (ComodideException ex)
		{
			JOptionPane.showMessageDialog(null, ex.getMessage());
			log.info(ex.getMessage());
		}
		finally
		{
			this.lock = false; // always make sure unlocked at this stage
			model.endUpdate();
		}
	}

	/**
	 * This method is a convenience method for finding a cell holding a particular
	 * entity in the schema diagram. We expect IRIs sent in here to be enclosed in
	 * <>, since that is what OWLAPI generates as toString() for named entities. Due
	 * to the creation mechanism of nodes via drag-and-drop ids are added before the
	 * id is assigned via class/property creation, therefore we must iterate through
	 * cells and match their current id instead of the id that they had when they
	 * were dragged and dropped (an arbitrary integer).
	 * 
	 * @param iri
	 */
	public mxCell getCell(OWLEntity entity)
	{
		Map<String, Object> cells = ((mxGraphModel) model).getCells();

		for (Object o : cells.values())
		{
			// Some cells, e.g., the root, aren't ComodideCell
			if (o instanceof ComodideCell)
			{
				ComodideCell cell = (ComodideCell) o;

				if (cell.getEntity().equals(entity))
				{
					return cell;
				}
			}
		}

		return null;
	}

	public List<mxCell> findCellsByEntity(OWLEntity entity)
	{
		return findCellsByIri(entity.getIRI());
	}

	public List<mxCell> findCellsByIri(IRI iri)
	{
		List<mxCell>        foundCells     = new ArrayList<mxCell>();
		Map<String, Object> cellsOnDiagram = ((mxGraphModel) model).getCells();
		for (Object o : cellsOnDiagram.values())
		{
			// Some cells, e.g., the root, aren't ComodideCell
			if (o instanceof ComodideCell)
			{
				ComodideCell candidateCell = (ComodideCell) o;
				if (candidateCell.isNamed() && candidateCell.getEntity().getIRI().equals(iri))
				{
					foundCells.add(candidateCell);
				}
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
		mxCell theCell = (mxCell) cell;

		if (theCell instanceof DatatypeCell || theCell instanceof SubClassEdgeCell)
		{
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

	public ClassCell addClass(OWLEntity owlEntity, double positionX, double positionY)
	{
		log.info(pf + "Adding OWL Class" + owlEntity.toString());
		if (getCell(owlEntity) != null)
		{
			return (ClassCell) getCell(owlEntity);
		}
		ClassCell cell = new ClassCell(owlEntity, positionX, positionY);
		this.addCell(cell);
		cellSizeUpdated(cell, false);
		return cell;
	}

	public DatatypeCell addDatatype(OWLEntity owlEntity, double positionX, double positionY)
	{
		log.info(pf + "Adding OWL Datatype " + owlEntity.toString());
		DatatypeCell cell = new DatatypeCell(owlEntity, positionX, positionY);
		this.addCell(cell);
		cellSizeUpdated(cell, false);
		return cell;
	}

	public SubClassEdgeCell addSubClassEdge(ClassCell parentClass, ClassCell subClass)
	{
		SubClassEdgeCell edge = new SubClassEdgeCell();
		this.addEdge(edge, this.getDefaultParent(), subClass, parentClass, null);
		return edge;
	}

	public void removeSubClassEdge(ClassCell parentClass, ClassCell subClass)
	{
		Object[] edges = this.getEdgesBetween(subClass, parentClass);
		for (Object edge : edges)
		{
			if (edge instanceof SubClassEdgeCell)
			{
				// If diagram is unlocked, proceed with cell deletion.
				if (!isLock())
				{
					model.beginUpdate();
					try
					{
						removeCells(new Object[] { edge });
					}
					finally
					{
						model.endUpdate();
					}
				}
			}
		}
	}

	public PropertyEdgeCell addPropertyEdge(OWLProperty owlProperty, ClassCell domainCell, mxCell rangeCell)
	{
		log.info(pf + "Adding OWL Property " + owlProperty.toString());
		PropertyEdgeCell edge = new PropertyEdgeCell(owlProperty);
		this.addEdge(edge, this.getDefaultParent(), domainCell, rangeCell, null);
		return edge;
	}

	public void removeOwlEntity(OWLEntity owlEntity)
	{
		// Figure out which cells that need to be removed.
		List<mxCell> cellsToRemove = new ArrayList<mxCell>();
		if (owlEntity.isOWLClass() || owlEntity.isOWLObjectProperty())
		{
			log.info(pf + "Removing class or object property cells for '" + owlEntity.toString() + "'");
			cellsToRemove.addAll(findCellsByEntity(owlEntity));
		}
		else if (owlEntity.isOWLDataProperty())
		{
			log.info(pf + "Removing data property cells for '" + owlEntity.toString() + "'");
			List<mxCell> dataPropertyCellsToRemove = findCellsByEntity(owlEntity);
			List<mxCell> dataTypeCellsToRemove     = new ArrayList<mxCell>();
			for (mxCell dataPropertyCell : dataPropertyCellsToRemove)
			{
				dataTypeCellsToRemove.add((mxCell) dataPropertyCell.getTarget());
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

	public ModuleCell addModule(OWLEntity owlEntity, double positionX, double positionY)
	{
		log.info(pf + "Adding a module " + owlEntity.toString());

		ModuleCell module;
		// Find or create the module cell
		if (getCell(owlEntity) != null)
		{
			/*
			 * TODO this assumes that there is no module and entity share the same name.
			 * This should hold if the ontology was created in CoModIDE in the first place.
			 */
			module = (ModuleCell) getCell(owlEntity);
		}
		else
		{
			module = new ModuleCell(owlEntity, positionX, positionY);
		}
		// Add the cell to the schema diagram.
		this.addCell(module);

		// TODO add module components to the module

		cellSizeUpdated(module, false);
		return module;
	}

	/**
	 * This method is overridden specifically to define the style of the created
	 * group cell.
	 * 
	 * the cells are included for information purposes the mxGeometry created in the
	 * ModuleCell will be over written later.
	 */
	@Override
	public Object createGroupCell(Object[] cells)
	{
		ModuleCell module = new ModuleCell(cells);
		return module;
	}
}
