package com.comodide.rendering.sdont.viz;

import java.util.HashSet;
import java.util.Set;

import com.comodide.rendering.editor.SDConstants;
import com.comodide.rendering.sdont.model.SDNode;
import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;

public class mxVertexMaker
{
	private mxGraph				graph;
	private Object				parent;

	public mxVertexMaker(mxGraph graph)
	{
		this.graph = graph;
		this.parent = this.graph.getDefaultParent();
	}

	public Set<mxCell> makeVertexCells(Set<SDNode> nodes)
	{
		Set<mxCell> cells = new HashSet<mxCell>();
		for(SDNode node : nodes)
		{
			cells.add(makeNode(node));
		}
		return cells;
	}

	public mxCell makeNode(SDNode node)
	{
		// Extract the data from the node
		String id = node.toString();

		// Create the vertex
		mxCell vertex = null;
		if(node.isDatatype())
		{
			vertex = (mxCell)this.graph.createVertex(parent, id, node, node.getPositionX(), node.getPositionY(), 75, 30, SDConstants.datatypeStyle);
		}
		else
		{
			vertex = (mxCell)this.graph.createVertex(parent, id, node, node.getPositionX(), node.getPositionY(), 75, 30, SDConstants.classStyle);
		}

		return vertex;
	}
}
