package com.comodide.rendering.sdont.viz;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.rendering.editor.SchemaDiagram;
import com.comodide.rendering.sdont.model.SDGraph;
import com.comodide.rendering.sdont.parsing.OntologyParser;

public class SDManager
{
	/** Logging */
	private static final Logger log = LoggerFactory.getLogger(SDManager.class);

	/** Model Manager */
	private OWLModelManager modelManager;

	/** SDont objects to manage */
	private OntologyParser ontologyParser;
	private SDGraph        graph;
	private SDMaker        maker;
	private SchemaDiagram  schemaDiagram;

	/** Empty Constructor */
	public SDManager()
	{

	}

	/** Useful Constructor */
	public SDManager(OWLModelManager modelManager)
	{
		this.modelManager = modelManager;
	}

	/** To be called initially */
	public SchemaDiagram initialSchemaDiagram()
	{
		this.ontologyParser = new OntologyParser(this.modelManager);
		this.graph = ontologyParser.parseOntology();
		this.maker = new SDMaker(graph, modelManager);
		this.schemaDiagram = maker.visualize();
		
		return schemaDiagram;
	}

	/** Called when changes in the underlying ontology are detected. */
	public void updateSchemaDiagramFromOntology(OWLOntologyChange change)
	{
		log.info("\t[CoModIDE:SDManager] Cascading Ontology Change");
		this.schemaDiagram.updateSchemaDiagramFromOntology(change);
	}
}
