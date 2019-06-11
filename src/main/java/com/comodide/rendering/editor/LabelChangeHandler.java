package com.comodide.rendering.editor;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.axiomatization.AxiomManager;
import com.comodide.rendering.sdont.model.SDEdge;
import com.comodide.rendering.sdont.model.SDNode;
import com.mxgraph.model.mxCell;

public class LabelChangeHandler
{
	/** Logging */
	private static final Logger log = LoggerFactory.getLogger(LabelChangeHandler.class);

	/** Singleton reference to AxiomManager. Handles OWL entity constructions */
	private AxiomManager axiomManager;

	/** explicit empty constructor */
	public LabelChangeHandler()
	{

	}

	public LabelChangeHandler(OWLModelManager modelManager, SchemaDiagram schemaDiagram)
	{
		this.axiomManager = AxiomManager.getInstance(modelManager, schemaDiagram);
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
		SDNode node = null;

		if (cell.getStyle().equals(SDConstants.classShape))
		{
			// Pass the label onto the AxiomManager
			// It will attempt to find if the class exists,
			// Otherwise, it will create a new class
			OWLEntity cls = axiomManager.handleClass(newLabel);
			// Wrap it in the intermediate layer (prevents ShortFormProvider reference)
			// and return.
			node = new SDNode(cls, false, cell.getGeometry().getX(), cell.getGeometry().getY());
		}
		else if (cell.getStyle().equals(SDConstants.datatypeShape))
		{
			// Add the new class to the ontology
			OWLDatatype datatype = this.axiomManager.findDatatype(newLabel);
			// Create an SDNode wrapper for the Axiom
			node = new SDNode((OWLEntity) datatype, true, cell.getGeometry().getX(), cell.getGeometry().getY());
		}
		else
		{
			// something something individuals?
		}

		return node;
	}
}
