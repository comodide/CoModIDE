package com.comodide.axiomatization;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.find.OWLEntityFinder;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.OWLEntityRenamer;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.patterns.PatternInstantiationConfiguration;
import com.comodide.patterns.PatternInstantiationConfiguration.EdgeCreationAxiom;
import com.comodide.rendering.editor.SchemaDiagram;
import com.comodide.rendering.sdont.parsing.AxiomParser;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGraphModel;

/**
 * The purpose of this class is to provide a single point of entry for the
 * management, creation, and addition of Axioms based on certain actions. For
 * example, add a new class to the ontology given a string, or rename a class,
 * etc.
 * 
 * @author cogan
 *
 */
public class AxiomManager
{
	/** AxiomManager is a singleton class */
	private static AxiomManager instance = null;

	public static AxiomManager getInstance(OWLModelManager modelManager, SchemaDiagram schemaDiagram)
	{
		if (instance == null)
		{
			return new AxiomManager(modelManager, schemaDiagram);
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

	/** Used for finding domains and ranges */
	private OWLReasoner reasoner;

	/** Used for current namespace */
	private IRI iri;

	/** Used for parsing axioms added to the ontology */
	private AxiomParser       axiomParser;
	private SimpleAxiomParser simpleAxiomParser;

	/** Used to retrieve cells representing classes from parsed axioms */
	private SchemaDiagram schemaDiagram;

	/** Used for generating human readable labels */
	private static final ShortFormProvider shortFormProvider = new SimpleShortFormProvider();

	private AxiomManager(OWLModelManager modelManager, SchemaDiagram schemaDiagram)
	{
		this.modelManager = modelManager;
		this.schemaDiagram = schemaDiagram;

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
			// Create Parsers for axioms
			this.axiomParser = new AxiomParser(this.owlDataFactory);
			this.simpleAxiomParser = new SimpleAxiomParser(schemaDiagram);
			// Get the Reasoner
			this.reasoner = this.modelManager.getReasoner();
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

	/**
	 * This method will attempt to first find a Class with the existing targetName
	 * If it does not find it, it will create a Class with target name in the active
	 * ontology.
	 * 
	 * If the currentClass is null, then it will return the result from above.
	 * 
	 * If the currentClass is not null and targetName exists, it will return
	 * targetName
	 * 
	 * If the currentClass is not null and targetName did not exist, it will rename
	 * currentClass to targetName
	 * 
	 * @return
	 */
	public OWLEntity handleClassChange(OWLClass currentClass, String targetName)
	{
		OWLClass targetClass = findClass(targetName);

		if (targetClass != null)
		{
			return targetClass;
		}

		if (currentClass == null)
		{
			return addNewClass(targetName);
		}
		else
		{
			return renameClass(currentClass, targetName);
		}
	}

	public OWLEntity handleClass(String className)
	{
		// Determine if the new class that we are naming the node exists
		OWLClass cls = findClass(className);

		// Null indicates that there was no class with that name, so create a new one
		if (cls == null)
		{
			return addNewClass(className);
		}
		else
		{
			return renameClass(cls, className);
		}

	}

	/** This method will add the created class to the ontology!! */
	public OWLEntity findOrAddClass(String className)
	{
		OWLClass owlClass = findClass(className);

		if (owlClass == null)
		{
			owlClass = addNewClass(className);
		}

		return owlClass;
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
				log.warn("[CoModIDE:AxiomManager] The returned class is not guaranteed to be the correct match.");
				throw new MultipleMatchesException("The returned class is not guaranteed to be the correct match.");
			}
			else if (classes.isEmpty())
			{
				// No class of that name was found.
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
	public OWLClass renameClass(OWLClass oldClass, String newName)
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

	public OWLObjectProperty handleObjectProperty(String propertyName, OWLEntity domain, OWLEntity range)
	{
		// Perhaps the ObjectProperty already exists?
		OWLObjectProperty property = findObjectProperty(propertyName);

		// Create the ObjectProperty and add the requisite axioms
		if (property == null)
		{
			property = addNewObjectProperty(propertyName);

			// Get which axioms to create
			Set<EdgeCreationAxiom> axioms = PatternInstantiationConfiguration.getSelectedEdgeCreationAxioms();

			/*
			 * These if statements will add an axiom for each of the EdgeCreationAxioms as
			 * selected in the Pattern Configuration view
			 */
			if (axioms.contains(EdgeCreationAxiom.RDFS_DOMAIN))
			{
				// Create the OWLAPI construct for the property domain restriction
				OWLObjectPropertyDomainAxiom opda = this.owlDataFactory.getOWLObjectPropertyDomainAxiom(property,
						domain.asOWLClass());
				// Package into AddAxiom
				AddAxiom addAxiom = new AddAxiom(this.owlOntology, opda);
				// Make the change to the ontology
				this.modelManager.applyChange(addAxiom);
			}

			if (axioms.contains(EdgeCreationAxiom.RDFS_RANGE))
			{
				// Create the OWLAPI construct for the property range restriction
				OWLObjectPropertyRangeAxiom opra = this.owlDataFactory.getOWLObjectPropertyRangeAxiom(property,
						range.asOWLClass());
				// Package into AddAxiom
				AddAxiom addAxiom = new AddAxiom(this.owlOntology, opra);
				// Make the change to the ontology
				this.modelManager.applyChange(addAxiom);
			}
		}

		return property;
	}

	public OWLObjectProperty findObjectProperty(String propertyName)
	{
		Set<OWLObjectProperty> properties = this.owlEntityFinder.getMatchingOWLObjectProperties(propertyName);

		if (properties.isEmpty())
		{
			return null;
		}
		else
		{
			for (OWLObjectProperty op : properties)
			{
				if (shortFormProvider.getShortForm(op).contentEquals(propertyName))
				{
					return op;
				}
			}
		}

		return null;
	}

	public OWLObjectProperty addNewObjectProperty(String propertyName)
	{
		// Create the IRI for the class using the active namespace
		IRI propertyIRI = IRI.create(this.iri + "#" + propertyName);
		// Create the OWLAPI construct for the class
		OWLObjectProperty objectProperty = this.owlDataFactory.getOWLObjectProperty(propertyIRI);
		// Create the Declaration Axiom
		OWLDeclarationAxiom oda = this.owlDataFactory.getOWLDeclarationAxiom(objectProperty);
		// Create the Axiom Change
		AddAxiom addAxiom = new AddAxiom(this.owlOntology, oda);
		// Apply the change to the active ontology!
		this.modelManager.applyChange(addAxiom);
		// Return a reference to the class that was added
		return objectProperty;
	}

	public OWLDataProperty handleDataProperty(String propertyName, OWLEntity domain, OWLEntity range)
	{
		OWLDataProperty dataProperty = findDataProperty(propertyName);

		if (dataProperty == null)
		{
			dataProperty = addNewDataProperty(propertyName);

			// Get which axioms to create
			Set<EdgeCreationAxiom> axioms = PatternInstantiationConfiguration.getSelectedEdgeCreationAxioms();

			/*
			 * These if statements will add an axiom for each of the EdgeCreationAxioms as
			 * selected in the Pattern Configuration view
			 */
			if (axioms.contains(EdgeCreationAxiom.RDFS_DOMAIN))
			{
				// Create the OWLAPI construct for the property domain restriction
				OWLDataPropertyDomainAxiom opda = this.owlDataFactory.getOWLDataPropertyDomainAxiom(dataProperty,
						domain.asOWLClass());
				// Package into AddAxiom
				AddAxiom addAxiom = new AddAxiom(this.owlOntology, opda);
				// Make the change to the ontology
				this.modelManager.applyChange(addAxiom);
			}

			if (axioms.contains(EdgeCreationAxiom.RDFS_RANGE))
			{
				// Create the OWLAPI construct for the property range restriction
				OWLDataPropertyRangeAxiom opra = this.owlDataFactory.getOWLDataPropertyRangeAxiom(dataProperty,
						range.asOWLDatatype());
				// Package into AddAxiom
				AddAxiom addAxiom = new AddAxiom(this.owlOntology, opra);
				// Make the change to the ontology
				this.modelManager.applyChange(addAxiom);
			}

		}

		return dataProperty;
	}

	// TODO document this
	public OWLDataProperty findDataProperty(String propertyName)
	{
		Set<OWLDataProperty> properties = this.owlEntityFinder.getMatchingOWLDataProperties(propertyName);

		if (properties.isEmpty())
		{
			return null;
		}
		else
		{
			for (OWLDataProperty dp : properties)
			{
				if (shortFormProvider.getShortForm(dp).contentEquals(propertyName))
				{
					return dp;
				}
			}
		}

		return null;
	}

	public OWLDataProperty addNewDataProperty(String propertyName)
	{
		// Create the IRI for the class using the active namespace
		IRI propertyIRI = IRI.create(this.iri + "#" + propertyName);
		// Create the OWLAPI construct for the class
		OWLDataProperty dataProperty = this.owlDataFactory.getOWLDataProperty(propertyIRI);
		// Create the Declaration Axiom
		OWLDeclarationAxiom oda = this.owlDataFactory.getOWLDeclarationAxiom(dataProperty);
		// Create the Axiom Change
		AddAxiom addAxiom = new AddAxiom(this.owlOntology, oda);
		// Apply the change to the active ontology!
		this.modelManager.applyChange(addAxiom);
		// Return a reference to the class that was added
		return dataProperty;
	}

	public EdgeContainer parseSimpleAxiom(OWLAxiom axiom)
	{
		return this.simpleAxiomParser.parseSimpleAxiom((OWLSubClassOfAxiom) axiom);
	}

	public EdgeContainer handleObjectPropertyDomain(OWLOntology ontology, OWLAxiom axiom)
	{
		OWLObjectPropertyDomainAxiom domainAxiom = (OWLObjectPropertyDomainAxiom) axiom;
		// Unpack from axiom
		OWLObjectProperty objectProperty = domainAxiom.getProperty().asOWLObjectProperty();
		OWLClass          domain         = domainAxiom.getDomain().asOWLClass();
		// Get shortforms
		String propertyName = shortFormProvider.getShortForm(objectProperty);
		String domainLabel  = shortFormProvider.getShortForm(domain);
		// Extract the Ranges for the object property
		Set<OWLObjectPropertyRangeAxiom> ranges = ontology.getObjectPropertyRangeAxioms(objectProperty);

		// Draw edge only if there is domain and range pair. Warn if multiple ranges.
		if (!ranges.isEmpty())
		{
			if (ranges.size() == 1)
			{
				// Get Range
				OWLClass range = ((OWLObjectPropertyRangeAxiom) ranges.toArray()[0]).getRange().asOWLClass();
				// Shortform
				String rangeLabel = shortFormProvider.getShortForm(range);

				// Obtain associated cells using the labels
				Map<String, Object> cells      = ((mxGraphModel) this.schemaDiagram.getModel()).getCells();
				Object              domainCell = cells.get(domainLabel);
				Object              rangeCell  = cells.get(rangeLabel);

				// Package and return
				return new EdgeContainer(propertyName, axiom, domainCell, rangeCell, "standardStyle");
			}
			else
			{
				log.warn("[CoModIDE:AxiomManager] Multiple domains found for property.");
			}
		}
		else
		{
			// Do nothing
			// i.e. if there is not an accompanying range to the domain, an edge can't be
			// drawn
		}

		return null;
	}

	public EdgeContainer handleObjectPropertyRange(OWLOntology ontology, OWLAxiom axiom)
	{
		OWLObjectPropertyRangeAxiom rangeAxiom = (OWLObjectPropertyRangeAxiom) axiom;
		// Unpack from axiom
		OWLObjectProperty objectProperty = rangeAxiom.getProperty().asOWLObjectProperty();
		OWLClass          range          = rangeAxiom.getRange().asOWLClass();
		// Get shortforms
		String propertyName = shortFormProvider.getShortForm(objectProperty);
		String rangeLabel   = shortFormProvider.getShortForm(range);
		// Extract the Ranges for the object property
		// For some reason the reasoner object was not finding any thing. I am not sure
		// why, thus
		// the propagation of ontology into this method.
		Set<OWLObjectPropertyDomainAxiom> domains = ontology.getObjectPropertyDomainAxioms(objectProperty);

		// Draw edge only if there is domain and range pair. Warn if multiple ranges.
		if (!domains.isEmpty())
		{
			if (domains.size() == 1)
			{
				// Get Domain
				OWLClass domain = ((OWLObjectPropertyDomainAxiom) domains.toArray()[0]).getDomain().asOWLClass();
				// Shortform
				String domainLabel = shortFormProvider.getShortForm(domain);

				// Obtain associated cells using the labels
				Map<String, Object> cells      = ((mxGraphModel) this.schemaDiagram.getModel()).getCells();
				Object              domainCell = cells.get(domainLabel);
				Object              rangeCell  = cells.get(rangeLabel);

				// Package and return
				return new EdgeContainer(propertyName, axiom, domainCell, rangeCell, "standardStyle");
			}
			else
			{
				log.warn("[CoModIDE:AxiomManager] Multiple domains found for property.");
			}
		}
		else
		{
			// Do nothing
			// i.e. if there is not an accompanying range to the domain, an edge can't be
			// drawn
		}

		return null;
	}

	public EdgeContainer handleDataPropertyDomain(OWLOntology ontology, OWLAxiom axiom)
	{
		OWLDataPropertyDomainAxiom domainAxiom = (OWLDataPropertyDomainAxiom) axiom;
		// Unpack from axiom
		OWLDataProperty dataProperty = domainAxiom.getProperty().asOWLDataProperty();
		OWLClass        domain       = domainAxiom.getDomain().asOWLClass();
		// Get shortforms
		String propertyName = shortFormProvider.getShortForm(dataProperty);
		String domainLabel  = shortFormProvider.getShortForm(domain);
		// Extract the Ranges for the object property
		Set<OWLDataPropertyRangeAxiom> ranges = ontology.getDataPropertyRangeAxioms(dataProperty);

		// Draw edge only if there is domain and range pair. Warn if multiple ranges.
		if (!ranges.isEmpty())
		{
			if (ranges.size() == 1)
			{
				// Get Range
				OWLClass range = ((OWLObjectPropertyRangeAxiom) ranges.toArray()[0]).getRange().asOWLClass();
				// Shortform
				String rangeLabel = shortFormProvider.getShortForm(range);

				// Obtain associated cells using the labels
				Map<String, Object> cells      = ((mxGraphModel) this.schemaDiagram.getModel()).getCells();
				Object              domainCell = cells.get(domainLabel);
				Object              rangeCell  = cells.get(rangeLabel);

				// Package and return
				return new EdgeContainer(propertyName, axiom, domainCell, rangeCell, "standardStyle");
			}
			else
			{
				log.warn("[CoModIDE:AxiomManager] Multiple domains found for property.");
			}
		}
		else
		{
			// Do nothing
			// i.e. if there is not an accompanying range to the domain, an edge can't be
			// drawn
		}

		return null;
	}

	public EdgeContainer handleDataPropertyRange(OWLOntology ontology, OWLAxiom axiom)
	{
		OWLDataPropertyRangeAxiom rangeAxiom = (OWLDataPropertyRangeAxiom) axiom;
		// Unpack from axiom
		OWLDataProperty dataProperty = rangeAxiom.getProperty().asOWLDataProperty();
		// Get shortforms
		String propertyName = shortFormProvider.getShortForm(dataProperty);
		Set<OWLDataPropertyDomainAxiom> domains = ontology.getDataPropertyDomainAxioms(dataProperty);

		// Draw edge only if there is exactly one domain. Warn if multiple domains.
		if (!domains.isEmpty())
		{
			if (domains.size() == 1)
			{
				// Get domain
				OWLClass domain = ((OWLDataPropertyDomainAxiom) domains.toArray()[0]).getDomain().asOWLClass();
				String domainLabel = shortFormProvider.getShortForm(domain);

				// Iterate through all cells to find the one whose ID matches the domain
				Map<String, Object> cells      = ((mxGraphModel) this.schemaDiagram.getModel()).getCells();
				for (String key: cells.keySet()) {
					mxCell cell = (mxCell)cells.get(key);
					if (cell.getId().equals(domainLabel)) {
						// Package and return
						// Sending a null target, since the target cell will be created in the calling method
						return new EdgeContainer(propertyName, axiom, cell, null, "standardStyle");
					}
				}
			}
			else
			{
				log.warn("[CoModIDE:AxiomManager] Multiple domains found for property.");
			}
		}
		else
		{
			// Do nothing
			// i.e. if there is not an accompanying domain to the range, an edge can't be
			// drawn
		}

		return null;
	}
}
