package com.comodide.rendering.sdont.viz;

import java.util.Map;
import java.util.Set;

import com.comodide.rendering.sdont.model.SDNode;

public interface NodeMaker<T>
{
	public Map<String, T> makeNodes(Set<SDNode> nodes);
	public T makeNode(SDNode node);
}
