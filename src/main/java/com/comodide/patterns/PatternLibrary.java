package com.comodide.patterns;

import java.io.InputStream;
import java.util.ArrayList;
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
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatternLibrary {
	
    private static PatternLibrary instance;
    
    private static final Logger log = LoggerFactory.getLogger(PatternLibrary.class);
    
    OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
    
    private final IRI CATEGORY_IRI = IRI.create("http://ontologydesignpatterns.org/opla#Category"); 
    public final Category ANY_CATEGORY = new Category("Any", IRI.create("https://w3id.org/comodide/ModlIndex#AnyCategory"));
    
    public static synchronized PatternLibrary getInstance() {
        if(instance == null){
            instance = new PatternLibrary();
        }
        return instance;
    }
	
	
	OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	OWLDataFactory factory = manager.getOWLDataFactory();
	OWLOntology index;
	
	private PatternLibrary() {
		
		try {
			ClassLoader classloader = this.getClass().getClassLoader();
			InputStream is = classloader.getResourceAsStream("modl/ModlIndex.owl");
			index = manager.loadOntologyFromOntologyDocument(is);
			patternCategories.put(ANY_CATEGORY, new ArrayList<Pattern>());
			OWLClass categoryClass = factory.getOWLClass(CATEGORY_IRI);
			for (OWLIndividual category: EntitySearcher.getIndividuals(categoryClass, index)) {
				if (category.isNamed()) {
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
					patternCategories.put(newCategory, new ArrayList<Pattern>());
				}
			}
		} 
		catch (Exception e) {
			log.error("Unable to load index; setting empty index.", e);
			e.printStackTrace();
		}
	}
	
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
	
	private Map<Category,List<Pattern>> patternCategories = new HashMap<Category,List<Pattern>>();
	
	
	private String[][] patternsTablePlaceholderData = {
    		{"Action","http://www.ontologydesignpatterns.org/cp/owl/action.owl"},
    		{"Description","http://www.ontologydesignpatterns.org/cp/owl/description.owl"},
    		{"Nary Relation","http://www.ontologydesignpatterns.org/cp/owl/naryrelation.owl"},
    		{"Participation","http://www.ontologydesignpatterns.org/cp/owl/participation.owl"},
    		{"Object Record","http://www.ontologydesignpatterns.org/cp/owl/objectrecord.owl"},
    		{"Communication Event","http://www.ontologydesignpatterns.org/cp/owl/communicationevent.owl"},
    		{"Event Core","http://www.ontologydesignpatterns.org/cp/owl/eventcore.owl"},
    		{"Information Realization","http://www.ontologydesignpatterns.org/cp/owl/informationrealization.owl"},
    };

	public Category[] getPatternCategories() {
		Set<Category> categorySet = patternCategories.keySet();
		return categorySet.toArray(new Category[categorySet.size()]);
	}
	
	public String[][] getPatternsForCategory(Category category) {
		// TODO: Implement actual selection of suitable patterns from categories
		if (category == ANY_CATEGORY) {
			return this.patternsTablePlaceholderData;
		}
		else {
			return this.patternsTablePlaceholderData;
		}
	}
}
