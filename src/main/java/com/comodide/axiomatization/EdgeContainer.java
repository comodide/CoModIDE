package com.comodide.axiomatization;

import org.semanticweb.owlapi.model.OWLAxiom;

public class EdgeContainer
{
	private String id;
	private OWLAxiom axiom;
	private Object source;
	private Object target;
	private String style;

	/** Empty Constructor */
	public EdgeContainer()
	{

	}

	/** FIXME this edge container is insufficient for tracing both domain AND range axioms */
	public EdgeContainer(String id, OWLAxiom axiom, Object source, Object target, String style)
	{
		this.id = id;
		this.axiom = axiom;
		this.source = source;
		this.target = target;
		this.style = style;
	}

	public String toString()
	{
		// FIXME remove subclass in true condition
		return id.equals("subclass") ? "subclass" : id;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public OWLAxiom getAxiom()
	{
		return axiom;
	}

	public void setAxiom(OWLAxiom axiom)
	{
		this.axiom = axiom;
	}

	public Object getSource()
	{
		return source;
	}

	public void setSource(Object source)
	{
		this.source = source;
	}

	public Object getTarget()
	{
		return target;
	}

	public void setTarget(Object target)
	{
		this.target = target;
	}

	public String getStyle()
	{
		return style;
	}

	public void setStyle(String style)
	{
		this.style = style;
	}
}
