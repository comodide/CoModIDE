package com.comodide.rendering;

import java.util.Collection;

import org.apache.commons.lang3.tuple.Pair;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PositioningOperations {

	private static final Logger log = LoggerFactory.getLogger(PositioningOperations.class);
	
	private static String OPLA_SD_NAMESPACE = "http://ontologydesignpatterns.org/opla-sd#"; 
	
	public static Pair<Double,Double> getXYCoordsForEntity(OWLEntity entity, OWLOntology ontology) {
		log.debug(String.format("Parsing OPLa-SD annotations to establish coordinates for entity '%s'", entity.toString()));
		OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();
		OWLAnnotationProperty entityPosition = factory.getOWLAnnotationProperty(IRI.create(String.format("%sentityPosition", OPLA_SD_NAMESPACE)));
		OWLAnnotationProperty entityPositionX = factory.getOWLAnnotationProperty(IRI.create(String.format("%sentityPositionX", OPLA_SD_NAMESPACE)));
		OWLAnnotationProperty entityPositionY = factory.getOWLAnnotationProperty(IRI.create(String.format("%sentityPositionY", OPLA_SD_NAMESPACE)));
		
		// Default values if no annotations are found; randomized to scatter the nodes a little so they don't all overlap exactly
		Double positionX = getRandomDoubleBetweenRange(0.0, 300.0);
		Double positionY = getRandomDoubleBetweenRange(0.0, 300.0);
		log.debug(String.format("Default (auto-generated) coordinates for entity '%s': (%s,%s)", entity.toString(), positionX.toString(), positionY.toString()));
		
		// We'll iterate through all the annotations, so in effect we will get one at random if multiple ones exist.
		// This is not efficient but the set of annotations should be small (i.e., at most one) so it doesn't really matter.
		Collection<OWLAnnotation> positionAnnotations = EntitySearcher.getAnnotations(entity.getIRI(), ontology, entityPosition);
		for (OWLAnnotation positionAnnotation: positionAnnotations) {
			if (positionAnnotation.anonymousIndividualValue().isPresent()) {
				OWLAnonymousIndividual positionIndividual = positionAnnotation.anonymousIndividualValue().get();
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
	
	
	public static void updateXYCoordinateAnnotations(OWLEntity entity, OWLOntology ontology) {
		// TODO: Implement me
	}
	
	
	private static double getRandomDoubleBetweenRange(double min, double max){
	    double x = (Math.random()*((max-min)+1))+min;
	    return x;
	}
}
