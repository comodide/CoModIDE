package com.comodide.rendering.sdont.viz;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.comodide.rendering.editor.SDConstants;
import com.comodide.rendering.sdont.model.SDNode;
import com.mxgraph.view.mxGraph;

public class mxVertexMaker implements NodeMaker<Object>
{
	private mxGraph				graph;
	private Object				parent;

	public mxVertexMaker(mxGraph graph)
	{
		this.graph = graph;
		this.parent = this.graph.getDefaultParent();
	}

	public Map<String, Object> makeNodes(Set<SDNode> nodes)
	{
		Map<String, Object> vertices = new HashMap<>();
		for(SDNode node : nodes)
		{
			vertices.put(node.toString(), makeNode(node));
		}
		return vertices;
	}

	public Object makeNode(SDNode node)
	{
		// Extract the data from the node
		String id = node.toString();

		// Create the vertex
		Object vertex = null;
		if(node.isDatatype())
		{
			vertex = this.graph.createVertex(parent, id, node, node.getPositionX(), node.getPositionY(), 75, 30, SDConstants.datatypeStyle);
		}
		else
		{
			vertex = this.graph.createVertex(parent, id, node, node.getPositionX(), node.getPositionY(), 75, 30, SDConstants.classStyle);
		}

		return vertex;
	}
}
