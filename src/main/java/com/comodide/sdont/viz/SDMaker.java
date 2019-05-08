package com.comodide.sdont.viz;

import java.util.Map;
import java.util.Set;

import com.comodide.sdont.model.SDEdge;
import com.comodide.sdont.model.SDGraph;
import com.comodide.sdont.model.SDNode;
import com.comodide.sdont.ui.SDontViewFrame;
import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

public class SDMaker
{
	private SDGraph				sdGraph;
	private mxGraph				mxGraph;
	private NodeMaker<Object>	vertexMaker;
	private EdgeMaker<Object>	edgeMaker;

	private Map<String, Object>	vertices;

	public SDMaker(SDGraph sdGraph)
	{
		this.sdGraph = sdGraph;
		// Create an mxgraph
		this.mxGraph = new mxGraph();
		// Create the makers for the cells
		this.vertexMaker = new mxVertexMaker(mxGraph);
		this.edgeMaker = new mxEdgeMaker(mxGraph);
	}

	public void visualize()
	{
		// Make the graph
		makeGraph();
		// set the layout for the graph
		executeLayout();
		// Create and display the visualization
		mxGraphComponent graphComponent = new mxGraphComponent(this.mxGraph);
		@SuppressWarnings("unused")
		SDontViewFrame frame = new SDontViewFrame(graphComponent);
	}

	private void makeGraph()
	{
		// Get default parent
		Object parent = this.mxGraph.getDefaultParent();
		// Make all vertices
		makeVertices(parent);
		// Make all edges
		makeEdges(parent);
	}

	private void executeLayout()
	{
		Object parent = mxGraph.getDefaultParent();
		mxFastOrganicLayout fol = new mxFastOrganicLayout(this.mxGraph);
		fol.execute(parent);
	}
	
	private void makeVertices(Object parent)
	{
		// Get all nodes from sdgraph
		Set<SDNode> nodeset = this.sdGraph.getNodeSet();
		// Create the visualizations of the nodes
		this.vertices = this.vertexMaker.makeNodes(nodeset);
		// Add them all to the graph
		this.mxGraph.getModel().beginUpdate();
		try
		{
			for(Object vertex : this.vertices.values())
			{
				this.mxGraph.addCell(vertex);
			}
		}
		finally
		{
			this.mxGraph.getModel().endUpdate();
		}
	}

	/**
	 * Unfortunately, due to limitations of the implementations of creating and
	 * adding an edge, the source and target of an edge are only set when they
	 * are added to the model. This breaks our implementation of making the edge
	 * and adding the edge to the model, separately. Thus, in the makeEdges call
	 * above, we use insertEdge instead of createEdge, thus removing the need
	 * for an analogous (to makeNodes) update try/catch block here.
	 * 
	 * @param parent
	 */
	private void makeEdges(Object parent)
	{
		// Get all edges from the sdgraph
		Set<SDEdge> edgeset = this.sdGraph.getEdgeSet();
		// Create the visualizations of the edges
		this.edgeMaker.makeEdges(edgeset, this.vertices);
	}
}
