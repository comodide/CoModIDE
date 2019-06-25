package com.comodide.rendering.sdont.viz;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.comodide.rendering.editor.SDConstants;
import com.comodide.rendering.sdont.model.SDEdge;
import com.comodide.rendering.sdont.model.SDNode;
import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;

public class mxEdgeMaker
{	
	private mxGraph graph;
	private Object parent;
	
	public mxEdgeMaker(mxGraph graph)
	{
		this.graph = graph;
		this.parent = this.graph.getDefaultParent();
	}
	
	public Map<String, Object> makeEdges(Set<SDEdge> sdedges, Set<mxCell> vertices)
	{
		Map<String, Object> edges = new HashMap<>();
		
		this.graph.getModel().beginUpdate();
		try
		{
			for(SDEdge edge : sdedges)
			{
				mxCell source = null;
				mxCell target = null;
				
				// Iterate over all vertices to find the source cell to link from
				for (mxCell potentialSourceOrTarget: vertices) {
					if (potentialSourceOrTarget.getValue() instanceof SDNode) {
						SDNode potentialSourceOrTargetNode = (SDNode)potentialSourceOrTarget.getValue();
						if (potentialSourceOrTargetNode.equals(edge.getSource())) {
							source = potentialSourceOrTarget;
						}
						if (potentialSourceOrTargetNode.equals(edge.getTarget())) {
							target = potentialSourceOrTarget;
						}
						if (source != null && target !=null) {
							break;
						}
					}
				}
				
				if (source != null && target != null) {
					if(edge.isSubclass())
					{
						this.graph.insertEdge(parent, edge.toString(), edge, source, target, SDConstants.subclassEdgeStyle);
					}
					else
					{
						this.graph.insertEdge(parent, edge.toString(), edge, source, target, SDConstants.standardEdgeStyle);			
					}
				}
			}
		}
		finally
		{
			this.graph.getModel().endUpdate();
		}
		
		return edges;
	}
}
