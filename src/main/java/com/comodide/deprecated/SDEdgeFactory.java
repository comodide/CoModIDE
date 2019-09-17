package com.comodide.deprecated;

import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.semanticweb.owlapi.model.OWLProperty;

@Deprecated
public class SDEdgeFactory
{
	private Set<SDNode> nodes;
	
	public SDEdgeFactory(Set<SDNode> nodeSet)
	{
		this.nodes = nodeSet;
	}
	
	/**
	 * Generate an SDEdge from one node that is known by the SDEdgeFactory (e.g., listed in SDEdgeFactory.nodes) to another, using the nodes' labels.
	 * @param from
	 * @param to
	 * @param isSubClass
	 * @param property
	 * @return Edge or null (if no edge can be created)
	 */
	public SDEdge makeSDEdge(String from, String to, boolean isSubClass, OWLProperty property) {
		
		SDNode source = null;
		SDNode target = null;
		for (SDNode candidateNode: nodes) {
			if (candidateNode.toString().equals(from)) {
				source = candidateNode;
			}
			if (candidateNode.toString().equals(to)) {
				target = candidateNode;
			}
			if (source != null && target != null) {
				break;
			}
		}
		
		if (source != null && target != null) {
			return new SDEdge(source, target, isSubClass, property);
		}
		else {
			return null;
		}
	}
	
	/**
	 * Generate an SDEdge from one node that is known by the SDEdgeFactory (e.g., listed in SDEdgeFactory.nodes) to another, using the nodes' labels.
	 * If the property that this edge represents is a data property (i.e., there may be multiple candidate target nodes with the same labels, then the
	 * datatypeCoordinates (position of the target node) are used to disambiguate the candidates.  
	 * @param from
	 * @param to
	 * @param isSubClass
	 * @param property
	 * @param datatypeCoordinates
	 * @return Edge or null (if no edge can be created)
	 */
	public SDEdge makeSDEdgeForDataProperty(String from, String to, boolean isSubClass, OWLProperty property, Pair<Double,Double> datatypeCoordinates) {
		
		SDNode source = null;
		SDNode target = null;
		for (SDNode candidateNode: nodes) {
			if (candidateNode.toString().equals(from)) {
				source = candidateNode;
			}
			if (property.isDataPropertyExpression()) {
				if (candidateNode.toString().equals(to) && 
						candidateNode.getPositionX()==datatypeCoordinates.getLeft() &&
						candidateNode.getPositionY()==datatypeCoordinates.getRight()) {
					target = candidateNode;
				}
				
			}
			else {
				if (candidateNode.toString().equals(to)) {
					target = candidateNode;
				}
			}
			if (source != null && target != null) {
				break;
			}
		}
		
		if (source != null && target != null) {
			return new SDEdge(source, target, isSubClass, property);
		}
		else {
			return null;
		}
	}
}
