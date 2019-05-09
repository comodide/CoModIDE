package com.comodide.rendering.sdont.parsing;

import java.io.File;
import java.util.Set;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.comodide.rendering.sdont.model.SDEdge;
import com.comodide.rendering.sdont.model.SDGraph;
import com.comodide.rendering.sdont.model.SDNode;

public class OntologyParser
{
	private OWLConnector	connector;

	private AxiomParser		axiomParser;
	private ConceptParser	conceptParser;

	public OntologyParser(String filename) throws OWLOntologyCreationException
	{
		this.connector = new OWLConnector(filename);

		this.axiomParser = new AxiomParser(this.connector);
		this.conceptParser = new ConceptParser(this.connector);
	}

	public OntologyParser(File file) throws OWLOntologyCreationException
	{
		this.connector = new OWLConnector(file);

		this.axiomParser = new AxiomParser(this.connector);
		this.conceptParser = new ConceptParser(this.connector);
	}

	public OntologyParser(OWLModelManager manager)
	{
	    this.connector = new OWLConnector(manager);
	    
	    this.axiomParser = new AxiomParser(this.connector);
	    this.conceptParser = new ConceptParser(this.connector);
	}
	
	public SDGraph parseOntology()
	{
		Set<SDNode> nodeSet = this.conceptParser.provideNodes();
		Set<SDEdge> edgeSet = this.axiomParser.provideEdges(nodeSet);

		// TODO top level annotations
		SDGraph graph = new SDGraph(nodeSet, edgeSet);

		return graph;
	}
}
