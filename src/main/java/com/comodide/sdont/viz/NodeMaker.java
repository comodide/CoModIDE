package com.comodide.sdont.viz;

import java.util.Map;
import java.util.Set;

import com.comodide.sdont.model.SDNode;

public interface NodeMaker<T>
{
	public Map<String, T> makeNodes(Set<SDNode> nodes);
	public T makeNode(SDNode node);
}
