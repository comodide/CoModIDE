package com.comodide.rendering.sdont.parsing;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.semanticweb.owlapi.model.OWLOntology;

import com.comodide.rendering.PositioningOperations;
import com.comodide.rendering.sdont.model.SDNode;

public class ConceptParser
{
	private OWLConnector connector;

	public ConceptParser(OWLConnector connector)
	{
		this.connector = connector;
	}

	public Set<SDNode> provideNodes()
	{
		// Construct the nodeset for all concepts and datatypes
		Set<SDNode> nodes = new HashSet<>();
		// Get the nodes for all classes
		nodes.addAll(conceptNodes());
		// Get the nodes for all datatypes
		nodes.addAll(datatypeNodes());

		return nodes;
	}

	private Set<SDNode> conceptNodes()
	{
		OWLOntology ontology = this.connector.getOntology();
		// Construct nodeset for classes
		Set<SDNode> conceptNodes = new HashSet<>();
		ontology.getClassesInSignature().forEach(concept -> {
			Pair<Double,Double> xyCoords = PositioningOperations.getXYCoordsForEntity(concept, ontology);
			conceptNodes.add(new SDNode(concept, false, xyCoords.getLeft(), xyCoords.getRight()));
		});

		return conceptNodes;
	}

	private Set<SDNode> datatypeNodes()
	{
		OWLOntology ontology = this.connector.getOntology();
		// Construct nodeset for datatypes
		Set<SDNode> datatypeNodes = new HashSet<>();
		ontology.getDatatypesInSignature().forEach(datatype -> {
			Pair<Double,Double> xyCoords = PositioningOperations.getXYCoordsForEntity(datatype, ontology);
			datatypeNodes.add(new SDNode(datatype, true, xyCoords.getLeft(), xyCoords.getRight()));
		});

		return datatypeNodes;
	}
}
