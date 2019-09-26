package com.comodide.editor.changehandlers;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.ClassExpressionType;
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
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLUnaryPropertyAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.editor.SchemaDiagram;
import com.comodide.editor.model.ClassCell;
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

	public UpdateFromOntologyHandler(SchemaDiagram schemaDiagram, OWLModelManager modelManager)
	{
		this.schemaDiagram = schemaDiagram;
		this.graphModel = (mxGraphModel) schemaDiagram.getModel();
	}
	
	public void handleAddAxiom(OWLAxiom axiom, OWLOntology ontology) {
		log.info("[CoModIDE:UFOH] Adding axiom: " + axiom.getAxiomWithoutAnnotations().toString() );
		// When a class is created it is declared. We extract the OWLEntity
		// From the declaration. It does not render declared properties.
		if (axiom.isOfType(AxiomType.DECLARATION))
		{
			handleAddDeclaration(ontology, axiom);
		}
		// General class axioms are parsed and rendered as an edge
		else if (axiom.isOfType(AxiomType.SUBCLASS_OF))
		{
			handleAddSubClassOfAxiom((OWLSubClassOfAxiom) axiom, ontology);
		}
		/*else if (axiom.isOfType(AxiomType.OBJECT_PROPERTY_DOMAIN, AxiomType.OBJECT_PROPERTY_RANGE, AxiomType.DATA_PROPERTY_DOMAIN, AxiomType.DATA_PROPERTY_RANGE)) {
			// Unpack the property concerned
			@SuppressWarnings("unchecked")
			OWLUnaryPropertyAxiom<OWLProperty> propertyAxiom = (OWLUnaryPropertyAxiom<OWLProperty>)axiom;
			OWLProperty property = propertyAxiom.getProperty();
			
			// Check whether there is exactly one domain and one range; if so, render
			Pair<Set<OWLEntity>,Set<OWLEntity>> domainsAndRanges = getDomainsAndRanges(property, ontology);
			if (domainsAndRanges.getLeft().size() == 1 || domainsAndRanges.getRight().size() == 1) {
				OWLEntity domain = domainsAndRanges.getLeft().iterator().next();
				Pair<Double,Double> domainCellPosition = PositioningOperations.getXYCoordsForEntity(domain, ontology);
				ClassCell domainCell = schemaDiagram.addClass(domain, domainCellPosition.getLeft(), domainCellPosition.getRight());
				
				OWLEntity range = domainsAndRanges.getRight().iterator().next();
				Pair<Double,Double> rangeCellPosition = PositioningOperations.getXYCoordsForEntity(range, ontology);
				ClassCell rangeCell = schemaDiagram.addClass(range, rangeCellPosition.getLeft(), rangeCellPosition.getRight());
				
				
			}
		}*/
 		/*else if (axiom.isOfType(AxiomType.OBJECT_PROPERTY_DOMAIN))
		{
			handleObjectPropertyDomainOrRange(ontology, (OWLObjectPropertyDomainAxiom) axiom);
		}
		else if (axiom.isOfType(AxiomType.OBJECT_PROPERTY_RANGE))
		{
			handleObjectPropertyDomainOrRange(ontology, (OWLObjectPropertyRangeAxiom) axiom);
		}
		else if (axiom.isOfType(AxiomType.DATA_PROPERTY_DOMAIN))
		{
			log.info("This is a DATA_PROPERTY_DOMAIN " +axiom.getAxiomWithoutAnnotations().toString() );
			handleDataPropertyDomainOrRange(ontology, (OWLDataPropertyDomainAxiom) axiom);
		}
		else if (axiom.isOfType(AxiomType.DATA_PROPERTY_RANGE))
		{
			handleDataPropertyDomainOrRange(ontology, (OWLDataPropertyRangeAxiom) axiom);
		}*/
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
			log.warn("[CoModIDE:UFOH] Removing axiom "  + axiom.getAxiomWithoutAnnotations().toString() + " TBD");
			// Unpack the property concerned
			@SuppressWarnings("unchecked")
			OWLUnaryPropertyAxiom<OWLProperty> propertyAxiom = (OWLUnaryPropertyAxiom<OWLProperty>)axiom;
			OWLProperty property = propertyAxiom.getProperty();
			
			// TODO: Implement me; update canvas removing no longer supported nodes
			// Check whether there is at least one domain and one range; if not, remove
			/*Pair<Set<OWLEntity>,Set<OWLEntity>> domainsAndRanges = getDomainsAndRanges(property, ontology);
			if (domainsAndRanges.getLeft().size() < 1 || domainsAndRanges.getRight().size() < 1) {
				schemaDiagram.removeOwlEntity(property);
			}*/
		}
	}
	
	/**
	 * This method traverses the ontology looking for the classes that should be connected by the property as an edge
	 * on the schema diagram. 
	 * @param property
	 * @param ontology
	 * @return List of class pairs to be connected by an edge (left is source, right is target). 
	 */
	private Set<Pair<OWLEntity,OWLEntity>> getEdgeSourcesAndTargets(OWLProperty property, OWLOntology ontology) {
		
		Set<Pair<OWLEntity,OWLEntity>> retVal = new HashSet<Pair<OWLEntity,OWLEntity>>();
		
		// Check rdfs:domain and rdfs:range. Only use one of each; we do not support multiple such declarations at this time
		OWLEntity rdfsDomain = null;
		OWLEntity rdfsRange = null;
		if (property instanceof OWLObjectProperty) {
			OWLObjectProperty objectProperty = (OWLObjectProperty)property;
			for (OWLObjectPropertyDomainAxiom domainAxiom: ontology.getObjectPropertyDomainAxioms(objectProperty)) {
				if (domainAxiom.getDomain().isNamed()) {
					rdfsDomain = domainAxiom.getDomain().asOWLClass();
				}
			}
			for (OWLObjectPropertyRangeAxiom rangeAxiom: ontology.getObjectPropertyRangeAxioms(objectProperty)) {
				if (rangeAxiom.getRange().isNamed()) {
					rdfsRange = rangeAxiom.getRange().asOWLClass();
				}
			}
		}
		else if (property instanceof OWLDataProperty) {
			OWLDataProperty dataProperty = (OWLDataProperty)property;
			for (OWLDataPropertyDomainAxiom domainAxiom: ontology.getDataPropertyDomainAxioms(dataProperty)) {
				if (domainAxiom.getDomain().isNamed()) {
					rdfsDomain= domainAxiom.getDomain().asOWLClass();
				}
			}
			for (OWLDataPropertyRangeAxiom rangeAxiom: ontology.getDataPropertyRangeAxioms(dataProperty)) {
				if (rangeAxiom.getRange().isNamed()) {
					rdfsRange = rangeAxiom.getRange().asOWLDatatype();
				}
			}
		}
		if (rdfsDomain != null && rdfsRange != null) {
			retVal.add(Pair.of(rdfsDomain, rdfsRange));
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
								retVal.add(Pair.of(subClassAxiom.getSubClass().asOWLClass(), restriction.getFiller().asOWLClass()));
							}
						}
					}
				}
				else if (property instanceof OWLDataProperty) {
					if (subClassAxiom.getSuperClass().getClassExpressionType() == ClassExpressionType.DATA_ALL_VALUES_FROM) {
						OWLDataAllValuesFrom restriction = (OWLDataAllValuesFrom)subClassAxiom.getSuperClass();
						if (restriction.getProperty().isNamed()) {
							if (restriction.getProperty().asOWLDataProperty().equals((OWLDataProperty)property) && restriction.getFiller().isNamed()) {
								retVal.add(Pair.of(subClassAxiom.getSubClass().asOWLClass(), restriction.getFiller().asOWLDatatype()));
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
								retVal.add(Pair.of(subClassAxiom.getSuperClass().asOWLClass(), restriction.getFiller().asOWLClass()));
							}
						}
					}
				}
				else if (property instanceof OWLDataProperty) {
					if (subClassAxiom.getSubClass().getClassExpressionType() == ClassExpressionType.DATA_SOME_VALUES_FROM) {
						OWLDataSomeValuesFrom restriction = (OWLDataSomeValuesFrom)subClassAxiom.getSubClass();
						if (restriction.getProperty().isNamed()) {
							if (restriction.getProperty().asOWLDataProperty().equals((OWLDataProperty)property) && restriction.getFiller().isDatatype()) {
								retVal.add(Pair.of(subClassAxiom.getSuperClass().asOWLClass(), restriction.getFiller().asOWLDatatype()));
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
			
			// By adding this "catch" we prevent the loopback "feature" of adding a Class
			// via CoModIDE propagating via this handler to add a duplicate cell
			if (!this.schemaDiagram.isLock())
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
		else if (owlEntity.isOWLObjectProperty() || owlEntity.isOWLDataProperty()) {
			OWLProperty property = (OWLProperty)owlEntity;
			
			Set<Pair<OWLEntity,OWLEntity>> edgeSourcesAndTargets = getEdgeSourcesAndTargets(property, ontology);
			
			if (!this.schemaDiagram.isLock()) {
				graphModel.beginUpdate();
				try
				{
					for (Pair<OWLEntity,OWLEntity> edgeToRender: edgeSourcesAndTargets) {
						OWLEntity source = edgeToRender.getLeft();
						OWLEntity target = edgeToRender.getRight();
						Pair<Double,Double> sourcePosition = PositioningOperations.getXYCoordsForEntity(source, ontology);
						ClassCell sourceCell = schemaDiagram.addClass(source, sourcePosition.getLeft(), sourcePosition.getRight());
						
						mxCell targetCell;
						if (target instanceof OWLDatatype) {
							
							Pair<Double,Double> targetPosition = PositioningOperations.getXYCoordsForEntity(property, ontology);
							log.warn("Adding Datatype cell " + target.toString() + "("+targetPosition.getLeft()+","+targetPosition.getRight()+") for property " + property.toString());
							targetCell = schemaDiagram.addDatatype(target, targetPosition.getLeft(), targetPosition.getRight());
						}
						else {
							Pair<Double,Double> targetPosition = PositioningOperations.getXYCoordsForEntity(target, ontology);
							targetCell = schemaDiagram.addClass(target, targetPosition.getLeft(), targetPosition.getRight());
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
	}

	private void handleAddSubClassOfAxiom(OWLSubClassOfAxiom axiom, OWLOntology ontology)
	{
		OWLClassExpression superClassExpression = axiom.getSuperClass();
		OWLClassExpression subClassExpression = axiom.getSubClass();
		
		if (superClassExpression.isNamed() && subClassExpression.isNamed()) {
			if (!this.schemaDiagram.isLock()) {
				graphModel.beginUpdate();
				try
				{
					OWLClass superClass = superClassExpression.asOWLClass();
					OWLClass subClass = subClassExpression.asOWLClass();
					Pair<Double,Double> superClassPosition = PositioningOperations.getXYCoordsForEntity(superClass, ontology);
					ClassCell superClassCell = schemaDiagram.addClass(superClass, superClassPosition.getLeft(), superClassPosition.getRight());
					Pair<Double,Double> subClassPosition = PositioningOperations.getXYCoordsForEntity(subClass, ontology);
					ClassCell subClassCell = schemaDiagram.addClass(subClass, subClassPosition.getLeft(), subClassPosition.getRight());
					schemaDiagram.addSubClassEdge(superClassCell, subClassCell);
				}
				finally
				{
					graphModel.endUpdate();
				}
			}
		}
	}
}
