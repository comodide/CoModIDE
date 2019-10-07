package com.comodide.editor.changehandlers;

import java.util.Collections;
import java.util.List;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.entity.EntityCreationPreferences;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.OWLEntityRenamer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.axiomatization.AxiomManager;
import com.comodide.editor.SchemaDiagram;
import com.comodide.editor.model.ClassCell;
import com.comodide.editor.model.ComodideCell;
import com.comodide.editor.model.DatatypeCell;
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

	/** Used for adding positional arguments to updatedCells */
	private OWLModelManager modelManager;
	
	public LabelChangeHandler(OWLModelManager modelManager, SchemaDiagram schemaDiagram)
	{
		this.axiomManager = AxiomManager.getInstance(modelManager, schemaDiagram);
		this.modelManager = modelManager;
	}

	public OWLEntity handle(mxCell cell, String newLabel) throws ComodideException
	{
		if (!(cell instanceof ComodideCell)) {
			throw new ComodideException(String.format("[CoModIDE:LabelChangeHandler] The non-CoModIDE cell '%s' was found on the schema diagram. This should never happen.", cell));
		}
		OWLEntity existingEntityWithName = this.axiomManager.findEntity(newLabel);
		if (existingEntityWithName != null) {
			throw new NameClashException(String.format("[CoModIDE:LabelChangeHandler] An OWL entity with the identifier '%s' already exists; unable to add another one.", newLabel));
		}
		cell.setId(newLabel);
		if (cell.isEdge())
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
		ComodideCell edgeCell = (ComodideCell)cell;
		OWLEntity property = edgeCell.getEntity();

		if (property != null) {
			// This is a renaming operation.
			// Construct new property IRI
			OWLOntology activeOntology = modelManager.getActiveOntology();
			String ontologyNamespace = activeOntology.getOntologyID().getOntologyIRI().orNull().toString();
			String entitySeparator = EntityCreationPreferences.getDefaultSeparator();
			IRI newIRI = IRI.create(ontologyNamespace + entitySeparator + newLabel);
			
			// Create and run renamer
			OWLOntologyManager ontologyManager = activeOntology.getOWLOntologyManager();
			OWLEntityRenamer renamer = new OWLEntityRenamer(ontologyManager, Collections.singleton(activeOntology));
			List<OWLOntologyChange> changes = renamer.changeIRI(property.getIRI(), newIRI);
			this.modelManager.applyChanges(changes);
			
			// Construct the OWLClass to return
			OWLDataFactory factory = ontologyManager.getOWLDataFactory();
			OWLEntity newEntity = factory.getOWLEntity(property.getEntityType(), newIRI);
			
			// Return a reference entity based on the new IRI
			return newEntity;
		}
		else {
			// This is a new property creation operation. 
		
			ComodideCell sourceCell = (ComodideCell)cell.getSource();
			ComodideCell targetCell = (ComodideCell)cell.getTarget();
			
			// Domain can not be a datatype
			if(sourceCell instanceof DatatypeCell)
			{
				log.warn("[CoModIDE:LabelChangeHandler] Cannot create axiom with datatype as domain.");
				return null;
			}
			
			OWLEntity domain = sourceCell.getEntity();
			OWLEntity range = targetCell.getEntity();
			
			// Create the property
			if(targetCell instanceof DatatypeCell)
			{
				property = this.axiomManager.handleDataProperty(newLabel, domain, range);
				// Update positioning annotations for target which are stored on the data property
				// (since the target datatype does not have own identity)
				for (OWLOntology ontology : modelManager.getOntologies())
				{
					if (ontology.containsEntityInSignature(property.getIRI()))
					{
						PositioningOperations.updateXYCoordinateAnnotations(property, ontology, targetCell.getGeometry().getX(), targetCell.getGeometry().getY());
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
		Double newX = cell.getGeometry().getX();
		Double newY = cell.getGeometry().getY();
		
		if (cell instanceof ClassCell)
		{
			ClassCell classCell = (ClassCell)cell;
			// Extract current class, if it is present
			// THis is a bit ugly and should be refactored deeper down in the code
			OWLClass currentClass = null;
			if (classCell.isNamed()) {
				currentClass = classCell.getEntity().asOWLClass();
			}
			
			// Pass the label onto the AxiomManager
			// It will attempt to find if the class exists,
			// Otherwise, it will create a new class
			OWLEntity classEntity = axiomManager.handleClassChange(currentClass, newLabel);
			
			// Check which of the loaded ontologies hosts the OWL entity representing
			// the class and update the positioning annotations on the entity in those ontologies.
			for (OWLOntology ontology : modelManager.getOntologies())
			{
				if (ontology.containsEntityInSignature(classEntity.getIRI()))
				{
					PositioningOperations.updateXYCoordinateAnnotations(classEntity, ontology, newX, newY);
				}
			}
			
			return classEntity;
		}
		return null;
	}
}
