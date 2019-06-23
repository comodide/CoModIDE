package com.comodide.rendering.editor;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.HasProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.axiomatization.AxiomManager;
import com.comodide.axiomatization.EdgeContainer;
import com.comodide.rendering.PositioningOperations;
import com.comodide.rendering.sdont.model.SDEdge;
import com.comodide.rendering.sdont.model.SDNode;
import com.comodide.rendering.sdont.viz.mxVertexMaker;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGraphModel;

public class UpdateFromOntologyHandler
{
	/** Logging */
	private static final Logger log = LoggerFactory.getLogger(UpdateFromOntologyHandler.class);

	/** Used for obtaining human readable lables */
	private static final ShortFormProvider shortFormProvider = new SimpleShortFormProvider();

	
	/** Used for updating the graph when handling changes */
	private SchemaDiagram schemaDiagram;
	private mxGraphModel  graphModel;

	/** Singleton reference to AxiomManager for parsing Axioms */
	private AxiomManager axiomManager;

	/** Used for creating the styled mxcells for the graph */
	private mxVertexMaker vertexMaker;

	/** Empty Constructor */
	public UpdateFromOntologyHandler()
	{

	}

	public UpdateFromOntologyHandler(SchemaDiagram schemaDiagram, OWLModelManager modelManager)
	{
		this.schemaDiagram = schemaDiagram;
		this.graphModel = (mxGraphModel) schemaDiagram.getModel();

		this.axiomManager = AxiomManager.getInstance(modelManager, schemaDiagram);

		this.vertexMaker = new mxVertexMaker(schemaDiagram);
	}

	public void handle(OWLOntologyChange change)
	{
		/* Determine the type of OntologyChange */
		// Unpack the OntologyChange
		OWLOntology ontology = change.getOntology();
		OWLAxiom    axiom    = change.getAxiom();
		// Add or remove from graph
		if (this.schemaDiagram.isLock())
		{
			// Do nothing
		}
		else if (change.isAddAxiom())
		{
			// When a class is created it is declared. We extract the OWLEntity
			// From the declaration. It does not render declared properties.
			if (axiom.isOfType(AxiomType.DECLARATION))
			{
				handleClass(ontology, axiom);
			}
			// General class axioms are parsed and rendered as an edge
			else if (axiom.isOfType(AxiomType.SUBCLASS_OF))
			{
				handleGeneralAxiom(ontology, axiom);
			}
			else if (axiom.isOfType(AxiomType.OBJECT_PROPERTY_DOMAIN))
			{
				handleObjectPropertyDomainOrRange(ontology, (OWLObjectPropertyDomainAxiom)axiom);
			}
			else if (axiom.isOfType(AxiomType.OBJECT_PROPERTY_RANGE))
			{
				handleObjectPropertyDomainOrRange(ontology, (OWLObjectPropertyRangeAxiom)axiom);
			}
			else if (axiom.isOfType(AxiomType.DATA_PROPERTY_DOMAIN))
			{
				handleDataPropertyDomainOrRange(ontology, (OWLDataPropertyDomainAxiom)axiom);
			}
			else if (axiom.isOfType(AxiomType.DATA_PROPERTY_RANGE))
			{
				handleDataPropertyDomainOrRange(ontology, (OWLDataPropertyRangeAxiom)axiom);
			}
			else
			{
				if (!axiom.isOfType(AxiomType.ANNOTATION_ASSERTION))
				{
					log.warn("[CoModIDE:UFOH] Unsupported AddAxiom: " + axiom.getAxiomWithoutAnnotations().toString());
				}
			}
		}
		else if (change.isRemoveAxiom())
		{
			// TODO removeAxiom implementation in Progress
			if (axiom.isOfType(AxiomType.DECLARATION))
			{
				removeClass(axiom);
			}
			else
			{
				if (!axiom.isOfType(AxiomType.ANNOTATION_ASSERTION))
				{
					log.warn("[CoModIDE:UFOH] Unsupported AddAxiom: " + axiom.getAxiomWithoutAnnotations().toString());
				}
			}
		}
		else
		{
			log.warn("[CoModIDE:UFOH] Unsupported change to the ontology.");
		}
	}

	public void handleClass(OWLOntology ontology, OWLAxiom axiom)
	{
		// The Cell representing the Class or Datatype
		Object cell = null;
		// Unpack data from Declaration
		OWLDeclarationAxiom declaration = (OWLDeclarationAxiom) axiom;
		OWLEntity           owlEntity   = declaration.getEntity();
		// Only handle Class or Datatype
		if (owlEntity.isOWLClass() || owlEntity.isOWLDatatype())
		{
			log.info("[CoModIDE:UFOH] Handling class or datatype change.");

			// Retrieve the opla-sd annotations for positions
			Pair<Double, Double> xyCoords = PositioningOperations.getXYCoordsForEntity(owlEntity, ontology);
			// The given coordinates might come from the ontology, or they might have been created by PositioningAnnotations (if none were given
			// in the ontology at the outset). To guard against the latter case, persist them right away.
			PositioningOperations.updateXYCoordinateAnnotations(owlEntity, ontology, xyCoords.getLeft(), xyCoords.getRight());
			// Package the node
			SDNode node = new SDNode(owlEntity, owlEntity.isOWLDatatype(), xyCoords);
			// Create the node
			cell = vertexMaker.makeNode(node);
			// We do not want to add a duplicate cell at this time
			// By adding this "catch" we prevent the loopback "feature" of adding a Class
			// via CoModIDE propagating via this handler to add a duplicate cell
			// TODO support duplicate cells in a sane manner
			boolean isPresent = this.schemaDiagram.isLock();

			// Update the SchemaDiagram if the cell isn't present
			if (!isPresent)
			{
				graphModel.beginUpdate();
				try
				{
					schemaDiagram.addCell(cell);
				}
				finally
				{
					graphModel.endUpdate();
				}
			}
		}
		else
		{
			// Do Nothing
			// We do not want to add properties as nodes.
		}
	}

	public void handleGeneralAxiom(OWLOntology ontology, OWLAxiom axiom)
	{
		log.info("[CoModIDE:UFOH] Handling property via GCI.");
		// Parse the axiom
		EdgeContainer edge = axiomManager.parseSimpleAxiom((OWLSubClassOfAxiom) axiom);
		// Unpack
		String id     = edge.getId();
		Object source = edge.getSource();
		Object target = edge.getTarget();
		String style  = edge.getStyle();
		// Update the SchemaDiagram
		graphModel.beginUpdate();
		try
		{
			// If null is passed as parent, a convenience function in the chain
			// will call getDefaultParent()
			// TODO: the value added below should be a SDEdge, not EdgeContainer
			schemaDiagram.insertEdge(null, id, edge, source, target, style);
		}
		finally
		{
			graphModel.endUpdate();
		}

	}

	private void handleObjectPropertyDomainOrRange(OWLOntology ontology, HasProperty<OWLObjectPropertyExpression> axiom)
	{
		log.info(String.format("[CoModIDE:UFOH] Handing Object Property Domain or Range Restriction '%s'.", axiom.toString()));
		
		OWLObjectProperty objectProperty = axiom.getProperty().asOWLObjectProperty();
		String propertyName = shortFormProvider.getShortForm(objectProperty);
		Set<OWLObjectPropertyDomainAxiom> domainAxioms = ontology.getObjectPropertyDomainAxioms(objectProperty);
		Set<OWLObjectPropertyRangeAxiom> rangeAxioms = ontology.getObjectPropertyRangeAxioms(objectProperty);
		
		if (domainAxioms.size() != 1 || rangeAxioms.size() != 1) {
			log.info(String.format("[CoModIDE:UFOH] Property '%s' cannot be processed as it does not have exactly 1 domain and range.", objectProperty.toString()));
		}
		else {
			// Get the things we will be linking up
			OWLClass domain = ((OWLObjectPropertyDomainAxiom) domainAxioms.toArray()[0]).getDomain().asOWLClass();
			String domainLabel = shortFormProvider.getShortForm(domain);
			OWLClass range = ((OWLObjectPropertyRangeAxiom) rangeAxioms.toArray()[0]).getRange().asOWLClass();
			String rangeLabel = shortFormProvider.getShortForm(range);
			
			// Iterate through all cells on the canvas to find the nodes whose IDs matches the domain/range we want to link from/to
			// Note the null check on source and target cells below; we cannot put the action code below into this loop, or we will
			// get a ConcurrentModificationException (i.e., in-place editing while iterating) on the cells map.
			mxCell sourceCell = null;
			mxCell targetCell = null;
			Map<String, Object> cells = ((mxGraphModel)this.schemaDiagram.getModel()).getCells();
			for (String key: cells.keySet()) {
				mxCell cell = (mxCell)cells.get(key);
				if (cell.getId().equals(domainLabel)) {
					sourceCell = cell;
				}
				if (cell.getId().equals(rangeLabel)) {
					targetCell = cell;
				}
				if (sourceCell != null && targetCell != null) {
					// We have found both ends of the edge
					break;
				}
			}
			
			// If we got a hit on the above search through the cells, then we have source and target cells to draw the edge between
			if (sourceCell != null && targetCell != null) {				
				// Only proceed if the source and target cells have backing SDNodes
				if (sourceCell.getValue() instanceof SDNode && targetCell.getValue() instanceof SDNode) {
					SDNode domainNode = (SDNode)sourceCell.getValue();
					SDNode rangeNode = (SDNode)targetCell.getValue();
					SDEdge sdEdge = new SDEdge(domainNode, rangeNode, false, objectProperty);
					
					// TODO: Check if locking would be needed here like for classes?
					// Update the SchemaDiagram
					graphModel.beginUpdate();
					try
					{
						// If null is passed as parent, a convenience function in the chain
						// will call getDefaultParent()
						schemaDiagram.insertEdge(null, propertyName, sdEdge, sourceCell, targetCell, SDConstants.standardEdgeStyle);
					}
					finally
					{
						graphModel.endUpdate();
					}
				}
			}
			
		}
	}

	private void handleDataPropertyDomainOrRange(OWLOntology ontology, HasProperty<OWLDataPropertyExpression> axiom)
	{
		log.info(String.format("[CoModIDE:UFOH] Handing Data Property Domain or Range Restriction '%s'.", axiom.toString()));
		
		OWLDataProperty dataProperty = axiom.getProperty().asOWLDataProperty();
		String propertyName = shortFormProvider.getShortForm(dataProperty);
		Set<OWLDataPropertyDomainAxiom> domainAxioms = ontology.getDataPropertyDomainAxioms(dataProperty);
		Set<OWLDataPropertyRangeAxiom> rangeAxioms = ontology.getDataPropertyRangeAxioms(dataProperty);
		
		if (domainAxioms.size() != 1 || rangeAxioms.size() != 1) {
			log.info(String.format("[CoModIDE:UFOH] Property '%s' cannot be processed as it does not have exactly 1 domain and range.", dataProperty.toString()));
		}
		else {
			// Get the things we will be linking up
			OWLClass domain = ((OWLDataPropertyDomainAxiom) domainAxioms.toArray()[0]).getDomain().asOWLClass();
			String domainLabel = shortFormProvider.getShortForm(domain);
			OWLDatatype range = ((OWLDataPropertyRangeAxiom) rangeAxioms.toArray()[0]).getRange().asOWLDatatype();
			
			// Iterate through all cells on the canvas to find the node whose ID matches the domain we want to link from
			// Note the null check on sourceCell below; we cannot put the action code below into this loop, or we will
			// get a ConcurrentModificationException (i.e., in-place editing while iterating) on the cells map.
			mxCell sourceCell = null;
			Map<String, Object> cells = ((mxGraphModel)this.schemaDiagram.getModel()).getCells();
			for (String key: cells.keySet()) {
				mxCell cell = (mxCell)cells.get(key);
				if (cell.getId().equals(domainLabel)) {
					sourceCell = cell;
					break;
				}
			}
			
			// If we got a hit on the above search through the cells, then we have a source cell to draw the edge from
			if (sourceCell != null) {
				// At this point we have the origin cell, i.e., the domain of the property
				// Now we need to find the coordinates for the target cell, i.e., the range of the property
				// Note that we are fetching the positioning axioms from the data property,
				// which has identity (as the target datatype might not have identity)
				Pair<Double, Double> xyCoords = PositioningOperations.getXYCoordsForEntity(dataProperty, ontology);
				
				// The given coordinates might come from the ontology, or they might have been created by PositioningAnnotations (if none were given
				// in the ontology at the outset). To guard against the latter case, assign them back to the model right away.
				PositioningOperations.updateXYCoordinateAnnotations(dataProperty, ontology, xyCoords.getLeft(), xyCoords.getRight());
				
				// Now, create a new node and corresponding cell for this datatype
				SDNode rangeNode = new SDNode(range, true, xyCoords);
				Object targetCell = vertexMaker.makeNode(rangeNode);
				
				// Only proceed if the sourceCell has a backing SDNode
				if (sourceCell.getValue() instanceof SDNode) {
					SDNode domainNode = (SDNode)sourceCell.getValue();
					SDEdge sdEdge = new SDEdge(domainNode, rangeNode, false, dataProperty);
					
					// TODO: Check if locking would be needed here like for classes?
					// Update the SchemaDiagram
					graphModel.beginUpdate();
					try
					{
						schemaDiagram.addCell(targetCell);
						// If null is passed as parent, a convenience function in the chain
						// will call getDefaultParent()
						schemaDiagram.insertEdge(null, propertyName, sdEdge, sourceCell, targetCell, SDConstants.standardEdgeStyle);
					}
					finally
					{
						graphModel.endUpdate();
					}
				}
			}
		}
	}
	
	public void removeClass(OWLAxiom axiom)
	{
		// Unpack
		OWLDeclarationAxiom declarationAxiom = (OWLDeclarationAxiom) axiom;
		OWLEntity owlEntity = declarationAxiom.getEntity();

		// Act only if class
		if(owlEntity.isOWLClass())
		{
			// Get the label
			String className = shortFormProvider.getShortForm(owlEntity);
			// Find the associated cell
			Object classCell = this.schemaDiagram.getCell(className);
			
			// Remove that cell from the schema diagram
			graphModel.beginUpdate();
			try
			{
				schemaDiagram.removeCells(new Object[] {classCell});
			}
			finally
			{
				graphModel.endUpdate();
			}
		}
		else
		{
			log.warn("debug:ufoh wasn't owl class as expected");
		}
	}
}
