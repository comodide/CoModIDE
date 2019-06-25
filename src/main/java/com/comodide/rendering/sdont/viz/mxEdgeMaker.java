package com.comodide.rendering.sdont.viz;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.rendering.PositioningOperations;
import com.comodide.rendering.editor.SDConstants;
import com.comodide.rendering.sdont.model.SDEdge;
import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;

import ch.qos.logback.core.joran.conditional.IfAction;

public class mxEdgeMaker
{
	
	private static final Logger log = LoggerFactory.getLogger(mxEdgeMaker.class);
	
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
			// Note that this code is rather inefficient; but I have prioritized clarity of reading over speed, and anyway
			// the number of nodes won't be that large I hope. /Karl
			for(SDEdge edge : sdedges)
			{
				log.warn("Attempting to make edge: " + edge.toString());
				mxCell source = null;
				mxCell target = null;
				
				// Iterate over all vertices to find the source cell to link from
				for (mxCell potentialSource: vertices) {
					if (potentialSource.getId().equals(edge.getSource().toString())) {
						source = potentialSource;
						break;
					}
				}
				
				// If this is an object property, simply find the target cell in the same way as above, based on label identifiers
				if (edge.isSubclass() || edge.getOwlProperty().isObjectPropertyExpression()) {
					for (mxCell potentialTarget: vertices) {
						if (potentialTarget.getId().equals(edge.getTarget().toString())) {
							target = potentialTarget;
							break;
						}
					}
				}
				
				// If this is a data property there may be multiple target cells with the same identifier on the canvas; 
				// so we also need to ensure that the X and Y positions match
				else if (edge.getOwlProperty().isDataPropertyExpression()) {
					for (mxCell potentialTarget: vertices) {
						if (potentialTarget.getId().equals(edge.getTarget().toString()) &&
								potentialTarget.getGeometry().getX()==edge.getTarget().getPositionX() &&
								potentialTarget.getGeometry().getY()==edge.getTarget().getPositionY()) {
							target = potentialTarget;
							break;
						}
					}
				}
				
				if (source == null) {
					log.error("Source for edge is null: " + edge.toString());
				}
				if (target == null) {
					log.error("Target for edge is null: " + edge.toString());
				}
				
				if (source != null && target != null) {
					log.warn(String.format("Inserting edge %s between %s and %s", edge.toString(), source.toString(), target.toString()));
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
