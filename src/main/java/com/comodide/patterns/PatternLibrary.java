package com.comodide.patterns;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

/**
 * Singleton class for accessing a pre-indexed OPLa-compliant ontology pattern library.
 * @author Karl Hammar <karl@karlhammar.com>
 *
 */
public class PatternLibrary {
	
	// Infrastructure
    private static PatternLibrary instance;
    private static final Logger log = LoggerFactory.getLogger(PatternLibrary.class);
    
    // Configuration fields
    private final IRI PATTERN_CLASS_IRI = IRI.create("http://ontologydesignpatterns.org/opla#Pattern"); 
    private final IRI CATEGORIZATION_PROPERTY_IRI = IRI.create("http://ontologydesignpatterns.org/opla#categorization");
    private final IRI SCHEMADIAGRAM_PROPERTY_IRI = IRI.create("http://ontologydesignpatterns.org/opla#renderedSchemaDiagram");
    private final IRI HTMLDOC_PROPERTY_IRI = IRI.create("http://ontologydesignpatterns.org/opla#htmlDocumentation");
    private final IRI OWLREP_PROPERTY_IRI = IRI.create("http://ontologydesignpatterns.org/opla#owlRepresentation");
    public final Category ANY_CATEGORY = new Category("Any", IRI.create("https://w3id.org/comodide/ModlIndex#AnyCategory"));
    
    // Instance fields
    OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	OWLDataFactory factory = manager.getOWLDataFactory();
	OWLOntology index;
	private Map<Category,List<Pattern>> patternCategories = new HashMap<Category,List<Pattern>>();
    
    // Singleton access method
    public static synchronized PatternLibrary getInstance() {
        if(instance == null){
            instance = new PatternLibrary();
        }
        return instance;
    }
	
    // Parse index on instance creation
	private PatternLibrary() {
		parseIndex();
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
    public void parseIndex() {
		try {
			// Set up index file (TODO: add support for an external pattern library)
			ClassLoader classloader = this.getClass().getClassLoader();
			InputStream is = classloader.getResourceAsStream("modl/ModlIndex.owl");
			index = manager.loadOntologyFromOntologyDocument(is);
			
			// Regardless of index structure there is always the Any category
			patternCategories.put(ANY_CATEGORY, new ArrayList<Pattern>());
			
			// Find all the pattern instances in the index
			OWLClass patternClass = factory.getOWLClass(PATTERN_CLASS_IRI);
			OWLObjectProperty categorizationProperty = factory.getOWLObjectProperty(CATEGORIZATION_PROPERTY_IRI);
			OWLDataProperty schemaDiagram = factory.getOWLDataProperty(SCHEMADIAGRAM_PROPERTY_IRI);
			OWLDataProperty htmlDocumentation = factory.getOWLDataProperty(HTMLDOC_PROPERTY_IRI);
			OWLDataProperty owlRepresentationProperty = factory.getOWLDataProperty(OWLREP_PROPERTY_IRI);
			for (OWLIndividual pattern: EntitySearcher.getIndividuals(patternClass, index)) {
				if (pattern.isNamed()) {
					
					// We've found a pattern; turn it into a Java object.
					OWLNamedIndividual namedPattern = (OWLNamedIndividual)pattern;
					List<String> patternLabels = getLabels(namedPattern, index);
					List<String> owlRepresentations = getDataPropertyValues(namedPattern, owlRepresentationProperty, index);
					
					// Mandatory fields if we are to proceed at all: rdfs:label, opla:owlRepresentation, 
					// and an IRI (which is already checked above in if pattern.isNamed())
					if (patternLabels.size() > 0 && owlRepresentations.size() > 0) {
						Pattern newPattern = new Pattern(patternLabels.get(0), namedPattern.getIRI(), owlRepresentations.get(0));
					
						// The below fields are nice to have but not mandatory to be indexed.
						List<String> schemaDiagrams = getDataPropertyValues(namedPattern, schemaDiagram, index);
						if (schemaDiagrams.size() > 0) {
							newPattern.setSchemaDiagramPath(schemaDiagrams.get(0));
						}
						List<String> htmlDocs = getDataPropertyValues(namedPattern, htmlDocumentation, index);
						if (htmlDocs.size() > 0) {
							newPattern.setHtmlDocumentation(htmlDocs.get(0));
						}
						
						// Find all the categories for this pattern. All patterns are assigned to at least the Any category by default
						List<Category> categoriesForPattern = new ArrayList<Category>();
						categoriesForPattern.add(ANY_CATEGORY);
						for (OWLIndividual category: EntitySearcher.getObjectPropertyValues(namedPattern, categorizationProperty, index)) {
							if (category.isNamed()) {
								
								// We've found a category; turn it into a Java object and and add to list
								OWLNamedIndividual namedCategory = (OWLNamedIndividual)category;
								List<String> categoryLabels = getLabels(namedCategory, index);
								String categoryLabel;
								if (categoryLabels.size() > 0) {
									categoryLabel = categoryLabels.get(0);
								}
								else {
									categoryLabel = namedCategory.getIRI().toString();
								}
								Category newCategory = new Category(categoryLabel, namedCategory.getIRI());
								categoriesForPattern.add(newCategory);
							}
						}
						
						// Go through the list of categories for pattern and generate the map structure Category -> List<Pattern> that we need 
						for (Category category: categoriesForPattern) {
							if (patternCategories.containsKey(category)) {
								List<Pattern> patternsForCategory = patternCategories.get(category);
								patternsForCategory.add(newPattern);
							}
							else {
								List<Pattern> patternsForCategory = new ArrayList<Pattern>();
								patternsForCategory.add(newPattern);
								patternCategories.put(category, patternsForCategory);
							}
						}
					}
				}
			}
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
		List<String> retVal = new ArrayList<String>();
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
		List<String> retVal = new ArrayList<String>();
		for(OWLLiteral literal: EntitySearcher.getDataPropertyValues(i, p, ontology)) {
			retVal.add(literal.getLiteral());
		}
		return retVal;
		
		
	}
	
	/**
	 * Returns a list of all categories (that have one or more patterns) in this library.
	 * @return
	 */
	public Category[] getPatternCategories() {
		// Get and sort all categories
		Set<Category> categorySet = patternCategories.keySet();
		List<Category> categoryList = new ArrayList<Category>(categorySet);
		Collections.sort(categoryList);
		// Put the Any category first in the list, for usability purposes
		categoryList.remove(ANY_CATEGORY);
		categoryList.add(0, ANY_CATEGORY);
		// Return as array
		return categoryList.toArray(new Category[categoryList.size()]);
	}
	
	/**
	 * Returns all the patterns for a given category.
	 * @param category
	 * @return
	 */
	public List<Pattern> getPatternsForCategory(Category category) {
		List<Pattern> returnedPatterns = patternCategories.get(category);
		Collections.sort(returnedPatterns);
		return returnedPatterns;
	}
}
