package com.comodide.axiomatization;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.find.OWLEntityFinder;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AxiomManager
{
	private static AxiomManager instance = null;

	public static AxiomManager getInstance(OWLModelManager modelManager)
	{
		if (instance == null)
		{
			return new AxiomManager(modelManager);
		}
		else
		{
			return instance;
		}
	}

	/** Bookkeeping */
	private final Logger log = LoggerFactory.getLogger(AxiomManager.class);
	private final String pf  = "[CoModIDE:AxiomManager] ";

	/** Used for adding axioms to the active ontology */
	private OWLModelManager modelManager;
	private OWLOntology     owlOntology;
	private OWLDataFactory  owlDataFactory;
	private OWLEntityFinder owlEntityFinder;

	/** default namespace */
	private PrefixDocumentFormat pdf;
	private String               prefix;

	private IRI iri;

	private AxiomManager(OWLModelManager modelManager)
	{
		this.modelManager = modelManager;

		// Retrieve the active ontology
		this.owlOntology = this.modelManager.getActiveOntology();
		// Only continue with the update if there is an active ontology
		if (this.owlOntology != null)
		{
			// DataFactory is used to create axioms to add to the ontology
			this.owlDataFactory = owlOntology.getOWLOntologyManager().getOWLDataFactory();
			// The EntitiyFinder allows us to find existing entities within the
			// ontology
			this.owlEntityFinder = this.modelManager.getOWLEntityFinder();
			// Get the namespace for the active ontology
			this.iri = this.owlOntology.getOntologyID().getOntologyIRI().orNull();
		}
		else
		{
			log.warn("[CoModIDE:AxiomAdder] active ontology is null.");
		}
	}

	/** This method is called when previousLabel.equals("") */
	public OWLClass addNewClass(String str)
	{
		/* Construct the Declaration Axiom */
		// Create the IRI for the class using the active namespace
		IRI classIRI = IRI.create(this.iri + "#" + str);
		// Create the OWLAPI construct for the class
		OWLClass owlClass = this.owlDataFactory.getOWLClass(classIRI);
		// Create the Declaration Axiom
		OWLDeclarationAxiom oda = this.owlDataFactory.getOWLDeclarationAxiom(owlClass);
		// Create the Axiom Change
		AddAxiom addAxiom = new AddAxiom(this.owlOntology, oda);
		// Apply the change to the active ontology!
		this.modelManager.applyChange(addAxiom);
		// Return a reference to the class that was added
		return owlClass;
	}

	/** This method should be called when !previousLabel.equals("") */
	public void replaceClass(Object value)
	{

	}

	/** This method should be called when newValue == null */
	public void removeClass()
	{

	}
	
	public List<OWLDatatype> addDatatype(String datatype)
	{
		// Determine if datatype property exists?
		Set<OWLDatatype> datatypes = this.owlEntityFinder.getMatchingOWLDatatypes(datatype);
		// return the matches
		return new ArrayList<>(datatypes);
	}
}
