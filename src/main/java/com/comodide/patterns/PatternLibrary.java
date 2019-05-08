package com.comodide.patterns;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class PatternLibrary {
	
    private static PatternLibrary instance;
    
    private static final Logger log = LoggerFactory.getLogger(PatternLibrary.class);
    
    OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
    
    private static final IRI CATEGORY_IRI = IRI.create("http://ontologydesignpatterns.org/opla#Category"); 
    
    public static synchronized PatternLibrary getInstance() {
        if(instance == null){
            instance = new PatternLibrary();
        }
        return instance;
    }
	
	
	OWLOntologyManager manager;
	OWLOntology index;
	
	private PatternLibrary() {
		
		
		patternCategories = new ArrayList<String>();
		manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory df = manager.getOWLDataFactory();
		//OWLAnnotationProperty label = df.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
		try {
			ClassLoader classloader = this.getClass().getClassLoader();
			InputStream is = classloader.getResourceAsStream("modl/ModlIndex.owl");
			index = manager.loadOntologyFromOntologyDocument(is);
			OWLReasoner reasoner = reasonerFactory.createReasoner(index);
			OWLClass categoryClass = df.getOWLClass(CATEGORY_IRI);

			NodeSet<OWLNamedIndividual> categoriesSet =  reasoner.getInstances(categoryClass, true);
			for (OWLNamedIndividual category: categoriesSet.getFlattened()) {
				patternCategories.add(category.toString());
				log.error(category.toString());
			}
		} 
		catch (Exception e) {
			log.error("Unable to load index; setting empty index.", e);
			e.printStackTrace();
		}
	}
	
	private List<String> patternCategories = Lists.newArrayList("Academy",
    		"Agriculture",
    		"Biology",
    		"Building and Construction",
    		"Business");
	
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

	public String[] getPatternCategories() {
		return patternCategories.toArray(new String[patternCategories.size()]);
	}
	
	public String[][] getPatterns() {
		return this.patternsTablePlaceholderData;
	}
}
