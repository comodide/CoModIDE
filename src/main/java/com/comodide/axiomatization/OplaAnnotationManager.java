package com.comodide.axiomatization;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.find.OWLEntityFinder;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.util.OWLEntityRenamer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.configuration.ComodideConfiguration;
import com.comodide.configuration.Namespaces;

/**
 * This is a central object for adding OPLa annotations to an ontology.
 * 
 * @author cogan
 *
 */
public class OplaAnnotationManager
{
	private static OplaAnnotationManager instance = null;

	public static OplaAnnotationManager getInstance(OWLModelManager modelManager)
	{
		if (instance == null)
		{
			instance = new OplaAnnotationManager(modelManager);
		}

		return instance;
	}

	/** Bookkeeping */
	private final Logger log = LoggerFactory.getLogger(OplaAnnotationManager.class);
	private final String pf  = "[CoModIDE:OPLaAnnotationManager] ";

	/** Used for adding annotations to the active ontology */
	private OWLModelManager  modelManager;
	private OWLOntology      owlOntology;
	private IRI              ontologyIRI;
	private OWLDataFactory   owlDataFactory;
	private OWLEntityFinder  owlEntityFinder;
	private OWLEntityRenamer owlEntityRenamer;

	private OplaAnnotationManager(OWLModelManager modelManager)
	{
		this.modelManager = modelManager;

		// Set reference to active ontology or the metadata ontology
		// depending on configuration settings
		if (ComodideConfiguration.getModuleMetadataExternal())
		{
			this.owlOntology = MetadataUtils.findOrCreateMetadataOntology(this.modelManager);
		}
		else
		{
			this.owlOntology = this.modelManager.getActiveOntology();
		}
		this.ontologyIRI = this.owlOntology.getOntologyID().getOntologyIRI().orNull();
		this.owlDataFactory = this.modelManager.getOWLDataFactory();
		this.owlEntityFinder = this.modelManager.getOWLEntityFinder();

		createEntityRenamer();
		log.info(pf + "successfully initialized.");
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

	public OWLNamedIndividual renameOplaModule(OWLNamedIndividual oldModule, String newLabel)
	{
			// Create new IRI for
			OWLNamedIndividual newModule    = createOplaModule(newLabel);
			IRI                newModuleIRI = newModule.getIRI();
			// Apply the renaming changes.
			List<OWLOntologyChange> changes = this.owlEntityRenamer.changeIRI(oldModule, newModuleIRI);
			this.modelManager.applyChanges(changes);

		return newModule;
	}

	public OWLNamedIndividual createOplaModule(String moduleName)
	{
		log.info("[CoModIDE:OPLaAnnotationManager] creating module: " + moduleName + ".");
		// Create an individual for the module
		IRI                moduleIRI = IRI.create(this.ontologyIRI.toString() + "#" + moduleName);
		OWLNamedIndividual module    = this.owlDataFactory.getOWLNamedIndividual(moduleIRI);
		// Create the module IRI
		IRI                    oplaModuleIRI   = IRI.create(Namespaces.OPLA_CORE_NAMESPACE + "Module");
		OWLClass               oplaModuleClass = this.owlDataFactory.getOWLClass(oplaModuleIRI);
		OWLClassAssertionAxiom caa             = this.owlDataFactory.getOWLClassAssertionAxiom(oplaModuleClass, module);
		// Wrap in change axiom
		AddAxiom aa = new AddAxiom(this.owlOntology, caa);
		// Add the axiom to the ontology
		this.modelManager.applyChange(aa);

		return module;
	}

	public void createIsNativeToAnnotation(IRI subject, IRI value)
	{
		// Create the IRI
		IRI isNativeToIRI = IRI.create(Namespaces.OPLA_CORE_NAMESPACE + "isNativeTo");
		// Wrap in owlapi
		OWLAnnotationProperty  ap = this.owlDataFactory.getOWLAnnotationProperty(isNativeToIRI);
		
		OWLAnnotation          an = this.owlDataFactory.getOWLAnnotation(ap, value);
		OWLAnnotationAssertionAxiom aa = this.owlDataFactory.getOWLAnnotationAssertionAxiom(subject, an);

		AddAxiom addAxiom = new AddAxiom(this.owlOntology, aa);
		this.modelManager.applyChange(addAxiom);
	}
	
	public void removeIsNativeToAnnotation(IRI subject, IRI value)
	{
		// Create the IRI
				IRI isNativeToIRI = IRI.create(Namespaces.OPLA_CORE_NAMESPACE + "isNativeTo");
				// Wrap in owlapi
				OWLAnnotationProperty  ap = this.owlDataFactory.getOWLAnnotationProperty(isNativeToIRI);
				
				OWLAnnotation          an = this.owlDataFactory.getOWLAnnotation(ap, value);
				OWLAnnotationAssertionAxiom aa = this.owlDataFactory.getOWLAnnotationAssertionAxiom(subject, an);

				RemoveAxiom removeAxiom = new RemoveAxiom(this.owlOntology, aa);
				this.modelManager.applyChange(removeAxiom);
	}
}
