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
	
	/** explicit empty constructor */
	public LabelChangeHandler()
	{

	}

	public LabelChangeHandler(OWLModelManager modelManager, SchemaDiagram schemaDiagram)
	{
		this.axiomManager = AxiomManager.getInstance(modelManager, schemaDiagram);
		this.modelManager = modelManager;
	}

	public Object handle(mxCell cell, String newLabel)
	{
		if (cell.isEdge())
		{
			return handleEdgeLabelChange(cell, newLabel);
		}
		else
		{
			return handleNodeLabelChange(cell, newLabel);
		}
	}

	public SDEdge handleEdgeLabelChange(mxCell cell, String newLabel)
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
		}
		else
		{
			property = this.axiomManager.handleObjectProperty(newLabel, domain, range);
		}
		
		edge = new SDEdge(source, target, false, property);

		return edge;
	}

	public SDNode handleNodeLabelChange(mxCell cell, String newLabel)
	{
		// Extract currentClass, if it is present
		OWLClass currentClass = null;
		Object value = cell.getValue();
		if(value instanceof SDNode)
		{
			currentClass = ((SDNode) value).getOwlEntity().asOWLClass();
		}
		
		SDNode node = null;

		Double newX = cell.getGeometry().getX();
		Double newY = cell.getGeometry().getY();
		
		OWLEntity entity = null;
		
		if (cell.getStyle().equals(SDConstants.classShape))
		{
			// Pass the label onto the AxiomManager
			// It will attempt to find if the class exists,
			// Otherwise, it will create a new class
			entity = axiomManager.handleClassChange(currentClass, newLabel);
			// Wrap it in the intermediate layer (prevents ShortFormProvider reference)
			// and return.
			node = new SDNode(entity, false, newX, newY);
		}
		else if (cell.getStyle().equals(SDConstants.datatypeShape))
		{
			// Add the new class to the ontology
			entity = this.axiomManager.findDatatype(newLabel);
			// Create an SDNode wrapper for the Axiom
			node = new SDNode(entity, true, newX, newY);
		}
		else
		{
			// something something individuals?
		}
		
		// Check which of the loaded ontologies hosts the
		// represented entity and update annotations in that ontology.
		for (OWLOntology ontology : modelManager.getOntologies())
		{
			if (ontology.containsEntityInSignature(entity.getIRI()))
			{
				PositioningOperations.updateXYCoordinateAnnotations(entity, ontology, newX, newY);
				break;
			}
		}

		return node;
	}
}
