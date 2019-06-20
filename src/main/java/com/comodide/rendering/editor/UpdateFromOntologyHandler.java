package com.comodide.rendering.editor;

import org.apache.commons.lang3.tuple.Pair;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.axiomatization.AxiomManager;
import com.comodide.axiomatization.EdgeContainer;
import com.comodide.rendering.PositioningOperations;
import com.comodide.rendering.sdont.model.SDEdge;
import com.comodide.rendering.sdont.model.SDNode;
import com.comodide.rendering.sdont.viz.mxVertexMaker;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGraphModel;

public class UpdateFromOntologyHandler
{
	/** Logging */
	private static final Logger log = LoggerFactory.getLogger(UpdateFromOntologyHandler.class);

	/** Used for updating the graph when handling changes */
	private SchemaDiagram schemaDiagram;
	private mxGraphModel  graphModel;

	/** Singleton reference to AxiomManager for parsing Axioms */
	private AxiomManager axiomManager;

	/** Used for creating the styled mxcells for the graph */
	private mxVertexMaker vertexMaker;

	/** Empty Constructor */
	public UpdateFromOntologyHandler()
	{

	}

	public UpdateFromOntologyHandler(SchemaDiagram schemaDiagram, OWLModelManager modelManager)
	{
		this.schemaDiagram = schemaDiagram;
		this.graphModel = (mxGraphModel) schemaDiagram.getModel();

		this.axiomManager = AxiomManager.getInstance(modelManager, schemaDiagram);

		this.vertexMaker = new mxVertexMaker(schemaDiagram);
	}

	public void handle(OWLOntologyChange change)
	{
		/* Determine the type of OntologyChange */
		// Unpack the OntologyChange
		OWLOntology ontology = change.getOntology();
		OWLAxiom    axiom    = change.getAxiom();
		// Add or remove from graph
		if (this.schemaDiagram.isLock())
		{
			// Do nothing
		}
		else if (change.isAddAxiom())
		{
			// When a class is created it is declared. We extract the OWLEntity
			// From the declaration. It does not render declared properties.
			if (axiom.isOfType(AxiomType.DECLARATION))
			{
				handleClass(ontology, axiom);
			}
			// General class axioms are parsed and rendered as an edge
			else if (axiom.isOfType(AxiomType.SUBCLASS_OF))
			{
				handleGeneralAxiom(ontology, axiom);
			}
			else if (axiom.isOfType(AxiomType.OBJECT_PROPERTY_DOMAIN))
			{
				handleObjectPropertyDomain(ontology, axiom);
			}
			else if (axiom.isOfType(AxiomType.OBJECT_PROPERTY_RANGE))
			{
				handleObjectPropertyRange(ontology, axiom);
			}
			else if (axiom.isOfType(AxiomType.DATA_PROPERTY_DOMAIN))
			{
				handleDataPropertyDomain(ontology, axiom);
			}
			else if (axiom.isOfType(AxiomType.DATA_PROPERTY_RANGE))
			{
				handleDataPropertyRange(ontology, axiom);
			}
			else
			{
				if (!axiom.isOfType(AxiomType.ANNOTATION_ASSERTION))
				{
					log.warn("[CoModIDE:UFOH] Unsupported AddAxiom: " + axiom.getAxiomWithoutAnnotations().toString());
				}
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
		// The Cell representing the Class or Datatype
		Object cell = null;
		// Unpack data from Declaration
		OWLDeclarationAxiom declaration = (OWLDeclarationAxiom) axiom;
		OWLEntity           owlEntity   = declaration.getEntity();
		// Only handle Class or Datatype
		if (owlEntity.isOWLClass() || owlEntity.isOWLDatatype())
		{
			log.info("[CoModIDE:UFOH] Handling class or datatype change.");

			// Retrieve the opla-sd annotations for positions
			Pair<Double, Double> xyCoords = PositioningOperations.getXYCoordsForEntity(owlEntity, ontology);
			// The given coordinates might come from the ontology, or they might have been created by PositioningAnnotations (if none were given
			// in the ontology at the outset). To guard against the latter case, persist them right away.
			PositioningOperations.updateXYCoordinateAnnotations(owlEntity, ontology, xyCoords.getLeft(), xyCoords.getRight());
			// Package the node
			SDNode node = new SDNode(owlEntity, owlEntity.isOWLDatatype(), xyCoords);
			// Create the node
			cell = vertexMaker.makeNode(node);
			// We do not want to add a duplicate cell at this time
			// By adding this "catch" we prevent the loopback "feature" of adding a Class
			// via CoModIDE propagating via this handler to add a duplicate cell
			// TODO support duplicate cells in a sane manner
			boolean isPresent = this.schemaDiagram.isLock();

			// Update the SchemaDiagram if the cell isn't present
			if (!isPresent)
			{
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
		}
		else
		{
			// Do Nothing
			// We do not want to add properties as nodes.
		}
	}

	public void handleGeneralAxiom(OWLOntology ontology, OWLAxiom axiom)
	{
		log.info("[CoModIDE:UFOH] Handling property via GCI.");
		// Parse the axiom
		EdgeContainer edge = axiomManager.parseSimpleAxiom((OWLSubClassOfAxiom) axiom);
		// Unpack
		String id     = edge.getId();
		Object source = edge.getSource();
		Object target = edge.getTarget();
		String style  = edge.getStyle();
		// Update the SchemaDiagram
		graphModel.beginUpdate();
		try
		{
			// If null is passed as parent, a convenience function in the chain
			// will call getDefaultParent()
			// TODO: the value added below should be a SDEdge, not EdgeContainer
			schemaDiagram.insertEdge(null, id, edge, source, target, style);
		}
		finally
		{
			graphModel.endUpdate();
		}

	}

	public void handleObjectPropertyDomain(OWLOntology ontology, OWLAxiom axiom)
	{
		log.info("[CoModIDE:UFOH] Handing Object Property Domain Restriction.");
		EdgeContainer edge = axiomManager.handleObjectPropertyDomain(ontology, axiom);
		// Null is returned if the edge cannot be handled (multiple ranges)
		// or if there is no range accompanying this domain.
		if (edge != null)
		{
			// Unpack
			String id     = edge.getId();
			Object source = edge.getSource();
			Object target = edge.getTarget();
			String style  = edge.getStyle();
			// Update the SchemaDiagram
			graphModel.beginUpdate();
			try
			{
				// If null is passed as parent, a convenience function in the chain
				// will call getDefaultParent()
				// TODO: the value added below should be a SDEdge, not EdgeContainer
				schemaDiagram.insertEdge(null, id, edge, source, target, style);
			}
			finally
			{
				graphModel.endUpdate();
			}
		}
	}

	public void handleObjectPropertyRange(OWLOntology ontology, OWLAxiom axiom)
	{
		log.info("[CoModIDE:UFOH] Handing Object Property Range Restriction.");
		EdgeContainer edge = axiomManager.handleObjectPropertyRange(ontology, axiom);
		// Null is returned if the edge cannot be handled (multiple ranges)
		// or if there is no range accompanying this domain.
		if (edge != null)
		{
			// Unpack
			String id     = edge.getId();
			Object source = edge.getSource();
			Object target = edge.getTarget();
			String style  = edge.getStyle();
			// Update the SchemaDiagram
			graphModel.beginUpdate();
			try
			{
				// If null is passed as parent, a convenience function in the chain
				// will call getDefaultParent()
				// TODO: the value added below should be a SDEdge, not EdgeContainer
				schemaDiagram.insertEdge(null, id, edge, source, target, style);
			}
			finally
			{
				graphModel.endUpdate();
			}
		}
	}

	public void handleDataPropertyDomain(OWLOntology ontology, OWLAxiom axiom)
	{
		log.info("[CoModIDE:UFOH] Handing Data Property Domain Restriction.");
		EdgeContainer edge = axiomManager.handleDataPropertyDomain(ontology, axiom);
		// Null is returned if the edge cannot be handled (multiple ranges)
		// or if there is no range accompanying this domain.
		if (edge != null)
		{
			// Unpack
			String id     = edge.getId();
			Object source = edge.getSource();
			Object target = edge.getTarget();
			String style  = edge.getStyle();
			// Update the SchemaDiagram
			graphModel.beginUpdate();
			try
			{
				// If null is passed as parent, a convenience function in the chain
				// will call getDefaultParent()
				// TODO: the value added below should be a SDEdge, not EdgeContainer
				schemaDiagram.insertEdge(null, id, edge, source, target, style);
			}
			finally
			{
				graphModel.endUpdate();
			}
		}
	}

	public void handleDataPropertyRange(OWLOntology ontology, OWLAxiom axiom)
	{
		log.info("[CoModIDE:UFOH] Handing Data Property Range Restriction.");
		EdgeContainer edge = axiomManager.handleDataPropertyRange(ontology, axiom);
		// Null is returned if the edge cannot be handled (multiple domains)
		// or if there is no range accompanying this domain.
		if (edge != null)
		{
			// Unpack
			String id     = edge.getId();
			Object source = edge.getSource();
			String style  = edge.getStyle();

			OWLDataPropertyRangeAxiom rangeAxiom   = (OWLDataPropertyRangeAxiom) axiom;
			OWLDataProperty           dataProperty = rangeAxiom.getProperty().asOWLDataProperty();
			OWLDatatype               range        = rangeAxiom.getRange().asOWLDatatype();
			
			// Note that we are fetching the positioning axioms from the data property,
			// which has identity (the datatype might not have)
			Pair<Double, Double> xyCoords = PositioningOperations.getXYCoordsForEntity(dataProperty, ontology);
			
			// The given coordinates might come from the ontology, or they might have been created by PositioningAnnotations (if none were given
			// in the ontology at the outset). To guard against the latter case, persist them right away.
			PositioningOperations.updateXYCoordinateAnnotations(dataProperty, ontology, xyCoords.getLeft(), xyCoords.getRight());
			
			// Now, create a new node and corresponding cell for this datatype
			SDNode rangeNode = new SDNode(range, true, xyCoords);
			Object target = vertexMaker.makeNode(rangeNode);
			
			// Only proceed if we actually have a source/domain cell we can use as SDNode
			if (source instanceof mxCell) {
				mxCell sourceCell = (mxCell)source;
				if (sourceCell.getValue() instanceof SDNode) {
					SDNode domainNode = (SDNode)sourceCell.getValue();
					SDEdge sdEdge = new SDEdge(domainNode, rangeNode, false, dataProperty);
					
					// TODO: Check if locking would be needed here like for classes?
					// Update the SchemaDiagram
					graphModel.beginUpdate();
					try
					{
						schemaDiagram.addCell(target);
						// If null is passed as parent, a convenience function in the chain
						// will call getDefaultParent()
						schemaDiagram.insertEdge(null, id, sdEdge, source, target, style);
					}
					finally
					{
						graphModel.endUpdate();
					}
				}
			}
		}
	}
}
