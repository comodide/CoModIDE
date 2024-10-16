package com.comodide.patterns;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * Singleton class for accessing a pre-indexed OPLa-compliant ontology pattern library.
 * @author Karl Hammar <karl@karlhammar.com>
 *
 */
public class PatternLibrary {

	// Constants
	public static final String DEFAULT_LIBRARY_PATH = "modls/modl/ModlIndex.owl";

	// Infrastructure
    private static PatternLibrary instance;
    private static final Logger log = LoggerFactory.getLogger(PatternLibrary.class);

    // Configuration fields
	private final IRI PATTERN_CLASS_IRI = IRI.create("http://ontologydesignpatterns.org/opla#Pattern");
	private final IRI CATEGORIZATION_PROPERTY_IRI = IRI.create("http://ontologydesignpatterns.org/opla#categorization");
	private final IRI SCHEMADIAGRAM_PROPERTY_IRI = IRI.create("http://ontologydesignpatterns.org/opla#renderedSchemaDiagram");
	private final IRI HTMLDOC_PROPERTY_IRI = IRI.create("http://ontologydesignpatterns.org/opla#htmlDocumentation");
	private final IRI OWLREP_PROPERTY_IRI = IRI.create("http://ontologydesignpatterns.org/opla#owlRepresentation");
    public final PatternCategory ANY_CATEGORY = new PatternCategory("Any", IRI.create("https://w3id.org/comodide/ModlIndex#AnyCategory"));

    // Instance fields
	private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	private OWLDataFactory factory = manager.getOWLDataFactory();
	private OWLOntology index;
	private Map<PatternCategory,List<Pattern>> patternCategories = new HashMap<>();

    // Singleton access method
    public static synchronized PatternLibrary getInstance() {
        if(instance == null){
			setInstanceByPath(DEFAULT_LIBRARY_PATH);
        }
        return instance;
    }

	public static synchronized void setInstanceByPath(@Nonnull final String libraryPath) {
		instance = new PatternLibrary(libraryPath);
	}

    // Parse index on instance creation
	private PatternLibrary(@Nonnull final String filePath) {
		parseIndex(filePath);
	}

	// Based on pattern object, instantiate pattern from disk into OWLOntology
	public OWLOntology getOwlRepresentation(Pattern pattern) throws OWLOntologyCreationException {
		// TODO: add support for an external pattern library
		OWLOntologyManager exportManager = OWLManager.createOWLOntologyManager();
		ClassLoader classloader = this.getClass().getClassLoader();
		InputStream is = classloader.getResourceAsStream(pattern.getOwlRepresentationPath());
		if (is == null) {
			throw new OWLOntologyCreationException(String.format("The OWL representation path '%s' could not be read as an input stream.", pattern.getOwlRepresentationPath()));
		}
		return exportManager.loadOntologyFromOntologyDocument(is);
	}

	/**
	 * Parses a pattern index and updates the data structures that are needed to feed other classes.
	 */
    public void parseIndex(@Nonnull final String filePath) {
		try {
			// Set up index file (TODO: add support for an external pattern library)
			ClassLoader classloader = this.getClass().getClassLoader();
			InputStream is = classloader.getResourceAsStream(filePath);
			index = manager.loadOntologyFromOntologyDocument(is);

			// Regardless of index structure there is always the Any category
			patternCategories.put(ANY_CATEGORY, new ArrayList<>());

			// Find all the pattern instances in the index
			OWLClass patternClass = factory.getOWLClass(PATTERN_CLASS_IRI);
			OWLObjectProperty categorizationProperty = factory.getOWLObjectProperty(CATEGORIZATION_PROPERTY_IRI);
			OWLDataProperty schemaDiagram = factory.getOWLDataProperty(SCHEMADIAGRAM_PROPERTY_IRI);
			OWLDataProperty htmlDocumentation = factory.getOWLDataProperty(HTMLDOC_PROPERTY_IRI);
			OWLDataProperty owlRepresentationProperty = factory.getOWLDataProperty(OWLREP_PROPERTY_IRI);

			EntitySearcher.getIndividuals(patternClass, index).stream()
					.filter(OWLIndividual::isNamed)
					.map(individual -> (OWLNamedIndividual) individual)
					.forEach(namedPattern -> {
						// We've found a pattern; turn it into a Java object.
						List<String> patternLabels = getLabels(namedPattern, index);
						List<String> owlRepresentations = getDataPropertyValues(namedPattern, owlRepresentationProperty, index);

						// Mandatory fields if we are to proceed at all: rdfs:label, opla:owlRepresentation,
						// and an IRI (which is already checked above in if pattern.isNamed())
						if (patternLabels.isEmpty() || owlRepresentations.isEmpty())
							return;

						Pattern newPattern = new Pattern(
								patternLabels.get(0),
								namedPattern.getIRI(),
								// appends the parent directory path to each pattern representation.
								// without this, the libraries can't be found or loaded.
								"modls/" + owlRepresentations.get(0)
						);

						// The below fields are nice to have but not mandatory to be indexed.
						getDataPropertyValues(namedPattern, schemaDiagram, index).stream()
								.findFirst().ifPresent(newPattern::setSchemaDiagramPath);
						getDataPropertyValues(namedPattern, htmlDocumentation, index).stream()
								.findFirst().ifPresent(newPattern::setHtmlDocumentation);

						// Find all the categories for this pattern. All patterns are assigned to at least the Any category by default
						List<PatternCategory> categoriesForPattern =
								EntitySearcher.getObjectPropertyValues(namedPattern, categorizationProperty, index).stream()
								.filter(OWLIndividual::isNamed)
								.map(individual -> (OWLNamedIndividual) individual)
								.map(namedCategory -> {
									List<String> categoryLabels = getLabels(namedCategory, index);
									String categoryLabel = !categoryLabels.isEmpty() ?
											categoryLabels.get(0) :
											namedCategory.getIRI().toString();
									return new PatternCategory(categoryLabel, namedCategory.getIRI());
								})
								.collect(Collectors.toList());
						categoriesForPattern.add(0, ANY_CATEGORY);

						// Go through the list of categories for pattern and generate the map structure Category -> List<Pattern> that we need
						categoriesForPattern.forEach(category -> {
							patternCategories.putIfAbsent(category, new ArrayList<>());
							patternCategories.computeIfPresent(category, (patternCategory, patterns) -> {
                                patterns.add(newPattern);
								return patterns;
							});
						});
					});
		}
		catch (Exception e) {
			log.error("Unable to reindex pattern library; setting up empty index.", e);
			e.printStackTrace();
		}
    }

	/**
	 * Convenience method that returns the string values of all rdfs:label annotations on
	 * a given entity in a given ontology.
	 * @param entity
	 * @param ontology
	 * @return
	 */
	private List<String> getLabels(OWLEntity entity, OWLOntology ontology) {
		List<String> retVal = new ArrayList<>();
		for(OWLAnnotation annotation: EntitySearcher.getAnnotations(entity, ontology, factory.getRDFSLabel())) {
		    OWLAnnotationValue value = annotation.getValue();
		    if(value instanceof OWLLiteral) {
		    	retVal.add(((OWLLiteral) value).getLiteral());
		    }
		}
		return retVal;
	}

	/**
	 * Convenience method that returns the string values of all occurrences of a given data
	 * property on a given individual in a given ontology
	 * @param i - Individual
	 * @param p - Data property
	 * @param ontology - Ontology
	 * @return List of strings, one per each occurrence of the data property, on the individual, in the ontology
	 */
	private List<String> getDataPropertyValues(OWLIndividual i, OWLDataProperty p, OWLOntology ontology) {
		List<String> retVal = new ArrayList<>();
		for(OWLLiteral literal: EntitySearcher.getDataPropertyValues(i, p, ontology)) {
			// literal.getLiteral() sometimes includes modl/ and sometimes doesn't.
			retVal.add(literal.getLiteral());
		}
		return retVal;


	}

	/**
	 * Returns a list of all categories (that have one or more patterns) in this library.
	 * @return
	 */
	public PatternCategory[] getPatternCategories() {
		// Get and sort all categories
		Set<PatternCategory> categorySet = patternCategories.keySet();
		List<PatternCategory> categoryList = new ArrayList<>(categorySet);
		Collections.sort(categoryList);
		// Put the Any category first in the list, for usability purposes
		categoryList.remove(ANY_CATEGORY);
		categoryList.add(0, ANY_CATEGORY);
		// Return as array
		return categoryList.toArray(new PatternCategory[0]);
	}

	/**
	 * Returns all the patterns for a given category.
	 * @param category
	 * @return
	 */
	public List<Pattern> getPatternsForCategory(PatternCategory category) {
		List<Pattern> returnedPatterns = patternCategories.get(category);
		Collections.sort(returnedPatterns);
		return returnedPatterns;
	}

}
