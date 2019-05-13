package com.comodide.rendering.sdont.model;

import org.semanticweb.owlapi.model.OWLEntity;

public class SDNode
{
	private String label;
	private boolean isDatatype;
	
	private OWLEntity owlEntity;
	
	public SDNode(String label, boolean isDatatype, OWLEntity owlEntity)
	{
		this.label = label;
		this.isDatatype = isDatatype;
		this.owlEntity = owlEntity;
	}

	public String toString()
	{
	    return this.label;
	}
	
	public OWLEntity getOwlEntity()
    {
        return owlEntity;
    }

    public void setOwlEntity(OWLEntity owlEntity)
    {
        this.owlEntity = owlEntity;
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
