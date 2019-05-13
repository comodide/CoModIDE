package com.comodide.rendering.sdont.viz;

import org.protege.editor.owl.model.OWLModelManager;

import com.comodide.rendering.editor.SchemaDiagram;
import com.comodide.rendering.sdont.model.SDGraph;
import com.comodide.rendering.sdont.parsing.OntologyParser;
import com.mxgraph.swing.mxGraphComponent;

public class SDManager
{
    /** Model Manager */
    private OWLModelManager modelManager;

    /** sdont objects to manager */
    private OntologyParser ontologyParser;
    private SDGraph graph;
    private SDMaker maker;
    
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
        this.maker = new SDMaker(graph);

        return maker.visualize();
    }

    public mxGraphComponent update()
    {
        // TODO make changes to graph
        return null;
    }
    
    public SchemaDiagram updateNaive()
    {
        return initialSchemaDiagram();
    }
}
