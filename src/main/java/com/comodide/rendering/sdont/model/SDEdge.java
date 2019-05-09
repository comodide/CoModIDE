package com.comodide.rendering.sdont.model;

public class SDEdge
{
	private String label;
	private boolean isSubclass;
	
	private SDNode source;
	private SDNode target;
	
	public SDEdge(String label, boolean isSubClass, SDNode source, SDNode target)
	{
		this.label = label;
		this.isSubclass = isSubClass;
		this.source = source;
		this.target = target;
	}

	public String getLabel()
	{
		return label;
	}

	public boolean isSubclass()
	{
		return isSubclass;
	}

	public SDNode getSource()
	{
		return source;
	}

	public SDNode getTarget()
	{
		return target;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	public void setSubclass(boolean isSubclass)
	{
		this.isSubclass = isSubclass;
	}

	public void setSource(SDNode source)
	{
		this.source = source;
	}

	public void setTarget(SDNode target)
	{
		this.target = target;
	}
}
