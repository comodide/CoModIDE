package com.comodide.editor.changehandlers;

import java.util.List;
import java.util.Set;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.entity.EntityCreationPreferences;
import org.protege.editor.owl.model.find.OWLEntityFinder;
import org.semanticweb.owlapi.io.AnonymousIndividualProperties;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.OWLEntityRenamer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.axiomatization.AxiomManager;
import com.comodide.axiomatization.OplaAnnotationManager;
import com.comodide.editor.SchemaDiagram;
import com.comodide.editor.model.ClassCell;
import com.comodide.editor.model.ComodideCell;
import com.comodide.editor.model.DatatypeCell;
import com.comodide.editor.model.ModuleCell;
import com.comodide.editor.model.PropertyEdgeCell;
import com.comodide.exceptions.ComodideException;
import com.comodide.exceptions.NameClashException;
import com.comodide.rendering.PositioningOperations;
import com.mxgraph.model.mxCell;

public class LabelChangeHandler
{
	/** Logging */
	private static final Logger log = LoggerFactory.getLogger(LabelChangeHandler.class);

	/** Singleton reference to AxiomManager. Handles OWL entity constructions */
	private AxiomManager axiomManager;

	/**
	 * Singleton reference to OPLaAnnotationManager. Handles OPLa annotation
	 * constructions
	 */
	private OplaAnnotationManager oplaAnnotationmanager;

	/** Used for adding positional arguments to updatedCells */
	private OWLModelManager modelManager;

	public LabelChangeHandler(OWLModelManager modelManager, SchemaDiagram schemaDiagram)
	{
		this.modelManager = modelManager;
		this.axiomManager = AxiomManager.getInstance(modelManager);
		this.oplaAnnotationmanager = OplaAnnotationManager.getInstance(modelManager);
	}

	public OWLEntity handle(mxCell cell, String newLabel, Object[] children) throws ComodideException
	{
		// Error or cast
		if (!(cell instanceof ComodideCell))
		{
			throw new ComodideException(String.format(
					"[CoModIDE:LabelChangeHandler] The non-CoModIDE cell '%s' was found on the schema diagram. This should never happen.",
					cell));
		}

		ComodideCell comodideCell = (ComodideCell) cell;

		// Ensure that the IRI created by this new label is in fact new
		IRI             activeOntologyIri        = modelManager.getActiveOntology().getOntologyID().getOntologyIRI()
				.or(IRI.generateDocumentIRI());
		String          entitySeparator          = EntityCreationPreferences.getDefaultSeparator();
		IRI             newIRI                   = IRI
				.create(activeOntologyIri.toString() + entitySeparator + newLabel);
		OWLEntityFinder finder                   = modelManager.getOWLEntityFinder();
		Set<OWLEntity>  existingEntitiesWithName = finder.getEntities(newIRI);
		if (existingEntitiesWithName.size() > 0)
		{
			throw new NameClashException(String.format(
					"[CoModIDE:LabelChangeHandler] An OWL entity with the identifier '%s' already exists; unable to add another one.",
					newLabel));
		}

		if (comodideCell.isModule())
		{
			return handleModuleLabelChange(cell, newLabel, children);
		}
		else if (cell.isEdge())
		{
			return handleEdgeLabelChange(cell, newLabel);
		}
		else
		{
			return handleNodeLabelChange(cell, newLabel);
		}
	}

	private OWLEntity handleEdgeLabelChange(mxCell cell, String newLabel)
	{
		// Unpack useful things
		ComodideCell edgeCell = (ComodideCell) cell;
		OWLEntity    property = edgeCell.getEntity();

		if (property != null && property.isNamed() && !property.getIRI().equals(edgeCell.defaultIRI()))
		{
			// This is a renaming operation.
			// Construct new property IRI
			OWLOntology activeOntology    = modelManager.getActiveOntology();
			String      ontologyNamespace = activeOntology.getOntologyID().getOntologyIRI()
					.or(IRI.generateDocumentIRI()).toString();
			String      entitySeparator   = EntityCreationPreferences.getDefaultSeparator();
			IRI         newIRI            = IRI.create(ontologyNamespace + entitySeparator + newLabel);

			// Create and run renamer
			OWLOntologyManager ontologyManager = activeOntology.getOWLOntologyManager();
			OWLEntityRenamer   renamer         = new OWLEntityRenamer(ontologyManager, modelManager.getOntologies());
			// The below configuration, and corresponding reset to that configuration
			// two lines down, is a workaround for an OWLAPI bug;
			// see https://github.com/owlcs/owlapi/issues/892
			AnonymousIndividualProperties.setRemapAllAnonymousIndividualsIds(false);
			List<OWLOntologyChange> changes = renamer.changeIRI(property, newIRI);
			this.modelManager.applyChanges(changes);
			AnonymousIndividualProperties.resetToDefault();

			// Construct the OWLEntity to return
			OWLDataFactory factory   = ontologyManager.getOWLDataFactory();
			OWLEntity      newEntity = factory.getOWLEntity(property.getEntityType(), newIRI);

			// Return a reference entity based on the new IRI
			return newEntity;
		}
		else
		{
			// This is a new property creation operation.
			ComodideCell sourceCell = (ComodideCell) cell.getSource();
			ComodideCell targetCell = (ComodideCell) cell.getTarget();

			// Domain can not be a datatype
			if (sourceCell instanceof DatatypeCell)
			{
				log.warn("[CoModIDE:LabelChangeHandler] Cannot create axiom with datatype as domain.");
				return null;
			}

			OWLEntity domain = sourceCell.getEntity();
			OWLEntity range  = targetCell.getEntity();

			// Create the property
			if (targetCell instanceof DatatypeCell)
			{
				property = this.axiomManager.handleDataProperty(newLabel, domain, range);
				// Update positioning annotations for target which are stored on the data
				// property
				// (since the target datatype does not have own identity)
				for (OWLOntology ontology : modelManager.getOntologies())
				{
					if (ontology.containsEntityInSignature(property.getIRI()))
					{
						PositioningOperations.updateXYCoordinateAnnotations(property, ontology,
								targetCell.getGeometry().getX(), targetCell.getGeometry().getY());
					}
				}

			}
			else
			{
				property = this.axiomManager.handleObjectProperty(newLabel, domain, range);
			}
			return property;
		}
	}

	private OWLEntity handleNodeLabelChange(mxCell cell, String newLabel)
	{
		if (cell instanceof ClassCell)
		{
			ClassCell classCell = (ClassCell) cell;
			// Extract current class, if it is present
			OWLClass currentClass = null;
			if (classCell.isNamed())
			{
				// This is a renaming operation
				currentClass = classCell.getEntity().asOWLClass();

				OWLOntology activeOntology    = modelManager.getActiveOntology();
				String      ontologyNamespace = activeOntology.getOntologyID().getOntologyIRI()
						.or(IRI.generateDocumentIRI()).toString();
				String      entitySeparator   = EntityCreationPreferences.getDefaultSeparator();
				IRI         newIRI            = IRI.create(ontologyNamespace + entitySeparator + newLabel);

				// Create and run renamer
				OWLOntologyManager ontologyManager = activeOntology.getOWLOntologyManager();
				OWLEntityRenamer   renamer         = new OWLEntityRenamer(ontologyManager,
						modelManager.getOntologies());
				// The below configuration, and corresponding reset to that configuration
				// two lines down, is a workaround for an OWLAPI bug;
				// see https://github.com/owlcs/owlapi/issues/892
				AnonymousIndividualProperties.setRemapAllAnonymousIndividualsIds(false);
				List<OWLOntologyChange> changes = renamer.changeIRI(currentClass, newIRI);
				this.modelManager.applyChanges(changes);
				AnonymousIndividualProperties.resetToDefault();

				// Construct the OWLEntity to return
				OWLDataFactory factory   = ontologyManager.getOWLDataFactory();
				OWLEntity      newEntity = factory.getOWLEntity(currentClass.getEntityType(), newIRI);

				// Return a reference entity based on the new IRI
				return newEntity;
			}
			else
			{
				// This is a creation operation -- pass it off to the axiom manager thing
				return this.axiomManager.addNewClass(newLabel);
			}
		}
		return null;
	}

	private OWLEntity handleModuleLabelChange(mxCell cell, String newLabel, Object[] children)
	{
		// Cast the module cell
		ModuleCell moduleCell = (ModuleCell) cell;

		OWLNamedIndividual module = null;

		if (moduleCell.isNamed())
		{
			// Rename the module
			module = this.oplaAnnotationmanager.renameOplaModule(moduleCell.getEntity().asOWLNamedIndividual(),
					newLabel);
		}
		else
		{
			// Create the module
			module = this.oplaAnnotationmanager.createOplaModule(newLabel);

			// Create all isNativeTo annotations for the new module and its contents
			for (Object o : children)
			{
				// This should cover object and data properties
				if (o instanceof ClassCell || o instanceof PropertyEdgeCell || o instanceof ModuleCell)
				{
					ComodideCell tempModuleCell = (ComodideCell) o;
					OWLEntity    moduleClass    = (OWLEntity) tempModuleCell.getEntity();
					this.oplaAnnotationmanager.createIsNativeToAnnotation(moduleClass.getIRI(), module.getIRI());
				}
				// This is currently handled above.
//				else if(o instanceof ModuleCell)
//				{
//					
//				}
				else
				{
					// Don't do anything for DatatypeCells
					// Don't do anything for SubclassEdgeCells
				}
			}
		}

		return module;
	}
}
