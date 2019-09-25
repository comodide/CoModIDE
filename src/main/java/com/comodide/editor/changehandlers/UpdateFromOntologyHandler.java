package com.comodide.editor.changehandlers;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.HasProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLUnaryPropertyAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.axiomatization.AxiomManager;
import com.comodide.editor.SchemaDiagram;
import com.comodide.editor.model.ClassCell;
import com.comodide.editor.model.DatatypeCell;
import com.comodide.rendering.PositioningOperations;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGraphModel;

public class UpdateFromOntologyHandler
{
	/** Logging */
	private static final Logger log = LoggerFactory.getLogger(UpdateFromOntologyHandler.class);

	/** Used for updating the graph when handling changes */
	private SchemaDiagram schemaDiagram;
	private mxGraphModel  graphModel;

	/** Singleton reference to AxiomManager for parsing Axioms */
	private AxiomManager axiomManager;

	public UpdateFromOntologyHandler(SchemaDiagram schemaDiagram, OWLModelManager modelManager)
	{
		this.schemaDiagram = schemaDiagram;
		this.graphModel = (mxGraphModel) schemaDiagram.getModel();

		this.axiomManager = AxiomManager.getInstance(modelManager, schemaDiagram);
	}
	
	public void handleAddAxiom(OWLAxiom axiom, OWLOntology ontology) {
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
			handleObjectPropertyDomainOrRange(ontology, (OWLObjectPropertyDomainAxiom) axiom);
		}
		else if (axiom.isOfType(AxiomType.OBJECT_PROPERTY_RANGE))
		{
			handleObjectPropertyDomainOrRange(ontology, (OWLObjectPropertyRangeAxiom) axiom);
		}
		else if (axiom.isOfType(AxiomType.DATA_PROPERTY_DOMAIN))
		{
			handleDataPropertyDomainOrRange(ontology, (OWLDataPropertyDomainAxiom) axiom);
		}
		else if (axiom.isOfType(AxiomType.DATA_PROPERTY_RANGE))
		{
			handleDataPropertyDomainOrRange(ontology, (OWLDataPropertyRangeAxiom) axiom);
		}
		else if (axiom.isOfType(AxiomType.ANNOTATION_ASSERTION))
		{
			// Do nothing
			// Code to change positions in SchemaDiagram based on changes to EntityPosition
			// could possibly go here.
		}
		else
		{
			log.warn("[CoModIDE:UFOH] Unsupported AddAxiom: " + axiom.getAxiomWithoutAnnotations().toString());
		}
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
			handleAddAxiom(axiom, ontology);
		}
		else if (change.isRemoveAxiom())
		{
			handleRemoveAxiom(axiom, ontology);
		}
		else
		{
			log.warn("[CoModIDE:UFOH] Unsupported change to the ontology.");
		}
	}

	public void handleRemoveAxiom(OWLAxiom axiom, OWLOntology ontology) {
		log.warn("[CoModIDE:UFOH] I am being told to remove: " + axiom.getAxiomWithoutAnnotations().toString());
		if (axiom.isOfType(AxiomType.DECLARATION))
		{
			OWLDeclarationAxiom declarationAxiom = (OWLDeclarationAxiom) axiom;
			OWLEntity owlEntity = declarationAxiom.getEntity();
			schemaDiagram.removeOwlEntity(owlEntity);
		}
		else if (axiom.isOfType(AxiomType.SUBCLASS_OF))
		{
			// TODO: Implement me
			log.warn("[CoModIDE:UFOH] Subclass axiom removal TBD.");
		}
		else if (axiom.isOfType(AxiomType.OBJECT_PROPERTY_DOMAIN, AxiomType.OBJECT_PROPERTY_RANGE, AxiomType.DATA_PROPERTY_DOMAIN, AxiomType.DATA_PROPERTY_RANGE))
		{
			log.warn("[CoModIDE:UFOH] Removing axiom "  + axiom.getAxiomWithoutAnnotations().toString());
			// Unpack the property concerned
			@SuppressWarnings("unchecked")
			OWLUnaryPropertyAxiom<OWLProperty> propertyAxiom = (OWLUnaryPropertyAxiom<OWLProperty>)axiom;
			OWLProperty property = propertyAxiom.getProperty();
			
			// Check whether there is at least one domain and one range; if not, remove
			Pair<Set<OWLEntity>,Set<OWLEntity>> domainsAndRanges = getDomainsAndRanges(property, ontology);
			if (domainsAndRanges.getLeft().size() < 1 || domainsAndRanges.getRight().size() < 1) {
				schemaDiagram.removeOwlEntity(property);
			}
		}
	}
	
	/**
	 * This method traverses the ontology to see if it contains sufficient axioms to render an edge
	 * on the schema diagram for a certain property (including domain/range and scoped domain/range).
	 * @param property
	 * @param ontology
	 * @return
	 */
	private Pair<Set<OWLEntity>,Set<OWLEntity>> getDomainsAndRanges(OWLProperty property, OWLOntology ontology) {
		
		// Check how many domains and ranges are in the ontology; only render if both are 1
		Set<OWLEntity> domains = new HashSet<OWLEntity>();
		Set<OWLEntity> ranges = new HashSet<OWLEntity>();
		if (property instanceof OWLObjectProperty) {
			OWLObjectProperty objectProperty = (OWLObjectProperty)property;
			for (OWLObjectPropertyDomainAxiom domainAxiom: ontology.getObjectPropertyDomainAxioms(objectProperty)) {
				if (domainAxiom.getDomain().isNamed()) {
					domains.add(domainAxiom.getDomain().asOWLClass());
				}
			}
			for (OWLObjectPropertyRangeAxiom rangeAxiom: ontology.getObjectPropertyRangeAxioms(objectProperty)) {
				if (rangeAxiom.getRange().isNamed()) {
					ranges.add(rangeAxiom.getRange().asOWLClass());
				}
			}
		}
		else if (property instanceof OWLDataProperty) {
			OWLDataProperty dataProperty = (OWLDataProperty)property;
			for (OWLDataPropertyDomainAxiom domainAxiom: ontology.getDataPropertyDomainAxioms(dataProperty)) {
				if (domainAxiom.getDomain().isNamed()) {
					domains.add(domainAxiom.getDomain().asOWLClass());
				}
			}
			for (OWLDataPropertyRangeAxiom rangeAxiom: ontology.getDataPropertyRangeAxioms(dataProperty)) {
				if (rangeAxiom.getRange().isNamed()) {
					ranges.add(rangeAxiom.getRange().asOWLDatatype());
				}
			}
		}
		
		// Walk through the ontology, looking for scoped domains/ranges
		for( OWLAxiom axiom : ontology.getAxioms(AxiomType.SUBCLASS_OF)) {
			OWLSubClassOfAxiom subClassAxiom = (OWLSubClassOfAxiom)axiom;
			// If the subclass is named and superclass is an expression, step into and figure out if a scoped range exists
			if (subClassAxiom.getSubClass().isNamed() && subClassAxiom.getSuperClass().isAnonymous()) {
				if (property instanceof OWLObjectProperty) {
					if (subClassAxiom.getSuperClass().getClassExpressionType() == ClassExpressionType.OBJECT_ALL_VALUES_FROM) {
						OWLObjectAllValuesFrom restriction = (OWLObjectAllValuesFrom)subClassAxiom.getSuperClass();
						if (restriction.getProperty().isNamed()) {
							if (restriction.getProperty().asOWLObjectProperty().equals((OWLObjectProperty)property) && restriction.getFiller().isNamed()) {
								domains.add(subClassAxiom.getSubClass().asOWLClass());
								ranges.add(restriction.getFiller().asOWLClass());
							}
						}
					}
				}
				else if (property instanceof OWLDataProperty) {
					if (subClassAxiom.getSuperClass().getClassExpressionType() == ClassExpressionType.DATA_ALL_VALUES_FROM) {
						OWLDataAllValuesFrom restriction = (OWLDataAllValuesFrom)subClassAxiom.getSuperClass();
						if (restriction.getProperty().isNamed()) {
							if (restriction.getProperty().asOWLDataProperty().equals((OWLDataProperty)property) && restriction.getFiller().isNamed()) {
								domains.add(subClassAxiom.getSubClass().asOWLClass());
								ranges.add(restriction.getFiller().asOWLDatatype());
							}
						}
					}
				}
			}
			// If the superclass is named and the subclass is anonymous, then this is a GCI; step into and figure out if 
			// a scoped domain exists.
			if (subClassAxiom.getSuperClass().isNamed() && subClassAxiom.getSubClass().isAnonymous()) {
				if (property instanceof OWLObjectProperty) {
					if (subClassAxiom.getSubClass().getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM) {
						OWLObjectSomeValuesFrom restriction = (OWLObjectSomeValuesFrom)subClassAxiom.getSubClass();
						if (restriction.getProperty().isNamed()) {
							if (restriction.getProperty().asOWLObjectProperty().equals((OWLObjectProperty)property) && restriction.getFiller().isNamed()) {
								domains.add(subClassAxiom.getSuperClass().asOWLClass());
								ranges.add(restriction.getFiller().asOWLClass());
							}
						}
					}
				}
				else if (property instanceof OWLDataProperty) {
					if (subClassAxiom.getSubClass().getClassExpressionType() == ClassExpressionType.DATA_SOME_VALUES_FROM) {
						OWLDataSomeValuesFrom restriction = (OWLDataSomeValuesFrom)subClassAxiom.getSubClass();
						if (restriction.getProperty().isNamed()) {
							if (restriction.getProperty().asOWLDataProperty().equals((OWLDataProperty)property) && restriction.getFiller().isDatatype()) {
								domains.add(subClassAxiom.getSuperClass().asOWLClass());
								ranges.add(restriction.getFiller().asOWLDatatype());
							}
						}
					}
				}
			}
		}
		
		// By default we don't render anything
		return Pair.of(domains, ranges);
	}

	private void handleClass(OWLOntology ontology, OWLAxiom axiom)
	{
		// Unpack data from Declaration
		OWLDeclarationAxiom declaration = (OWLDeclarationAxiom) axiom;
		OWLEntity           owlEntity   = declaration.getEntity();
		// Only handle Class or Datatype
		if (owlEntity.isOWLClass() || owlEntity.isOWLDatatype())
		{
			log.info("[CoModIDE:UFOH] Handling class or datatype change.");

			// Retrieve the opla-sd annotations for positions
			Pair<Double, Double> xyCoords = PositioningOperations.getXYCoordsForEntity(owlEntity, ontology);
			// The given coordinates might come from the ontology, or they might have been
			// created by PositioningAnnotations (if none were given
			// in the ontology at the outset). To guard against the latter case, persist
			// them right away.
			PositioningOperations.updateXYCoordinateAnnotations(owlEntity, ontology, xyCoords.getLeft(),
					xyCoords.getRight());
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
					if (owlEntity.isOWLClass()) {
						schemaDiagram.addClass(owlEntity, xyCoords.getLeft(), xyCoords.getRight());
					}
					else {
						schemaDiagram.addDatatype(owlEntity, xyCoords.getLeft(), xyCoords.getRight());
					}
				}
				finally
				{
					graphModel.endUpdate();
				}
			}
		}
	}

	private void handleGeneralAxiom(OWLOntology ontology, OWLAxiom axiom)
	{
		log.info("[CoModIDE:UFOH] Handling property via GCI.");
		// Parse the axiom
		mxCell edge = axiomManager.parseSimpleAxiom((OWLSubClassOfAxiom) axiom);
		mxCell sourceCell = (mxCell) edge.getSource();
		mxCell targetCell = (mxCell) edge.getTarget();
		
		// Unpack
		//String id          = edge.toString();
		//String style       = edge.getStyle();

		// Find pre-existing cells on canvas, if any
		boolean sourceCellFound = false;
		boolean targetCellFound = false;
		Map<String, Object> cells      = ((mxGraphModel) this.schemaDiagram.getModel()).getCells();
		for (String key : cells.keySet())
		{
			mxCell cell = (mxCell) cells.get(key);
			if (cell.getId().equals(sourceCell.getId()))
			{
				edge.setSource(cell);
				sourceCellFound = true;
			}
			if (cell.getId().equals(targetCell.getId()))
			{
				edge.setTarget(cell);
				targetCellFound = true;
			}
			// Optimization to break out once both pre-existing cells have been found
			if (sourceCellFound && targetCellFound)
			{
				break;
			}
		}

		// Update the SchemaDiagram
		graphModel.beginUpdate();
		try
		{
			/*if (!sourceCellFound) {
				schemaDiagram.addCell(sourceCell);
			}
			if (!targetCellFound) {
				schemaDiagram.addCell(targetCell);
			}*/
			schemaDiagram.addCell(edge);
		}
		finally
		{
			graphModel.endUpdate();
		}
	}

	private void handleObjectPropertyDomainOrRange(OWLOntology ontology, HasProperty<OWLObjectPropertyExpression> axiom)
	{
		log.info(String.format("[CoModIDE:UFOH] Handing Object Property Domain or Range Restriction '%s'.",
				axiom.toString()));

		OWLObjectProperty                 objectProperty = axiom.getProperty().asOWLObjectProperty();
		Set<OWLObjectPropertyDomainAxiom> domainAxioms   = ontology.getObjectPropertyDomainAxioms(objectProperty);
		Set<OWLObjectPropertyRangeAxiom>  rangeAxioms    = ontology.getObjectPropertyRangeAxioms(objectProperty);

		if (domainAxioms.size() != 1 || rangeAxioms.size() != 1)
		{
			log.info(String.format(
					"[CoModIDE:UFOH] Property '%s' cannot be processed as it does not have exactly 1 domain and range.",
					objectProperty.toString()));
		}
		else
		{
			// Get the things we will be linking up
			OWLClass domain      = ((OWLObjectPropertyDomainAxiom) domainAxioms.toArray()[0]).getDomain().asOWLClass();
			String   domainLabel = domain.toString();
			OWLClass range       = ((OWLObjectPropertyRangeAxiom) rangeAxioms.toArray()[0]).getRange().asOWLClass();
			String   rangeLabel  = range.toString();

			// Iterate through all cells on the canvas to find the nodes whose IDs matches
			// the domain/range we want to link from/to
			// Note the null check on source and target cells below; we cannot put the
			// action code below into this loop, or we will
			// get a ConcurrentModificationException (i.e., in-place editing while
			// iterating) on the cells map.
			mxCell              sourceCell = null;
			mxCell              targetCell = null;
			Map<String, Object> cells      = ((mxGraphModel) this.schemaDiagram.getModel()).getCells();
			for (String key : cells.keySet())
			{
				mxCell cell = (mxCell) cells.get(key);
				if (cell.getId().equals(domainLabel))
				{
					sourceCell = cell;
				}
				if (cell.getId().equals(rangeLabel))
				{
					targetCell = cell;
				}
				if (sourceCell != null && targetCell != null)
				{
					// We have found both ends of the edge
					break;
				}
			}

			// If we got a hit on the above search through the cells, then we have source
			// and target cells to draw the edge between
			if (sourceCell != null && targetCell != null)
			{
				// Only proceed if the source and target cells are class cells
				if (sourceCell instanceof ClassCell && targetCell instanceof ClassCell)
				{
					ClassCell sourceClassCell = (ClassCell)sourceCell;
					ClassCell targetClassCell = (ClassCell)targetCell;
					
					// Only proceed if the source and target cells have identity assigned (i.e., not default name)
					if (sourceClassCell.isNamed() && targetClassCell.isNamed()) {

						// TODO: Check if locking would be needed here like for classes?
						// Update the SchemaDiagram
						graphModel.beginUpdate();
						try
						{
							// If null is passed as parent, a convenience function in the chain
							// will call getDefaultParent()
							schemaDiagram.addPropertyEdge(objectProperty, sourceClassCell, targetClassCell);
						}
						finally
						{
							graphModel.endUpdate();
						}
					}
				}
			}

		}
	}

	private void handleDataPropertyDomainOrRange(OWLOntology ontology, HasProperty<OWLDataPropertyExpression> axiom)
	{
		log.info(String.format("[CoModIDE:UFOH] Handing Data Property Domain or Range Restriction '%s'.",
				axiom.toString()));

		OWLDataProperty                 dataProperty = axiom.getProperty().asOWLDataProperty();
		Set<OWLDataPropertyDomainAxiom> domainAxioms = ontology.getDataPropertyDomainAxioms(dataProperty);
		Set<OWLDataPropertyRangeAxiom>  rangeAxioms  = ontology.getDataPropertyRangeAxioms(dataProperty);

		if (domainAxioms.size() != 1 || rangeAxioms.size() != 1)
		{
			log.info(String.format(
					"[CoModIDE:UFOH] Property '%s' cannot be processed as it does not have exactly 1 domain and range.",
					dataProperty.toString()));
		}
		else
		{
			// Get the things we will be linking up
			OWLClass    domain      = ((OWLDataPropertyDomainAxiom) domainAxioms.toArray()[0]).getDomain().asOWLClass();
			String      domainLabel = domain.toString();
			OWLDatatype range       = ((OWLDataPropertyRangeAxiom) rangeAxioms.toArray()[0]).getRange().asOWLDatatype();

			// Iterate through all cells on the canvas to find the node whose ID matches the
			// domain we want to link from
			// Note the null check on sourceCell below; we cannot put the action code below
			// into this loop, or we will
			// get a ConcurrentModificationException (i.e., in-place editing while
			// iterating) on the cells map.
			mxCell              sourceCell = null;
			Map<String, Object> cells      = ((mxGraphModel) this.schemaDiagram.getModel()).getCells();
			for (String key : cells.keySet())
			{
				mxCell cell = (mxCell) cells.get(key);
				if (cell.getId().equals(domainLabel))
				{
					sourceCell = cell;
					break;
				}
			}

			// If we got a hit on the above search through the cells, then we have a source
			// cell to draw the edge from
			if (sourceCell != null)
			{
				// At this point we have the origin cell, i.e., the domain of the property
				// Now we need to find the coordinates for the target cell, i.e., the range of
				// the property
				// Note that we are fetching the positioning axioms from the data property,
				// which has identity (as the target datatype might not have identity)
				Pair<Double, Double> xyCoords = PositioningOperations.getXYCoordsForEntity(dataProperty, ontology);

				// The given coordinates might come from the ontology, or they might have been
				// created by PositioningAnnotations (if none were given
				// in the ontology at the outset). To guard against the latter case, assign them
				// back to the model right away.
				PositioningOperations.updateXYCoordinateAnnotations(dataProperty, ontology, xyCoords.getLeft(),
						xyCoords.getRight());

				// Now, create a new node and corresponding cell for this datatype
				DatatypeCell targetCell = new DatatypeCell(range, xyCoords.getLeft(), xyCoords.getRight());
				//SDNode rangeNode  = new SDNode(range, true, xyCoords);
				//Object targetCell = vertexMaker.makeNode(rangeNode);

				// Only proceed if the sourceCell has a backing SDNode
				if (sourceCell instanceof ClassCell)
				{
					ClassCell sourceClassCell = (ClassCell)sourceCell; 
					if (sourceClassCell.isNamed()) {
	
						// TODO: Check if locking would be needed here like for classes?
						// Update the SchemaDiagram
						graphModel.beginUpdate();
						try
						{
							schemaDiagram.addCell(targetCell);
							// If null is passed as parent, a convenience function in the chain
							// will call getDefaultParent()
							schemaDiagram.addPropertyEdge(dataProperty, sourceClassCell, targetCell);
							//schemaDiagram.insertEdge(null, propertyName, sdEdge, sourceCell, targetCell,
							//		SDConstants.standardEdgeStyle);
						}
						finally
						{
							graphModel.endUpdate();
						}
					}
				}
			}
		}
	}
}
