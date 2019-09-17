package com.comodide.deprecated;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;

import com.comodide.editor.SchemaDiagram;
import com.comodide.editor.model.ClassCell;
import com.mxgraph.model.mxCell;

@Deprecated
public class mxEdgeMaker
{	
	private SchemaDiagram graph;
	
	public mxEdgeMaker(SchemaDiagram graph)
	{
		this.graph = graph;
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
					if (potentialSourceOrTarget.getValue() instanceof OWLEntity) {
						OWLEntity potentialSourceOrTargetEntity = (OWLEntity)potentialSourceOrTarget.getValue();
						if (potentialSourceOrTargetEntity.equals(edge.getSource().getOwlEntity())) {
							source = potentialSourceOrTarget;
						}
						if (potentialSourceOrTargetEntity.equals(edge.getTarget().getOwlEntity())) {
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
						this.graph.addSubClassEdge((ClassCell)source, (ClassCell)target);
						//this.graph.insertEdge(parent, edge.toString(), edge, source, target, SDConstants.subclassEdgeStyle);
					}
					else
					{
						this.graph.addPropertyEdge(edge.getOwlProperty(), (ClassCell)source, target);
						//this.graph.insertEdge(parent, edge.toString(), edge, source, target, SDConstants.standardEdgeStyle);			
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
