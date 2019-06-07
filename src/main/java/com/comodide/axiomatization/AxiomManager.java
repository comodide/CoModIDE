package com.comodide.axiomatization;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.find.OWLEntityFinder;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.OWLEntityRenamer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.rendering.sdont.parsing.AxiomParser;

public class AxiomManager
{
	/** AxiomManager is a singleton class */
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
	private OWLModelManager  modelManager;
	private OWLOntology      owlOntology;
	private OWLDataFactory   owlDataFactory;
	private OWLEntityFinder  owlEntityFinder;
	private OWLEntityRenamer owlEntityRenamer;

	/** Used for current namespace */
	private IRI iri;

	/** Used for parsing axioms added to the ontology */
	private AxiomParser axiomParser;
	
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
			// The EntityRenamer does exactly what you think it does
			createEntityRenamer();
			// Get the namespace for the active ontology
			this.iri = this.owlOntology.getOntologyID().getOntologyIRI().orNull();
			// Create the axiom parser
			this.axiomParser = new AxiomParser(this.owlDataFactory);
				
		}
		else
		{
			log.warn(pf + "active ontology is null.");
		}
	}

	private void createEntityRenamer()
	{
		// Get the Ontology Manager
		OWLOntologyManager ontologyManager = this.owlOntology.getOWLOntologyManager();
		// Embed the active ontology into a Set
		Set<OWLOntology> list = new HashSet<>();
		list.add(this.owlOntology);
		// Create the EntityRenamer
		this.owlEntityRenamer = new OWLEntityRenamer(ontologyManager, list);
	}

	public OWLEntity handleClass(String clazz)
	{
		OWLClass cls = findClass(clazz);

		if (cls == null)
		{
			return addNewClass(clazz);
		}
		else
		{
			return replaceClass(cls, clazz);
		}
	}

	public OWLClass findClass(String clazz)
	{
		// Get the list of matches
		// Based on how it is used here, we should never see more than one match.
		// TODO sanitize for wildcard
		Set<OWLClass> classes = this.owlEntityFinder.getMatchingOWLClasses(clazz);

		// Do some basic handling of potential errors
		try
		{
			if (classes.size() > 1)
			{
				throw new MultipleMatchesException("The returned class is not guaranteed to be the correct match.");
			}
			else if (classes.isEmpty())
			{
				// I hate this
				return null;
			}
			else
			{
				// Return the matched class
				OWLClass cls = (new ArrayList<OWLClass>(classes)).get(0);
				return cls;
			}
		}
		catch (MultipleMatchesException e)
		{
			// Return something? or is it better to return nothing?
			OWLClass cls = (new ArrayList<OWLClass>(classes)).get(0);
			return cls;
		}
	}

	public OWLDatatype findDatatype(String datatype)
	{
		// Determine if datatype property exists?
		Set<OWLDatatype> datatypes = this.owlEntityFinder.getMatchingOWLDatatypes(datatype);

		// Do some basic handling of potential errors
		try
		{
			if (datatypes.size() > 1)
			{
				throw new MultipleMatchesException("The returned class is not guaranteed to be the correct match.");
			}
			else if (datatypes.isEmpty())
			{
				// I hate this
				return null;
			}
			else
			{
				// Return the matched class
				OWLDatatype matchedDatatype = (new ArrayList<OWLDatatype>(datatypes)).get(0);
				return matchedDatatype;
			}
		}
		catch (MultipleMatchesException e)
		{
			// Return something? or is it better to return nothing?
			OWLDatatype cls = (new ArrayList<OWLDatatype>(datatypes)).get(0);
			return cls;
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
	public OWLClass replaceClass(OWLClass oldClass, String newName)
	{
		// Construct new class IRI
		IRI newIRI = IRI.create(this.iri + "#" + newName);
		// Get all OntologyChanges from the EntityRenamer
		List<OWLOntologyChange> changes = this.owlEntityRenamer.changeIRI(oldClass, newIRI);
		// Apply the changes
		this.modelManager.applyChanges(changes);
		// Construct the OWLClass to return
		OWLClass owlClass = this.owlDataFactory.getOWLClass(newIRI);
		// Return a reference of the class based on the new IRI
		return owlClass;
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
	
	public void addAxiom(OWLSubClassOfAxiom axiom)
	{
		// FIXME
//		SDEdge edge = this.axiomParser.parseAxiom(axiom);
	
		
	}
}
