package com.comodide.sdont.model;

public class SDNode
{
	private String label;
	private boolean isDatatype;
	
	public SDNode(String label, boolean isDatatype)
	{
		this.label = label;
		this.isDatatype = isDatatype;
	}

	public String getLabel()
	{
		return label;
	}

	public boolean isDatatype()
	{
		return isDatatype;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	public void setDatatype(boolean isDatatype)
	{
		this.isDatatype = isDatatype;
	}
}
