package com.comodide.rendering;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

/**
 * This class provides static methods that map from OPLa-SD annotations to (x,y) coordinate pairs, and vice versa. 
 * @author Karl Hammar <karl@karlhammar.com>
 *
 */
public class PositioningOperations {

	private static final Logger log = LoggerFactory.getLogger(PositioningOperations.class);
	
	private static String OPLA_SD_NAMESPACE = "http://ontologydesignpatterns.org/opla-sd#"; 
	
	private static OWLDataFactory factory = new OWLDataFactoryImpl();
	private static OWLAnnotationProperty entityPosition = factory.getOWLAnnotationProperty(IRI.create(String.format("%sentityPosition", OPLA_SD_NAMESPACE)));
	private static OWLAnnotationProperty entityPositionX = factory.getOWLAnnotationProperty(IRI.create(String.format("%sentityPositionX", OPLA_SD_NAMESPACE)));
	private static OWLAnnotationProperty entityPositionY = factory.getOWLAnnotationProperty(IRI.create(String.format("%sentityPositionY", OPLA_SD_NAMESPACE)));
	
	/**
	 * Get the (x,y) coordinate pair from OPLa-SD annotations on an OWL Entity in a given OWL ontology.
	 * @param entity -- The OWL entity to get annotations on
	 * @param ontology -- The OWL ontology in which the annotations are defined
	 * @return A pair of Doubles representing (x,y) coordinates. If no coordinates are found in the annotations, 
	 * each of the coordinates returned are randomized doubles between 0 and 300 (to scatter the rendered nodes 
	 * a little and avoid 100 % overlap) 
	 */
	public static Pair<Double,Double> getXYCoordsForEntity(OWLEntity entity, OWLOntology ontology) {
		log.debug(String.format("Parsing OPLa-SD annotations to establish coordinates for entity '%s'", entity.toString()));
		
		// Default values if no annotations are found; randomized to scatter the nodes a little so they don't all overlap exactly
		Double positionX = getRandomDoubleBetweenRange(0.0, 300.0);
		Double positionY = getRandomDoubleBetweenRange(0.0, 300.0);
		log.debug(String.format("Default (auto-generated) coordinates for entity '%s': (%s,%s)", entity.toString(), positionX.toString(), positionY.toString()));
		
		// We'll iterate through all the annotations, so in effect we will get one at random if multiple ones exist.
		// This is not efficient but the set of annotations should be small (i.e., one) so it doesn't really matter.
		Collection<OWLAnnotation> positionAnnotations = EntitySearcher.getAnnotations(entity.getIRI(), ontology, entityPosition);
		for (OWLAnnotation positionAnnotation: positionAnnotations) {
			if (positionAnnotation.anonymousIndividualValue().isPresent()) {
				OWLAnonymousIndividual positionIndividual = positionAnnotation.anonymousIndividualValue().get();
				// We have found an entityPosition annotation pointing from our entity to an anonymous "wrapper" individual; 
				// now find the sought (x,y) coordinates as annotations on that wrapper individual
				Collection<OWLAnnotation> positionXAnnotations = EntitySearcher.getAnnotations(positionIndividual, ontology, entityPositionX);
				for (OWLAnnotation positionXAnnotation: positionXAnnotations) {
					if (positionXAnnotation.annotationValue().asLiteral().isPresent() && positionXAnnotation.annotationValue().asLiteral().get().isDouble()) {
						positionX = positionXAnnotation.annotationValue().asLiteral().get().parseDouble();
						// One X coordinate per annotation is enough.
						break;
					}
				}
				Collection<OWLAnnotation> positionYAnnotations = EntitySearcher.getAnnotations(positionIndividual, ontology, entityPositionY);
				for (OWLAnnotation positionYAnnotation: positionYAnnotations) {
					if (positionYAnnotation.annotationValue().asLiteral().isPresent() && positionYAnnotation.annotationValue().asLiteral().get().isDouble()) {
						positionY = positionYAnnotation.annotationValue().asLiteral().get().parseDouble();
						// One Y coordinate per annotation is enough.
						break;
					}
				}
			}
		}
		log.debug(String.format("Coordinates for entity '%s': (%s,%s)", entity.toString(), positionX.toString(), positionY.toString()));
		return Pair.of(positionX, positionY);
	}
	
	/**
	 * Creates OPLa-SD positioning annotations, i.e., (x,y) on an OWL entity in an OWL 
	 * ontology (replacing any previous annotations in the process). 
	 * @param entity - The entity to annotate
	 * @param ontology - The ontology holding the annotations
	 * @param newX - X coordinate of entity
	 * @param newY - Y coordinate of entity
	 */
	public static void updateXYCoordinateAnnotations(OWLEntity entity, OWLOntology ontology, Double newX, Double newY) {
		
		OWLOntologyManager manager = ontology.getOWLOntologyManager();
		
		// 1. Remove any previous positioning annotations.
		Set<OWLAxiom> removedAxioms = new HashSet<OWLAxiom>();
		Collection<OWLAnnotation> positionAnnotations = EntitySearcher.getAnnotations(entity.getIRI(), ontology, entityPosition);
		for (OWLAnnotation positionAnnotation: positionAnnotations) {
			if (positionAnnotation.anonymousIndividualValue().isPresent()) {
				OWLAnonymousIndividual positionIndividual = positionAnnotation.anonymousIndividualValue().get();
				Set<OWLAnnotationAssertionAxiom> positionSubAnnotations = ontology.getAnnotationAssertionAxioms(positionIndividual);
				removedAxioms.addAll(positionSubAnnotations);
				removedAxioms.add(factory.getOWLAnnotationAssertionAxiom(entityPosition, entity.getIRI(), positionIndividual));
			}
		}
		manager.removeAxioms(ontology, removedAxioms);
		
		// 2. Create a new anonymous individual holding the positioning annotations.
		Set<OWLAxiom> newAxioms = new HashSet<OWLAxiom>();
		OWLAnonymousIndividual positionAnnotationIndividual = factory.getOWLAnonymousIndividual();
		OWLLiteral positionX = factory.getOWLLiteral(newX);
		OWLLiteral positionY = factory.getOWLLiteral(newY);
		OWLLiteral comment = factory.getOWLLiteral("This is an entity positioning annotation generated by CoModIDE (https://comodide.com/). Removing this annotation will break rendering the CoModIDE schema diagram view.", "en");
		OWLAnnotationAssertionAxiom positionXAnnotationAxiom = factory.getOWLAnnotationAssertionAxiom(entityPositionX, positionAnnotationIndividual, positionX);
		OWLAnnotationAssertionAxiom positionYAnnotationAxiom = factory.getOWLAnnotationAssertionAxiom(entityPositionY, positionAnnotationIndividual, positionY);
		OWLAnnotationAssertionAxiom positionAnnotationIndividualCommentAxiom = factory.getOWLAnnotationAssertionAxiom(factory.getRDFSComment(), positionAnnotationIndividual, comment);
		newAxioms.add(positionXAnnotationAxiom);
		newAxioms.add(positionYAnnotationAxiom);
		newAxioms.add(positionAnnotationIndividualCommentAxiom);
		
		// 3. Annotate the target entity with this new anonymous individual
		OWLAnnotationAssertionAxiom entityAnnotationAxiom = factory.getOWLAnnotationAssertionAxiom(entityPosition, entity.getIRI(), positionAnnotationIndividual);
		newAxioms.add(entityAnnotationAxiom);
		
		// 4. Add the new axioms
		manager.addAxioms(ontology, newAxioms);
	}
	
	private static double getRandomDoubleBetweenRange(double min, double max){
	    double x = (Math.random()*((max-min)+1))+min;
	    return x;
	}
}
