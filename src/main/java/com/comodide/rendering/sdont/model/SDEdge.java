package com.comodide.rendering.sdont.model;

import org.semanticweb.owlapi.model.OWLAxiom;

public class SDEdge
{
	private String label;
	private boolean isSubclass;
	
	private SDNode source;
	private SDNode target;
	
	private OWLAxiom owlAxiom;
	
	public SDEdge(String label, boolean isSubClass, SDNode source, SDNode target, OWLAxiom owlAxiom)
	{
		this.label = label;
		this.isSubclass = isSubClass;
		this.source = source;
		this.target = target;
		this.owlAxiom = owlAxiom;
	}
	
	public String toString()
	{
	    return this.label;
	}
	
	public OWLAxiom getOwlAxiom()
    {
        return owlAxiom;
    }

    public void setOwlAxiom(OWLAxiom owlAxiom)
    {
        this.owlAxiom = owlAxiom;
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
