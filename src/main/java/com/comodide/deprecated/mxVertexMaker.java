package com.comodide.deprecated;

import java.util.HashSet;
import java.util.Set;

import com.comodide.editor.SchemaDiagram;
import com.mxgraph.model.mxCell;

@Deprecated
public class mxVertexMaker
{
	private SchemaDiagram				graph;

	public mxVertexMaker(SchemaDiagram graph)
	{
		this.graph = graph;
	}

	public Set<mxCell> makeVertexCells(Set<SDNode> nodes)
	{
		this.graph.getModel().beginUpdate();
		Set<mxCell> cells = new HashSet<mxCell>();
		for(SDNode node : nodes)
		{
			cells.add(makeNode(node));
		}
		this.graph.getModel().endUpdate();
		return cells;
	}

	public mxCell makeNode(SDNode node)
	{
		// Extract the data from the node
		//String id = node.toString();

		// Create the vertex
		mxCell vertex = null;
		if(node.isDatatype())
		{
			vertex = this.graph.addDatatype(node.getOwlEntity(), node.getPositionX(), node.getPositionY());
			//vertex = (mxCell)this.graph.createVertex(parent, id, node, node.getPositionX(), node.getPositionY(), 75, 30, SDConstants.datatypeStyle);
		}
		else
		{
			vertex = this.graph.addClass(node.getOwlEntity(), node.getPositionX(), node.getPositionY());
			//vertex = (mxCell)this.graph.createVertex(parent, id, node, node.getPositionX(), node.getPositionY(), 75, 30, SDConstants.classStyle);
		}

		return vertex;
	}
}
