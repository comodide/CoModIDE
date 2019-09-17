package com.comodide.deprecated;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLOntology;

import com.comodide.rendering.PositioningOperations;

@Deprecated
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
			PositioningOperations.updateXYCoordinateAnnotations(concept, ontology, xyCoords.getLeft(), xyCoords.getRight());
			conceptNodes.add(new SDNode(concept, false, xyCoords.getLeft(), xyCoords.getRight()));
		});
		return conceptNodes;
	}

	private Set<SDNode> datatypeNodes()
	{
		OWLOntology ontology = this.connector.getOntology();
		// Construct nodeset for datatypes
		Set<SDNode> datatypeNodes = new HashSet<>();

		// Create nodes for datatypes linked from data properties (which use positioning annotations on the data property)
		ontology.getDataPropertiesInSignature().forEach(dataProperty -> {
			Pair<Double,Double> xyCoords = PositioningOperations.getXYCoordsForEntity(dataProperty, ontology);
			PositioningOperations.updateXYCoordinateAnnotations(dataProperty, ontology, xyCoords.getLeft(), xyCoords.getRight());
			for (OWLDataPropertyRangeAxiom rangeAxiom: ontology.getDataPropertyRangeAxioms(dataProperty)) {
				Set<OWLDatatype> rangeTypes = rangeAxiom.getDatatypesInSignature();
				for (OWLDatatype range: rangeTypes) {
					datatypeNodes.add(new SDNode(range, true, xyCoords.getLeft(), xyCoords.getRight()));
				}
			}
		});
		
		return datatypeNodes;
	}
}
