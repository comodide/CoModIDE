package com.comodide.deprecated;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLProperty;

/**
 * 
 * @author Cogs
 *
 * This object is used for passing source to target via role edges from a Parser to a Visualizer
 *
 */
@Deprecated
public class Triple
{
	private String fr;
	private String to;
	private String pr;

	private boolean isSubClass;
	
	private OWLProperty owlProperty;
	private OWLAxiom owlAxiom;
	
	public Triple(String fr, String to, String pr, OWLProperty owlProperty)
	{
		this.fr = fr;
		this.to = to;
		this.pr = pr;
		this.isSubClass = false;
		this.owlAxiom = null;
		this.owlProperty = owlProperty;
	}

	public Triple(String fr, String to, OWLAxiom owlAxiom)
	{
	    this(fr, to, "SUBCLASS", null);
		this.isSubClass = true;
		this.owlAxiom = owlAxiom;
	}
	
	public OWLProperty wraps()
	{
	    return this.getOwlProperty();
	}
	
	public OWLAxiom represents()
	{
		return this.owlAxiom;
	}
	
    public OWLProperty getOwlProperty()
	{
		return owlProperty;
	}

	public void setOwlProperty(OWLProperty owlProperty)
	{
		this.owlProperty = owlProperty;
	}
	
	public boolean isSubClass()
	{
		return isSubClass;
	}

	public void setSubClass(boolean isSubClass)
	{
		this.isSubClass = isSubClass;
	}

	public String getFr()
	{
		return fr;
	}
	
	public void setFr(String fr)
	{
		this.fr = fr;
	}
	
	public String getTo()
	{
		return to;
	}
	
	public void setTo(String to)
	{
		this.to = to;
	}
	
	public String getPr()
	{
		return pr;
	}
	
	public void setPr(String pr)
	{
		this.pr = pr;
	}
}
