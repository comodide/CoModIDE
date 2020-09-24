package com.comodide.editor.changehandlers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.model.OWLRestriction;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLUnaryPropertyAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.editor.SchemaDiagram;
import com.comodide.editor.model.ClassCell;
import com.comodide.editor.model.ComodideCell;
import com.comodide.editor.model.PropertyEdgeCell;
import com.comodide.rendering.PositioningOperations;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxICell;

public class UpdateFromOntologyHandler
{
	/** Logging */
	private static final Logger log = LoggerFactory.getLogger(UpdateFromOntologyHandler.class);

	/** Used for updating the graph when handling changes */
	private SchemaDiagram schemaDiagram;
	private mxGraphModel  graphModel;

	public UpdateFromOntologyHandler(SchemaDiagram schemaDiagram, OWLModelManager modelManager)
	{
		this.schemaDiagram = schemaDiagram;
		this.graphModel = (mxGraphModel) schemaDiagram.getModel();
	}

	public void handle(OWLOntologyChange change)
	{
		// Unpack the OntologyChange
		OWLOntology ontology = change.getOntology();
		OWLAxiom    axiom    = change.getAxiom();

		// If we're open for business
		if (!this.schemaDiagram.isLock())
		{
			if (change.isAddAxiom())
			{
				handleAddAxiom(axiom, ontology);
			}
			else if (change.isRemoveAxiom())
			{
				handleRemoveAxiom(axiom, ontology);
			}
			else
			{
				log.info("[CoModIDE:UFOH] Unsupported change to the ontology.");
			}
		}
	}

	/**
	 * This is public to enable 'rerendering' of an ontology. This by passes the
	 * handle method.
	 */
	public void handleAddAxiom(OWLAxiom axiom, OWLOntology ontology)
	{
		// If we're open for business
		if (!this.schemaDiagram.isLock())
		{
			// Handle class, datatype, or property declarations
			if (axiom.isOfType(AxiomType.DECLARATION))
			{
				handleAddDeclaration(ontology, axiom);
			}
			// Handle subclasses (simple subclasses and restriction-based scoped
			// domains/ranges)
			else if (axiom.isOfType(AxiomType.SUBCLASS_OF))
			{
				handleAddSubClassOfAxiom((OWLSubClassOfAxiom) axiom, ontology);
			}
			else if (axiom.isOfType(AxiomType.ANNOTATION_ASSERTION))
			{
				handleAddAnnotationAssertionAxiom((OWLAnnotationAssertionAxiom) axiom, ontology);
			}
			else
			{
				log.info("[CoModIDE:UFOH] Unsupported AddAxiom: " + axiom.getAxiomWithoutAnnotations().toString());
			}
		}
	}

	private void handleRemoveAxiom(OWLAxiom axiom, OWLOntology ontology)
	{
		if (axiom.isOfType(AxiomType.DECLARATION))
		{
			OWLDeclarationAxiom declarationAxiom = (OWLDeclarationAxiom) axiom;
			OWLEntity           owlEntity        = declarationAxiom.getEntity();
			schemaDiagram.removeOwlEntity(owlEntity);
		}
		else if (axiom.isOfType(AxiomType.SUBCLASS_OF))
		{
			handleRemoveSubClassOfAxiom((OWLSubClassOfAxiom) axiom, ontology);
		}
		else if (axiom.isOfType(AxiomType.OBJECT_PROPERTY_DOMAIN, AxiomType.OBJECT_PROPERTY_RANGE,
				AxiomType.DATA_PROPERTY_DOMAIN, AxiomType.DATA_PROPERTY_RANGE))
		{
			// Unpack the property concerned
			@SuppressWarnings("unchecked")
			OWLUnaryPropertyAxiom<OWLProperty> propertyAxiom = (OWLUnaryPropertyAxiom<OWLProperty>) axiom;
			OWLProperty                        property      = propertyAxiom.getProperty();

			// Remove any edges that are no longer supported
			reRenderAllPropertyEdges(property, ontology);
		}
	}

	/**
	 * This method traverses the ontology looking for the classes that should be
	 * connected by the property as an edge on the schema diagram.
	 * 
	 * @param property
	 * @param ontology
	 * @return List of class pairs to be connected by an edge (left is source, right
	 *         is target).
	 */
	private Set<Pair<OWLEntity, OWLEntity>> getEdgeSourcesAndTargets(OWLProperty property, OWLOntology ontology)
	{

		Set<Pair<OWLEntity, OWLEntity>> retVal = new HashSet<Pair<OWLEntity, OWLEntity>>();

		// Check rdfs:domain and rdfs:range. Only use one of each; we do not support
		// multiple such declarations at this time
		OWLEntity rdfsDomain = null;
		OWLEntity rdfsRange  = null;
		if (property instanceof OWLObjectProperty)
		{
			OWLObjectProperty objectProperty = (OWLObjectProperty) property;
			for (OWLObjectPropertyDomainAxiom domainAxiom : ontology.getObjectPropertyDomainAxioms(objectProperty))
			{
				if (domainAxiom.getDomain().isNamed())
				{
					rdfsDomain = domainAxiom.getDomain().asOWLClass();
				}
			}
			for (OWLObjectPropertyRangeAxiom rangeAxiom : ontology.getObjectPropertyRangeAxioms(objectProperty))
			{
				if (rangeAxiom.getRange().isNamed())
				{
					rdfsRange = rangeAxiom.getRange().asOWLClass();
				}
			}
		}
		else if (property instanceof OWLDataProperty)
		{
			OWLDataProperty dataProperty = (OWLDataProperty) property;
			for (OWLDataPropertyDomainAxiom domainAxiom : ontology.getDataPropertyDomainAxioms(dataProperty))
			{
				if (domainAxiom.getDomain().isNamed())
				{
					rdfsDomain = domainAxiom.getDomain().asOWLClass();
				}
			}
			for (OWLDataPropertyRangeAxiom rangeAxiom : ontology.getDataPropertyRangeAxioms(dataProperty))
			{
				if (rangeAxiom.getRange().isNamed())
				{
					rdfsRange = rangeAxiom.getRange().asOWLDatatype();
				}
			}
		}
		if (rdfsDomain != null && rdfsRange != null)
		{
			retVal.add(Pair.of(rdfsDomain, rdfsRange));
		}

		// Walk through the ontology, looking for scoped domains/ranges
		for (OWLAxiom axiom : ontology.getAxioms(AxiomType.SUBCLASS_OF))
		{
			OWLSubClassOfAxiom subClassAxiom = (OWLSubClassOfAxiom) axiom;
			// If the subclass is named and superclass is an expression, step into and
			// figure out if a scoped range exists
			if (subClassAxiom.getSubClass().isNamed() && subClassAxiom.getSuperClass().isAnonymous())
			{
				if (property instanceof OWLObjectProperty)
				{
					if (subClassAxiom.getSuperClass()
							.getClassExpressionType() == ClassExpressionType.OBJECT_ALL_VALUES_FROM)
					{
						OWLObjectAllValuesFrom restriction = (OWLObjectAllValuesFrom) subClassAxiom.getSuperClass();
						if (restriction.getProperty().isNamed())
						{
							if (restriction.getProperty().asOWLObjectProperty().equals((OWLObjectProperty) property)
									&& restriction.getFiller().isNamed())
							{
								retVal.add(Pair.of(subClassAxiom.getSubClass().asOWLClass(),
										restriction.getFiller().asOWLClass()));
							}
						}
					}
				}
				else if (property instanceof OWLDataProperty)
				{
					if (subClassAxiom.getSuperClass()
							.getClassExpressionType() == ClassExpressionType.DATA_ALL_VALUES_FROM)
					{
						OWLDataAllValuesFrom restriction = (OWLDataAllValuesFrom) subClassAxiom.getSuperClass();
						if (restriction.getProperty().isNamed())
						{
							if (restriction.getProperty().asOWLDataProperty().equals((OWLDataProperty) property)
									&& restriction.getFiller().isNamed())
							{
								retVal.add(Pair.of(subClassAxiom.getSubClass().asOWLClass(),
										restriction.getFiller().asOWLDatatype()));
							}
						}
					}
				}
			}
			// If the superclass is named and the subclass is anonymous, then this is a GCI;
			// step into and figure out if
			// a scoped domain exists.
			if (subClassAxiom.getSuperClass().isNamed() && subClassAxiom.getSubClass().isAnonymous())
			{
				if (property instanceof OWLObjectProperty)
				{
					if (subClassAxiom.getSubClass()
							.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM)
					{
						OWLObjectSomeValuesFrom restriction = (OWLObjectSomeValuesFrom) subClassAxiom.getSubClass();
						if (restriction.getProperty().isNamed())
						{
							if (restriction.getProperty().asOWLObjectProperty().equals((OWLObjectProperty) property)
									&& restriction.getFiller().isNamed())
							{
								retVal.add(Pair.of(subClassAxiom.getSuperClass().asOWLClass(),
										restriction.getFiller().asOWLClass()));
							}
						}
					}
				}
				else if (property instanceof OWLDataProperty)
				{
					if (subClassAxiom.getSubClass()
							.getClassExpressionType() == ClassExpressionType.DATA_SOME_VALUES_FROM)
					{
						OWLDataSomeValuesFrom restriction = (OWLDataSomeValuesFrom) subClassAxiom.getSubClass();
						if (restriction.getProperty().isNamed())
						{
							if (restriction.getProperty().asOWLDataProperty().equals((OWLDataProperty) property)
									&& restriction.getFiller().isDatatype())
							{
								retVal.add(Pair.of(subClassAxiom.getSuperClass().asOWLClass(),
										restriction.getFiller().asOWLDatatype()));
							}
						}
					}
				}
			}
		}
		return retVal;
	}

	private void handleAddDeclaration(OWLOntology ontology, OWLAxiom axiom)
	{
		// Unpack data from Declaration
		OWLDeclarationAxiom declaration = (OWLDeclarationAxiom) axiom;
		OWLEntity           owlEntity   = declaration.getEntity();

		// We do not render anything from the OPLa namespace
		if (owlEntity.getIRI().toString().contains("http://ontologydesignpatterns.org/opla#"))
		{
			return;
		}

		// Handle Class or Datatype
		if (owlEntity.isOWLClass() || owlEntity.isOWLDatatype())
		{
			// Retrieve the opla-sd annotations for positions
			Pair<Double, Double> xyCoords = PositioningOperations.getXYCoordsForEntity(owlEntity, ontology);
			// The given coordinates might come from the ontology, or they might have been
			// created by PositioningAnnotations (if none were given
			// in the ontology at the outset). To guard against the latter case, persist
			// them right away.
			PositioningOperations.updateXYCoordinateAnnotations(owlEntity, ontology, xyCoords.getLeft(),
					xyCoords.getRight());

			graphModel.beginUpdate();
			try
			{
				if (owlEntity.isOWLClass())
				{
					schemaDiagram.addClass(owlEntity, xyCoords.getLeft(), xyCoords.getRight());
				}
				else
				{
					schemaDiagram.addDatatype(owlEntity, xyCoords.getLeft(), xyCoords.getRight());
				}
			}
			finally
			{
				graphModel.endUpdate();
			}
		}
		// Handle properties
		else if (owlEntity.isOWLObjectProperty() || owlEntity.isOWLDataProperty())
		{
			OWLProperty property = (OWLProperty) owlEntity;

			// Get the edges that should be rendered for this property
			Set<Pair<OWLEntity, OWLEntity>> edgeSourcesAndTargets = getEdgeSourcesAndTargets(property, ontology);

			graphModel.beginUpdate();
			try
			{
				for (Pair<OWLEntity, OWLEntity> edgeToRender : edgeSourcesAndTargets)
				{
					OWLEntity            source         = edgeToRender.getLeft();
					OWLEntity            target         = edgeToRender.getRight();
					Pair<Double, Double> sourcePosition = PositioningOperations.getXYCoordsForEntity(source, ontology);

					// Get the source cell. Note that it may exist on canvas already, in which case
					// the existing cell is returned.
					ClassCell sourceCell = schemaDiagram.addClass(source, sourcePosition.getLeft(),
							sourcePosition.getRight());

					// The target cell differs depending on type -- datatype cells
					// have positions given on the data property, and can be duplicated
					mxCell targetCell;
					if (target instanceof OWLDatatype)
					{
						Pair<Double, Double> targetPosition = PositioningOperations.getXYCoordsForEntity(property,
								ontology);
						targetCell = schemaDiagram.addDatatype(target, targetPosition.getLeft(),
								targetPosition.getRight());
					}
					else
					{
						Pair<Double, Double> targetPosition = PositioningOperations.getXYCoordsForEntity(target,
								ontology);
						targetCell = schemaDiagram.addClass(target, targetPosition.getLeft(),
								targetPosition.getRight());
					}
					schemaDiagram.addPropertyEdge(property, sourceCell, targetCell);
				}
			}
			finally
			{
				graphModel.endUpdate();
			}
		}
	}

	private void handleRemoveSubClassOfAxiom(OWLSubClassOfAxiom axiom, OWLOntology ontology)
	{
		OWLClassExpression superClassExpression = axiom.getSuperClass();
		OWLClassExpression subClassExpression   = axiom.getSubClass();

		// This is a simple subclass edge between two nodes, find it and kill it
		if (superClassExpression.isNamed() && subClassExpression.isNamed())
		{
			ClassCell superClassCell = (ClassCell) schemaDiagram.getCell(superClassExpression.asOWLClass());
			ClassCell subClassCell   = (ClassCell) schemaDiagram.getCell(subClassExpression.asOWLClass());
			schemaDiagram.removeSubClassEdge(superClassCell, subClassCell);
		}
		// This is potentially a scoped domain/range. Recompute valid edges and
		// remove any non-valid occurrences of this edge in the diagram
		// Note that we only handle subclasses where one of two involves expressions are
		// restrictions, not nested
		// expressions where both are.
		else if (superClassExpression instanceof OWLRestriction || subClassExpression instanceof OWLRestriction)
		{

			OWLRestriction restriction;
			if (superClassExpression instanceof OWLRestriction)
			{
				restriction = (OWLRestriction) superClassExpression;
			}
			else
			{
				restriction = (OWLRestriction) subClassExpression;
			}

			OWLPropertyExpression pe = restriction.getProperty();
			if (pe.isNamed())
			{
				OWLProperty property = (OWLProperty) pe;
				reRenderAllPropertyEdges(property, ontology);
			}
		}
	}

	private void reRenderAllPropertyEdges(OWLProperty property, OWLOntology ontology)
	{
		Set<Pair<OWLEntity, OWLEntity>> stillValidEdges = getEdgeSourcesAndTargets(property, ontology);

		// Iterate over all existing cells on the diagram. For each cell that is an
		// edge, construct
		// a pair of source and target OWL entities representing that edge.
		// If that pair is not in the set of still valid edges computed above, remove
		// it.
		// For each still valid edge that is found on the canvas, remove it from the set
		// above
		// such that at the end of this loop, only un-rendered edges remain in the set.
		List<mxCell> currentEdges = schemaDiagram.findCellsByEntity(property);
		for (mxCell cell : currentEdges)
		{
			if (cell instanceof PropertyEdgeCell)
			{
				PropertyEdgeCell           currentEdgeCell   = (PropertyEdgeCell) cell;
				OWLEntity                  currentEdgeSource = ((ComodideCell) currentEdgeCell.getSource()).getEntity();
				OWLEntity                  currentEdgeTarget = ((ComodideCell) currentEdgeCell.getTarget()).getEntity();
				Pair<OWLEntity, OWLEntity> currentEdgeAsPair = Pair.of(currentEdgeSource, currentEdgeTarget);
				if (!stillValidEdges.contains(currentEdgeAsPair))
				{
					schemaDiagram.removeCells(new Object[] { cell });
				}
				else
				{
					stillValidEdges.remove(currentEdgeAsPair);
				}
			}
		}
		// For each remaining un-rendered edge, render it.
		for (Pair<OWLEntity, OWLEntity> nonRenderedEdge : stillValidEdges)
		{
			OWLEntity            source         = nonRenderedEdge.getLeft();
			OWLEntity            target         = nonRenderedEdge.getRight();
			Pair<Double, Double> sourcePosition = PositioningOperations.getXYCoordsForEntity(source, ontology);

			// Get the source cell. Note that it may exist on canvas already, in which case
			// the existing cell is returned.
			ClassCell sourceCell = schemaDiagram.addClass(source, sourcePosition.getLeft(), sourcePosition.getRight());

			// The target cell differs depending on type -- datatype cells
			// have positions given on the data property, and can be duplicated
			mxCell targetCell;
			if (target instanceof OWLDatatype)
			{
				Pair<Double, Double> targetPosition = PositioningOperations.getXYCoordsForEntity(property, ontology);
				targetCell = schemaDiagram.addDatatype(target, targetPosition.getLeft(), targetPosition.getRight());
			}
			else
			{
				Pair<Double, Double> targetPosition = PositioningOperations.getXYCoordsForEntity(target, ontology);
				targetCell = schemaDiagram.addClass(target, targetPosition.getLeft(), targetPosition.getRight());
			}
			schemaDiagram.addPropertyEdge(property, sourceCell, targetCell);
		}
	}

	private void handleAddAnnotationAssertionAxiom(OWLAnnotationAssertionAxiom axiom, OWLOntology ontology)
	{
		// Unpack the handled assertion; if it is a a nested
		// entity positioning assertion with a double value proceed
		OWLAnnotationSubject  subject  = axiom.getSubject();
		OWLAnnotationProperty property = axiom.getProperty();
		OWLAnnotationValue    value    = axiom.getValue();
		if ((property.equals(PositioningOperations.entityPositionX)
				|| property.equals(PositioningOperations.entityPositionY)) && subject instanceof OWLAnonymousIndividual
				&& value.isLiteral() && value.asLiteral().get().isDouble())
		{
			// Extract the parent annotation assertion
			OWLAnonymousIndividual subjectIndividual = (OWLAnonymousIndividual) subject;
			for (OWLAnnotationAssertionAxiom candidateParentAnnotation : ontology
					.getAxioms(AxiomType.ANNOTATION_ASSERTION))
			{
				if (candidateParentAnnotation.getValue().equals(subjectIndividual)
						&& candidateParentAnnotation.getSubject() instanceof IRI)
				{
					// If the parent assertion is on an IRI entity, that is the actual entity
					// that our positiong assertion concerns. Find and update the corresponding
					// cell on the canvas.
					IRI    subjectIRI = (IRI) candidateParentAnnotation.getSubject();
					double position   = value.asLiteral().get().parseDouble();
					for (mxCell cell : schemaDiagram.findCellsByIri(subjectIRI))
					{
						if (cell instanceof ComodideCell)
						{
							mxICell cellToMove;
							if (cell instanceof PropertyEdgeCell)
							{
								// If this occurs then we have a datatype edge; in that case we need to move the
								// datatype that this cells points at
								cellToMove = cell.getTarget();
							}
							else
							{
								cellToMove = cell;
							}
							graphModel.beginUpdate();
							try
							{
								mxGeometry geo = cellToMove.getGeometry();
								if (geo != null)
								{
									mxGeometry newGeo = (mxGeometry) geo.clone();
									if (property.equals(PositioningOperations.entityPositionX))
									{
										newGeo.setX(position);
									}
									else
									{
										newGeo.setY(position);
									}
									graphModel.setGeometry(cellToMove, newGeo);
								}
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

	private void handleAddSubClassOfAxiom(OWLSubClassOfAxiom axiom, OWLOntology ontology)
	{
		OWLClassExpression superClassExpression = axiom.getSuperClass();
		OWLClassExpression subClassExpression   = axiom.getSubClass();

		if (superClassExpression.isNamed() && subClassExpression.isNamed())
		{
			graphModel.beginUpdate();
			try
			{
				OWLClass             superClass         = superClassExpression.asOWLClass();
				OWLClass             subClass           = subClassExpression.asOWLClass();
				Pair<Double, Double> superClassPosition = PositioningOperations.getXYCoordsForEntity(superClass,
						ontology);
				ClassCell            superClassCell     = schemaDiagram.addClass(superClass,
						superClassPosition.getLeft(), superClassPosition.getRight());
				Pair<Double, Double> subClassPosition   = PositioningOperations.getXYCoordsForEntity(subClass,
						ontology);
				ClassCell            subClassCell       = schemaDiagram.addClass(subClass, subClassPosition.getLeft(),
						subClassPosition.getRight());
				schemaDiagram.addSubClassEdge(superClassCell, subClassCell);
			}
			finally
			{
				graphModel.endUpdate();
			}
		}
		else if (superClassExpression instanceof OWLRestriction || subClassExpression instanceof OWLRestriction)
		{
			OWLRestriction restriction;
			if (superClassExpression instanceof OWLRestriction)
			{
				restriction = (OWLRestriction) superClassExpression;
			}
			else
			{
				restriction = (OWLRestriction) subClassExpression;
			}

			OWLPropertyExpression pe = restriction.getProperty();
			if (pe.isNamed())
			{
				OWLProperty property = (OWLProperty) pe;
				reRenderAllPropertyEdges(property, ontology);
			}
		}
	}
}
