package com.comodide.sdont.viz;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import com.comodide.sdont.model.SDNode;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

public class mxVertexMaker implements NodeMaker<Object>
{
	private static final String	datatypeStyle	= "datatypeVertex";
	private static final String	standardStyle	= "standardVertex";

	private mxGraph				graph;
	private Object				parent;

	public mxVertexMaker(mxGraph graph)
	{
		this.graph = graph;
		this.parent = this.graph.getDefaultParent();
		
		createStandardVertexStyle();
		createDatatypeVertexStyle();
	}

	public Map<String, Object> makeNodes(Set<SDNode> nodes)
	{
		Map<String, Object> vertices = new HashMap<>();
		for(SDNode node : nodes)
		{
			vertices.put(node.getLabel(), makeNode(node));
		}
		return vertices;
	}

	public Object makeNode(SDNode node)
	{
		// Extract the data from the node
		String id = node.getLabel();
		String value = node.getLabel();

		// Create the vertex
		Object vertex = null;
		if(node.isDatatype())
		{
			vertex = this.graph.createVertex(parent, id, value, 0, 0, 75, 30, datatypeStyle);
		}
		else
		{
			vertex = this.graph.createVertex(parent, id, value, 0, 0, 75, 30, standardStyle);
		}

		return vertex;
	}

	private void createStandardVertexStyle()
	{
		mxStylesheet stylesheet = graph.getStylesheet();
		Hashtable<String, Object> style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
		style.put(mxConstants.STYLE_ROUNDED, "true");
		style.put(mxConstants.STYLE_FONTCOLOR, "#000000");
		style.put(mxConstants.STYLE_FILLCOLOR, "#FFCC00");
		stylesheet.putCellStyle(standardStyle, style);
	}
	
	private void createDatatypeVertexStyle()
	{
		mxStylesheet stylesheet = graph.getStylesheet();
		Hashtable<String, Object> style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE);
		style.put(mxConstants.STYLE_FONTCOLOR, "#000000");
		style.put(mxConstants.STYLE_FILLCOLOR, "#B0E0E0");
		stylesheet.putCellStyle(datatypeStyle, style);
	}
}
