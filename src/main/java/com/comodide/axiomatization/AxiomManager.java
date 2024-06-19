package com.comodide.axiomatization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.entity.EntityCreationPreferences;
import org.protege.editor.owl.model.find.OWLEntityFinder;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.util.OWLEntityRenamer;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.ComodideConfiguration;
import com.comodide.ComodideConfiguration.EdgeCreationAxiom;
import com.comodide.editor.model.PropertyEdgeCell;
import com.comodide.exceptions.MultipleMatchesException;
import com.mxgraph.model.mxCell;

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

	/**
	 * AxiomManager is a singleton class
	 */
	private static AxiomManager instance = null;

	public static AxiomManager getInstance(OWLModelManager modelManager)
	{
		if (instance == null)
		{
			instance = new AxiomManager(modelManager); // , schemaDiagram);
		}
		return instance;
	}

	/** Bookkeeping */
	private final Logger log = LoggerFactory.getLogger(AxiomManager.class);
	private final String pf  = "[CoModIDE:AxiomManager] ";

	/** Used for adding axioms to the active ontology */
	private OWLModelManager                modelManager;
	private OWLOntology                    owlOntology;
	private OWLDataFactory                 owlDataFactory;
	private OWLEntityFinder                owlEntityFinder;
	private OWLEntityRenamer               owlEntityRenamer;
	/** Used for adding OWLAx Axioms to the active ontology */
	private OWLAxAxiomFactory              owlaxAxiomFactory;
	/** Used for current namespace */
	private IRI                            iri;
	/** Used for parsing axioms added to the ontology */
	private SimpleAxiomParser              simpleAxiomParser;
	/** Used for generating human readable labels */
	private static final ShortFormProvider shortFormProvider = new SimpleShortFormProvider();

	private AxiomManager(OWLModelManager modelManager)
	{
		this.modelManager = modelManager;

		// Retrieve the active ontology
		this.owlOntology = this.modelManager.getActiveOntology();
		// Only continue with the update if there is an active ontology
		if (this.owlOntology != null)
		{
			// DataFactory is used to create axioms to add to the ontology
			this.owlDataFactory = this.modelManager.getOWLDataFactory();
			// The EntitiyFinder allows us to find existing entities within the
			// ontology
			this.owlEntityFinder = this.modelManager.getOWLEntityFinder();
			// The EntityRenamer does exactly what you think it does
			createEntityRenamer();
			// Get the namespace for the active ontology
			this.iri = this.owlOntology.getOntologyID().getOntologyIRI().orNull();
			// Create Parsers for axioms
			this.simpleAxiomParser = new SimpleAxiomParser();
			// Create the OWLAxAxiomFactory
			this.owlaxAxiomFactory = new OWLAxAxiomFactory(this.modelManager);
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

	public OWLAxiom createOWLAxAxiom(OWLAxAxiomType axiomType, PropertyEdgeCell edgeCell)
	{
		OWLAxiom axiom = this.owlaxAxiomFactory.createAxiomFromEdge(axiomType, edgeCell);

		return axiom;
	}

	public void addOWLAxAxiom(OWLAxAxiomType axiomType, PropertyEdgeCell edgeCell)
	{
		OWLAxiom axiom = createOWLAxAxiom(axiomType, edgeCell);

		OWLOntologyChange addAxiom = new AddAxiom(owlOntology, axiom);
		this.modelManager.applyChange(addAxiom);
	}

	public void addOWLAxAxiomtoBFO(OWLEntity source, OWLEntity target)
	{
		OWLClassExpression sourceExpression = source.asOWLClass();
		OWLClassExpression targetExpression = target.asOWLClass();
		// To be returned
		OWLAxiom owlaxAxiom = this.owlDataFactory.getOWLSubClassOfAxiom(sourceExpression, targetExpression);

		AddAxiom add = new AddAxiom(owlOntology, owlaxAxiom);
		this.modelManager.applyChange(add);
		// return owlaxAxiom;
	}

	public void addPropertyOWLAxAxiom(OWLEntity source, OWLEntity target)
	{
		OWLObjectPropertyExpression sourcePropertyExpression = source.asOWLObjectProperty();
		OWLObjectPropertyExpression targetPropertyExpression = target.asOWLObjectProperty();
		OWLAxiom                    owlaxAxiom               = this.owlDataFactory
				.getOWLSubObjectPropertyOfAxiom(sourcePropertyExpression, targetPropertyExpression);

		AddAxiom add = new AddAxiom(owlOntology, owlaxAxiom);
		this.modelManager.applyChange(add);
	}

	public void removePropertyOWLAxAxiom(OWLEntity source, OWLEntity target)
	{
		OWLObjectPropertyExpression sourcePropertyExpression = source.asOWLObjectProperty();
		OWLObjectPropertyExpression targetPropertyExpression = target.asOWLObjectProperty();
		OWLAxiom                    owlaxAxiom               = this.owlDataFactory
				.getOWLSubObjectPropertyOfAxiom(sourcePropertyExpression, targetPropertyExpression);
		// OWLAxiom axiom = this.owlaxAxiomFactory.createAxiomOfProperty(axiomType,
		// source, target);

		RemoveAxiom add = new RemoveAxiom(owlOntology, owlaxAxiom);
		this.modelManager.applyChange(add);

	}

	public void removeOWLAxAxiom(OWLAxAxiomType axiomType, PropertyEdgeCell edgeCell)
	{
		OWLAxiom axiom = createOWLAxAxiom(axiomType, edgeCell);

		OWLOntologyChange removeAxiom = new RemoveAxiom(owlOntology, axiom);
		this.modelManager.applyChange(removeAxiom);
	}

	public void removeOWLAxAxiomtoBFO(OWLEntity source, OWLEntity target)
	{
		OWLClassExpression sourceExpression = source.asOWLClass();
		OWLClassExpression targetExpression = target.asOWLClass();
		// To be returned
		OWLAxiom owlaxAxiom = this.owlDataFactory.getOWLSubClassOfAxiom(sourceExpression, targetExpression);
		// OWLAxiom axiom = this.owlaxAxiomFactory.createAxiom(axiomType, source,
		// property, target);

		RemoveAxiom add = new RemoveAxiom(owlOntology, owlaxAxiom);
		this.modelManager.applyChange(add);
	}

	/**
	 * This method will attempt to first find a Class with the existing targetName
	 * If it does not find it, it will create a Class with target name in the active
	 * ontology.
	 * <p>
	 * If the currentClass is null, then it will return the result from above.
	 * <p>
	 * If the currentClass is not null and targetName exists, it will return
	 * targetName
	 * <p>
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

	/**
	 * This method will add the created class to the ontology!!
	 */
	public OWLEntity findOrAddClass(String className)
	{
		OWLClass owlClass = findClass(className);

		if (owlClass == null)
		{
			owlClass = addNewClass(className);
		}

		return owlClass;
	}

	public OWLEntity findEntity(String entity) throws MultipleMatchesException
	{
		Set<OWLEntity> foundEntities = this.owlEntityFinder.getMatchingOWLEntities(entity);
		if (foundEntities.size() > 1)
		{
			throw new MultipleMatchesException(String.format(
					"[CoModIDE:AxiomManager] There are multiple OWL entities matching the identifier %s.", entity));
		}
		else if (foundEntities.isEmpty())
		{
			return null;
		}
		else
		{
			return foundEntities.iterator().next();
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
		String entitySeparator = EntityCreationPreferences.getDefaultSeparator();
		IRI    classIRI        = IRI.create(iri + entitySeparator + str);
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
		String                  entitySeparator = EntityCreationPreferences.getDefaultSeparator();
		IRI                     newIRI          = IRI.create(iri + entitySeparator + newName);
		List<OWLOntologyChange> changes         = this.owlEntityRenamer.changeIRI(oldClass, newIRI);
		// Apply the changes
		this.modelManager.applyChanges(changes);
		// Construct the OWLClass to return
		OWLClass owlClass = this.owlDataFactory.getOWLClass(newIRI);
		// Return a reference of the class based on the new IRI
		return owlClass;
	}

	/**
	 * This method should be called when newValue == null
	 */
	public void removeClass()
	{
		// TODO Why is this empty???!!! (3/15/20)
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

	// TODO finish documentation
	public OWLObjectProperty handleObjectProperty(String propertyName, OWLEntity domain, OWLEntity range)
	{
		// Perhaps the ObjectProperty already exists?
		OWLObjectProperty property = findObjectProperty(propertyName);

		// Create the ObjectProperty and add the requisite axioms
		if (property == null)
		{
			property = addNewObjectProperty(propertyName);

			// Get which axioms to create
			Set<EdgeCreationAxiom> axiomGenerationConfiguration = ComodideConfiguration.getSelectedEdgeCreationAxioms();

			/*
			 * These if statements will add an axiom for each of the EdgeCreationAxioms as
			 * selected in the Pattern Configuration view
			 */
			if (axiomGenerationConfiguration.contains(EdgeCreationAxiom.RDFS_DOMAIN_RANGE))
			{
				// Create the OWLAPI construct for the property domain/range restriction
				OWLObjectPropertyDomainAxiom opda = this.owlDataFactory.getOWLObjectPropertyDomainAxiom(property,
						domain.asOWLClass());
				OWLObjectPropertyRangeAxiom  opra = this.owlDataFactory.getOWLObjectPropertyRangeAxiom(property,
						range.asOWLClass());
				// Package into AddAxioms
				AddAxiom addDomainAxiom = new AddAxiom(this.owlOntology, opda);
				AddAxiom addRangeAxiom  = new AddAxiom(this.owlOntology, opra);
				// Make the change to the ontology
				ArrayList<AddAxiom> changes = new ArrayList<AddAxiom>(Arrays.asList(addDomainAxiom, addRangeAxiom));
				this.modelManager.applyChanges(changes);
			}

			if (axiomGenerationConfiguration.contains(EdgeCreationAxiom.SCOPED_DOMAIN))
			{
				OWLObjectSomeValuesFrom someValuesFrom = this.owlDataFactory.getOWLObjectSomeValuesFrom(property,
						range.asOWLClass());
				OWLSubClassOfAxiom      subClassAxiom  = this.owlDataFactory.getOWLSubClassOfAxiom(someValuesFrom,
						domain.asOWLClass());
				AddAxiom                addAxiomChange = new AddAxiom(this.owlOntology, subClassAxiom);
				this.modelManager.applyChange(addAxiomChange);
			}

			if (axiomGenerationConfiguration.contains(EdgeCreationAxiom.SCOPED_RANGE))
			{
				OWLObjectAllValuesFrom allValuesFrom  = this.owlDataFactory.getOWLObjectAllValuesFrom(property,
						range.asOWLClass());
				OWLSubClassOfAxiom     subClassAxiom  = this.owlDataFactory.getOWLSubClassOfAxiom(domain.asOWLClass(),
						allValuesFrom);
				AddAxiom               addAxiomChange = new AddAxiom(this.owlOntology, subClassAxiom);
				this.modelManager.applyChange(addAxiomChange);
			}
		}
		return property;
	}

	// TODO Document this
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
		String entitySeparator = EntityCreationPreferences.getDefaultSeparator();
		IRI    propertyIRI     = IRI.create(iri + entitySeparator + propertyName);
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
			Set<EdgeCreationAxiom> axioms = ComodideConfiguration.getSelectedEdgeCreationAxioms();

			/*
			 * These if statements will add an axiom for each of the EdgeCreationAxioms as
			 * selected in the Pattern Configuration view
			 */
			if (axioms.contains(EdgeCreationAxiom.RDFS_DOMAIN_RANGE))
			{
				// Create the OWLAPI construct for the property domain/range restriction
				OWLDataPropertyDomainAxiom opda = this.owlDataFactory.getOWLDataPropertyDomainAxiom(dataProperty,
						domain.asOWLClass());
				OWLDataPropertyRangeAxiom  opra = this.owlDataFactory.getOWLDataPropertyRangeAxiom(dataProperty,
						range.asOWLDatatype());
				// Package into AddAxioms
				AddAxiom addDomainAxiom = new AddAxiom(this.owlOntology, opda);
				AddAxiom addRangeAxiom  = new AddAxiom(this.owlOntology, opra);
				// Make the change to the ontology
				ArrayList<AddAxiom> changes = new ArrayList<AddAxiom>(Arrays.asList(addDomainAxiom, addRangeAxiom));
				this.modelManager.applyChanges(changes);
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
		String entitySeparator = EntityCreationPreferences.getDefaultSeparator();
		IRI    propertyIRI     = IRI.create(iri + entitySeparator + propertyName);
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

	public mxCell parseSimpleAxiom(OWLAxiom axiom)
	{
		return this.simpleAxiomParser.parseSimpleAxiom((OWLSubClassOfAxiom) axiom);
	}

	public boolean matchOWLAxAxiomType(OWLAxAxiomType axiomType, PropertyEdgeCell edgeCell)
	{
		OWLAxiom owlaxAxiom = this.owlaxAxiomFactory.createAxiomFromEdge(axiomType, edgeCell);

		for (OWLAxiom axiom : this.owlOntology.getAxioms())
		{
			if (axiom.equalsIgnoreAnnotations(owlaxAxiom))
			{
				return true;
			}
		}
		return false;
	}

	public boolean matchSubClassAxiom(OWLClass source, OWLClass target)
	{
		// Create the subclass axiom
		OWLAxiom subClassAxiom = this.owlDataFactory.getOWLSubClassOfAxiom(source, target);
		for (OWLAxiom axiom : this.owlOntology.getSubClassAxiomsForSubClass(source))
		{
			// If they're equal, finish
			if (axiom.equalsIgnoreAnnotations(subClassAxiom))
			{
				return true;
			}
		}
		return false;
	}

	public boolean matchSubPropertyAxiom(OWLEntity source, OWLObjectProperty targePropertyExpression)
	{
		OWLObjectPropertyExpression sourcePropertyExpression = source.asOWLObjectProperty();
		// Create the subclass axiom
		OWLAxiom subClassAxiom = this.owlDataFactory.getOWLSubObjectPropertyOfAxiom(sourcePropertyExpression,
				targePropertyExpression);
		for (OWLAxiom owlaxAxiom : this.owlOntology.getObjectSubPropertyAxiomsForSubProperty(sourcePropertyExpression))
		{
			// If they're equal, finish
			if (owlaxAxiom.equalsIgnoreAnnotations(subClassAxiom))
			{
				return true;
			}
		}
		return false;
	}
}
