package com.comodide.rendering.sdont.viz;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.comodide.rendering.editor.SDConstants;
import com.comodide.rendering.sdont.model.SDEdge;
import com.mxgraph.view.mxGraph;

public class mxEdgeMaker implements EdgeMaker<Object>
{
	
	private mxGraph graph;
	private Object parent;
	
	public mxEdgeMaker(mxGraph graph)
	{
		this.graph = graph;
		this.parent = this.graph.getDefaultParent();
	}
	
	public Map<String, Object> makeEdges(Set<SDEdge> sdedges, Map<String, Object> vertices)
	{
		Map<String, Object> edges = new HashMap<>();
		
		this.graph.getModel().beginUpdate();
		try
		{
			for(SDEdge sdedge : sdedges)
			{
				edges.put(sdedge.toString(), makeEdge(sdedge, vertices));
			}
		}
		finally
		{
			this.graph.getModel().endUpdate();
		}
		
		return edges;
	}
	
	public Object makeEdge(SDEdge sdEdge, Map<String, Object> vertices)
	{
		// Extract the data from the edge
		String id = sdEdge.toString();
		
		Object source = vertices.get(sdEdge.getSource().toString());
		Object target = vertices.get(sdEdge.getTarget().toString());
		
		// Create the mxEdge
		Object edge = null;
		if(sdEdge.isSubclass())
		{
			edge = this.graph.insertEdge(parent, id, sdEdge, source, target, SDConstants.subclassEdgeStyle);
		}
		else
		{
			edge = this.graph.insertEdge(parent, id, sdEdge, source, target, SDConstants.standardEdgeStyle);			
		}
		return edge;
	}
}
