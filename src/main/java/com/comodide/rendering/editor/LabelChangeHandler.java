package com.comodide.rendering.editor;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
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

	public LabelChangeHandler(OWLModelManager modelManager)
	{
		this.axiomManager = AxiomManager.getInstance(modelManager);
	}

	public Object handle(mxCell cell, String newLabel)
	{
		if (cell.isEdge())
		{
			return handleEdgeChange(cell, newLabel);
		}
		else
		{
			return handleNodeChange(cell, newLabel);
		}
	}

	public SDEdge handleEdgeChange(mxCell cell, String newLabel)
	{
		// Determine if the
		return null;
	}

	public SDNode handleNodeChange(mxCell cell, String newLabel)
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
			log.info("\t[CoModIDE:LabelChangeHandler] New datatype detected.");

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
