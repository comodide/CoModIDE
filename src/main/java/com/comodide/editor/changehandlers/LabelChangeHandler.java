package com.comodide.editor.changehandlers;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.axiomatization.AxiomManager;
import com.comodide.editor.SchemaDiagram;
import com.comodide.editor.model.ClassCell;
import com.comodide.editor.model.ComodideCell;
import com.comodide.editor.model.DatatypeCell;
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

	public OWLEntity handle(mxCell cell, String newLabel)
	{
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
		ComodideCell sourceCell = (ComodideCell)cell.getSource();
		ComodideCell targetCell = (ComodideCell)cell.getTarget();
		
		// Domain can not be a datatype
		if(sourceCell instanceof DatatypeCell)
		{
			log.warn("[CoModIDE:LabelChangeHandler] Cannot create axiom with datatype as domain.");
			return null;
		}
		
		OWLProperty property = null;
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
