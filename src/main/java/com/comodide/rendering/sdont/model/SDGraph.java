package com.comodide.rendering.sdont.model;

import java.util.Set;

@Deprecated
public class SDGraph
{
	private Set<SDNode> nodeSet;
	private Set<SDEdge> edgeSet;
	
	public SDGraph(Set<SDNode> nodeSet, Set<SDEdge> edgeSet)
	{
		this.nodeSet = nodeSet;
		this.edgeSet = edgeSet;
	}

	public Set<SDNode> getNodeSet()
	{
		return nodeSet;
	}

	public Set<SDEdge> getEdgeSet()
	{
		return edgeSet;
	}

	public void setNodeSet(Set<SDNode> nodeSet)
	{
		this.nodeSet = nodeSet;
	}

	public void setEdgeSet(Set<SDEdge> edgeSet)
	{
		this.edgeSet = edgeSet;
	}
}
