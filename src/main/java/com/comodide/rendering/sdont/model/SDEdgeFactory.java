package com.comodide.rendering.sdont.model;

import java.util.HashMap;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;

import com.comodide.rendering.sdont.parsing.Triple;

public class SDEdgeFactory
{
	private HashMap<String, SDNode> nodeMap;
	
	public SDEdgeFactory(Set<SDNode> nodeSet)
	{
		this.nodeMap = new HashMap<>();
		nodeSet.forEach(node -> {
			this.nodeMap.put(node.getLabel(), node);
		});
	}
	
	public SDEdge makeSDEdge(Triple t)
	{
		String label = t.getPr();
		String from = t.getFr();
		String to = t.getTo();
		
		boolean isSubClass = t.isSubClass();
		
		SDNode source = nodeMap.get(from);
		SDNode target = nodeMap.get(to);

		OWLAxiom owlAxiom = t.wraps();
		
		SDEdge edge = new SDEdge(label, isSubClass, source, target, owlAxiom);

		return edge;
	}
}
