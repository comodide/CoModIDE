package com.comodide.rendering.sdont.viz;

import java.util.List;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;

import com.comodide.rendering.editor.SchemaDiagram;
import com.comodide.rendering.sdont.model.SDGraph;
import com.comodide.rendering.sdont.parsing.OntologyParser;

public class ComodideEditorManager implements OWLOntologyChangeListener
{
	/** Model Manager */
	private OWLModelManager modelManager;

	/** SDont objects to manage */
	private OntologyParser ontologyParser;
	private SDGraph        graph;
	private SDMaker        maker;
	private SchemaDiagram  schemaDiagram;

	public SchemaDiagram getSchemaDiagram() {
		return schemaDiagram;
	}

	/** Useful Constructor */
	public ComodideEditorManager(OWLModelManager modelManager)
	{
		this.modelManager = modelManager;
		
		/* Register as listener to detect changes in the ontology */
		this.modelManager.addOntologyChangeListener(this);
		
		this.ontologyParser = new OntologyParser(this.modelManager);
		this.graph = ontologyParser.parseOntology();
		this.maker = new SDMaker(graph, modelManager);
		this.schemaDiagram = maker.visualize();
	}

	/** Called when changes in the underlying ontology are detected. */
	public void updateSchemaDiagramFromOntologyChange(OWLOntologyChange change)
	{
		this.schemaDiagram.updateSchemaDiagramFromOntology(change);
	}
	

	@Override
	public void ontologiesChanged(List<? extends OWLOntologyChange> changes) throws OWLException {
		changes.forEach(change -> {if(change.isAxiomChange()) updateSchemaDiagramFromOntologyChange(change);});
	}
}
