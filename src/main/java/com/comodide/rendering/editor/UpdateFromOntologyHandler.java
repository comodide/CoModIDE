package com.comodide.rendering.editor;

import org.apache.commons.lang3.tuple.Pair;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.axiomatization.AxiomManager;
import com.comodide.rendering.PositioningOperations;
import com.comodide.rendering.sdont.model.SDNode;
import com.comodide.rendering.sdont.viz.mxEdgeMaker;
import com.comodide.rendering.sdont.viz.mxVertexMaker;

public class UpdateFromOntologyHandler
{
	/** Logging */
	private static final Logger log = LoggerFactory.getLogger(UpdateFromOntologyHandler.class);

	/** Singleton reference to AxiomManager. Handles OWL entity constructions */
	private AxiomManager axiomManager;

	/** Used for creating the styled mxcells for the graph */
	private mxVertexMaker vertexMaker;
	private mxEdgeMaker   edgeMaker;

	/** Empty Constructor */
	public UpdateFromOntologyHandler()
	{

	}

	public UpdateFromOntologyHandler(SchemaDiagram schemaDiagram, OWLModelManager modelManager)
	{
		this.axiomManager = AxiomManager.getInstance(modelManager);
		this.vertexMaker = new mxVertexMaker(schemaDiagram);
		this.edgeMaker = new mxEdgeMaker(schemaDiagram);
	}

	public Object handle(OWLOntologyChange change)
	{
		// Unpack the OntologyChange
		OWLOntology ontology = change.getOntology();
		OWLAxiom    axiom    = change.getAxiom();
		// The cell to return
		Object cell = null;
		// Add or remove from graph? Might not be necessary.
		if (change.isAddAxiom())
		{
			if (axiom.isOfType(AxiomType.DECLARATION))
			{
				// Unpack data from Declaration
				OWLDeclarationAxiom declaration = (OWLDeclarationAxiom) axiom;
				OWLEntity           owlEntity   = declaration.getEntity();

				if (owlEntity.isOWLClass() || owlEntity.isOWLDatatype())
				{
					// Retrieve the opla-sd annotations for positions
					Pair<Double, Double> xyCoords = PositioningOperations.getXYCoordsForEntity(owlEntity, ontology);
					// Package the node
					SDNode node = new SDNode(owlEntity, owlEntity.isOWLDatatype(), xyCoords);
					// Create the node
					cell = vertexMaker.makeNode(node);
				}
				else
				{
					log.info("\t\tNot adding as node: " + owlEntity.toString());
				}
			}
			else if (axiom.isOfType(AxiomType.SUBCLASS_OF))
			{

			}
			else
			{
				log.info(axiom.toString());
			}
		}
		else
		{
			// TODO isRemoveAxiom
		}

		return cell;
	}
}
