package com.comodide.sdont.parsing;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public class OWLConnector
{
	private OWLOntology			ontology;
	private OWLDataFactory		dataFactory;

	public OWLConnector(String filename) throws OWLOntologyCreationException
	{
		// Get an ontology manager and datafactory
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		this.dataFactory = manager.getOWLDataFactory();
		// Force silent import errors
		manager.setOntologyLoaderConfiguration(manager.getOntologyLoaderConfiguration()
		        .setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT));
		// Create a file from the name
		File file = new File(filename);
		IRI iri = IRI.create(file.toURI());
		// Load Ontology
		this.ontology = manager.loadOntologyFromOntologyDocument(iri);
	}

	public OWLConnector(File file) throws OWLOntologyCreationException
	{
		// Get an ontology manager and datafactory
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		this.dataFactory = manager.getOWLDataFactory();
		// Force silent import errors
		manager.setOntologyLoaderConfiguration(manager.getOntologyLoaderConfiguration()
		        .setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT));
		IRI iri = IRI.create(file.toURI());
		// Load Ontology
		this.ontology = manager.loadOntologyFromOntologyDocument(iri);
	}
	
	public List<OWLClass> retrieveClasses()
	{
		return asList(this.ontology.getClassesInSignature());
	}
	
	public List<OWLDatatype> retrieveDatatypes()
	{
		return asList(this.ontology.getDatatypesInSignature());
	}
	
	public List<OWLObjectProperty> retrieveObjectProperties()
	{
		return asList(this.ontology.getObjectPropertiesInSignature());
	}
	
	public List<OWLDataProperty> retrieveDataProperties()
	{
		return asList(this.ontology.getDataPropertiesInSignature());
	}
	
	public List<OWLAxiom> retrieveAxioms()
	{
		return asList(this.ontology.getAxioms());
	}
	
	public List<OWLSubClassOfAxiom> retrieveSubClassAxioms()
	{
		return asList(this.ontology.getAxioms(AxiomType.SUBCLASS_OF));
	}
	
	@SuppressWarnings("deprecation")
    public List<OWLObjectPropertyAxiom> retreiveAxiomsRelatedToObjProp(OWLObjectProperty op)
	{
		return asList(this.ontology.getAxioms(op));
	}
	
	@SuppressWarnings("deprecation")
    public List<OWLDataPropertyAxiom> retreiveAxiomsRelatedToDataProp(OWLDataProperty dp)
	{
		return asList(this.ontology.getAxioms(dp));
	}
	
	private static <T> List<T> asList(Set<T> s)
	{
	    return new ArrayList<T>(s);
	}
	
	public OWLOntology getOntology()
	{
		return ontology;
	}

	public OWLDataFactory getDataFactory()
	{
		return dataFactory;
	}

	public void setOntology(OWLOntology ontology)
	{
		this.ontology = ontology;
	}

	public void setDataFactory(OWLDataFactory dataFactory)
	{
		this.dataFactory = dataFactory;
	}
}
