package com.comodide.sdont.viz;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import com.comodide.sdont.model.SDEdge;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

public class mxEdgeMaker implements EdgeMaker<Object>
{
	private static final String subclassStyle = "subclassEdge";
	private static final String standardStyle = "standardEdge";
	
	private mxGraph graph;
	private Object parent;
	
	public mxEdgeMaker(mxGraph graph)
	{
		this.graph = graph;
		this.parent = this.graph.getDefaultParent();
		
		createStandardEdgeStyle();
		createSubclassEdgeStyle();
	}
	
	public Map<String, Object> makeEdges(Set<SDEdge> sdedges, Map<String, Object> vertices)
	{
		Map<String, Object> edges = new HashMap<>();
		
		this.graph.getModel().beginUpdate();
		try
		{
			for(SDEdge sdedge : sdedges)
			{
				edges.put(sdedge.getLabel(), makeEdge(sdedge, vertices));
			}
		}
		finally
		{
			this.graph.getModel().endUpdate();
		}
		
		return edges;
	}
	
	public Object makeEdge(SDEdge sdedge, Map<String, Object> vertices)
	{
		// Extract the data from the edge
		String id = sdedge.getLabel();
		String value = sdedge.getLabel();
		
		Object source = vertices.get(sdedge.getSource().getLabel());
		Object target = vertices.get(sdedge.getTarget().getLabel());
		
		// Create the mxEdge
		Object edge = null;
		if(sdedge.isSubclass())
		{
			edge = this.graph.insertEdge(parent, id, value, source, target, subclassStyle);
		}
		else
		{
			edge = this.graph.insertEdge(parent, id, value, source, target, standardStyle);			
		}
		return edge;
	}
	
	private void createStandardEdgeStyle()
	{
		mxStylesheet stylesheet = graph.getStylesheet();
		Hashtable<String, Object> style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_STROKECOLOR, "#000000");
		style.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_CLASSIC);
		stylesheet.putCellStyle(standardStyle, style);
	}
	
	private void createSubclassEdgeStyle()
	{
		mxStylesheet stylesheet = graph.getStylesheet();
		Hashtable<String, Object> style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_STROKECOLOR, "#000000");
		style.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_OPEN);
		stylesheet.putCellStyle(subclassStyle, style);
	}
}
