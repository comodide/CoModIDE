package com.comodide.rendering.sdont.parsing;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDatatype;

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
		// Retrieve classes
		List<OWLClass> concepts = this.connector.retrieveClasses();
		// Construct nodeset for classes
		Set<SDNode> conceptNodes = new HashSet<>();
		concepts.forEach(concept -> {
			conceptNodes.add(new SDNode(concept, false));
		});

		return conceptNodes;
	}

	private Set<SDNode> datatypeNodes()
	{
		// Retrieve datatypes
		List<OWLDatatype> datatypes = this.connector.retrieveDatatypes();
		// Construct nodeset for datatypes
		Set<SDNode> datatypeNodes = new HashSet<>();
		datatypes.forEach(datatype -> {
			datatypeNodes.add(new SDNode(datatype, true));
		});

		return datatypeNodes;
	}
}
