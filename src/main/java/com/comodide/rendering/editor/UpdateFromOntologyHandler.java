package com.comodide.rendering.editor;

import org.apache.commons.lang3.tuple.Pair;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.axiomatization.AxiomManager;
import com.comodide.axiomatization.EdgeContainer;
import com.comodide.axiomatization.SimpleAxiomParser;
import com.comodide.rendering.PositioningOperations;
import com.comodide.rendering.sdont.model.SDNode;
import com.comodide.rendering.sdont.viz.mxEdgeMaker;
import com.comodide.rendering.sdont.viz.mxVertexMaker;
import com.mxgraph.model.mxGraphModel;

public class UpdateFromOntologyHandler
{
	/** Logging */
	private static final Logger log = LoggerFactory.getLogger(UpdateFromOntologyHandler.class);

	/** Used for updating the graph when handling changes */
	private SchemaDiagram schemaDiagram;
	private mxGraphModel  graphModel;

	/** Singleton reference to AxiomManager. Handles OWL entity constructions */
	private AxiomManager axiomManager;

	/** SimpleAxiomParser, currently shortcuts axiomManagemer */
	private SimpleAxiomParser simpleAxiomParser;

	/** Used for creating the styled mxcells for the graph */
	private mxVertexMaker vertexMaker;
	private mxEdgeMaker   edgeMaker;

	/** Empty Constructor */
	public UpdateFromOntologyHandler()
	{

	}

	public UpdateFromOntologyHandler(SchemaDiagram schemaDiagram, OWLModelManager modelManager)
	{
		this.schemaDiagram = schemaDiagram;
		this.graphModel = (mxGraphModel) schemaDiagram.getModel();

		this.axiomManager = AxiomManager.getInstance(modelManager);
		this.simpleAxiomParser = new SimpleAxiomParser(schemaDiagram);

		this.vertexMaker = new mxVertexMaker(schemaDiagram);
		this.edgeMaker = new mxEdgeMaker(schemaDiagram);
	}

	public void handle(OWLOntologyChange change)
	{
		log.info("[CoModIDE:UFOH] Handling Ontology Change.");
		/* Determine the type of OntologyChange */
		// Unpack the OntologyChange
		OWLOntology ontology = change.getOntology();
		OWLAxiom    axiom    = change.getAxiom();
		// Add or remove from graph? Might not be necessary.
		if (change.isAddAxiom())
		{
			// When a class is created it is declared. We extract the OWLEntity
			// From the declaration. It does not render declared properties.
			if (axiom.isOfType(AxiomType.DECLARATION))
			{
				handleClass(ontology, axiom);
			}
			// Axioms are parsed and rendered as an edge
			else if (axiom.isOfType(AxiomType.SUBCLASS_OF))
			{
				handleProperty(ontology, axiom);
			}
			else
			{
				log.warn("[CoModIDE:UFOH] Unsupported AddAxiom: " + axiom.getAxiomWithoutAnnotations().toString());
			}
		}
		else if (change.isRemoveAxiom())
		{
			// TODO isRemoveAxiom
			log.warn("[CoModIDE:UFOH] RemoveAxioms are currently unhandled.");
		}
		else
		{
			log.warn("[CoModIDE:UFOH] Unsupported change to the ontology.");
		}
	}

	public void handleClass(OWLOntology ontology, OWLAxiom axiom)
	{
		log.info("\t[CoModIDE:UFOH] Handling class change.");
		
		// The Cell representing the Class or Datatype
		Object cell = null;
		// Unpack data from Declaration
		OWLDeclarationAxiom declaration = (OWLDeclarationAxiom) axiom;
		OWLEntity           owlEntity   = declaration.getEntity();
		// Only handle Class or Datatype
		if (owlEntity.isOWLClass() || owlEntity.isOWLDatatype())
		{
			// Retrieve the opla-sd annotations for positions
			Pair<Double, Double> xyCoords = PositioningOperations.getXYCoordsForEntity(owlEntity, ontology);
			// Package the node
			SDNode node = new SDNode(owlEntity, owlEntity.isOWLDatatype(), xyCoords);
			// Create the node
			cell = vertexMaker.makeNode(node);
			// Update the SchemaDiagram
			graphModel.beginUpdate();
			try
			{
				schemaDiagram.addCell(cell);
			}
			finally
			{
				graphModel.endUpdate();
			}
		}
		else
		{
			// Do Nothing
			// We do not want to add properties as nodes.
		}
	}

	public void handleProperty(OWLOntology ontology, OWLAxiom axiom)
	{
		log.info("\t[CoModIDE:UFOH] Handling property change.");

		// Parse the axiom
		EdgeContainer edge = simpleAxiomParser.parseSimpleAxiom((OWLSubClassOfAxiom) axiom);
		// Unpack
		String id     = edge.getId();
		Object source = edge.getSource();
		Object target = edge.getTarget();
//		String style  = edge.getStyle();
		// Update the SchemaDiagram
		graphModel.beginUpdate();
		try
		{
			schemaDiagram.insertEdge(null, id, edge, source, target); //style);
		}
		finally
		{
			graphModel.endUpdate();
		}

	}
}
