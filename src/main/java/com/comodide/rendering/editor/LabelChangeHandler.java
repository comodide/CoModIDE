package com.comodide.rendering.editor;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.axiomatization.AxiomManager;
import com.comodide.rendering.PositioningOperations;
import com.comodide.rendering.sdont.model.SDEdge;
import com.comodide.rendering.sdont.model.SDNode;
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

	public Object handle(mxCell cell, String newLabel)
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

	private SDEdge handleEdgeLabelChange(mxCell cell, String newLabel)
	{
		// Unpack useful things
		SDNode source = (SDNode) cell.getSource().getValue();
		SDNode target = (SDNode) cell.getTarget().getValue();
		
		// The edge to return
		SDEdge edge = null;
		
		// Domain can not be a datatype
		if(source.isDatatype())
		{
			log.warn("[CoModIDE:LabelChangeHandler] Cannot create axiom with datatype as domain.");
			return null;
		}
		
		OWLProperty property = null;
		OWLEntity domain = source.getOwlEntity();
		OWLEntity range = target.getOwlEntity();
		
		// Create the property
		if(target.isDatatype())
		{
			property = this.axiomManager.handleDataProperty(newLabel, domain, range);
			// Update positioning annotations for target which are stored on the data property
			// (since the target datatype does not have own identity)
			for (OWLOntology ontology : modelManager.getOntologies())
			{
				if (ontology.containsEntityInSignature(property.getIRI()))
				{
					PositioningOperations.updateXYCoordinateAnnotations(property, ontology, target.getPositionX(), target.getPositionY());
				}
			}
			
		}
		else
		{
			property = this.axiomManager.handleObjectProperty(newLabel, domain, range);
		}
		
		edge = new SDEdge(source, target, false, property);

		return edge;
	}

	private SDNode handleNodeLabelChange(mxCell cell, String newLabel)
	{
		SDNode node = null;

		Double newX = cell.getGeometry().getX();
		Double newY = cell.getGeometry().getY();
		
		if (cell.getStyle().equals(SDConstants.classStyle))
		{
			// Extract currentClass, if it is present
			OWLClass currentClass = null;
			if(cell.getValue() instanceof SDNode)
			{
				currentClass = ((SDNode) cell.getValue()).getOwlEntity().asOWLClass();
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
			
			// Wrap it in the intermediate layer (prevents ShortFormProvider reference) and return.
			node = new SDNode(classEntity, false, newX, newY);
		}
		else if (cell.getStyle().equals(SDConstants.datatypeStyle))
		{
			// Add the new class to the ontology
			OWLEntity datatypeEntity = this.axiomManager.findDatatype(newLabel);
			// Create an SDNode wrapper for the Axiom
			node = new SDNode(datatypeEntity, true, newX, newY);
		}

		return node;
	}
}
