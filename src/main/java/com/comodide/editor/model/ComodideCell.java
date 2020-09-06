package com.comodide.editor.model;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;

public abstract class ComodideCell extends mxCell
{

	private static final long              serialVersionUID  = 8089893998863898138L;
	private static final ShortFormProvider shortFormProvider = new SimpleShortFormProvider();
	private OWLEntity                      entity;

	public ComodideCell(OWLEntity owlEntity)
	{
		setEntity(owlEntity);
		this.geometry = new mxGeometry();
	}

	public void setEntity(OWLEntity entity)
	{
		this.entity = entity;
		this.value = shortFormProvider.getShortForm(entity);
	}

	public OWLEntity getEntity()
	{
		return this.entity;
	}

	public boolean isNamed()
	{
		return !this.getEntity().getIRI().toString().equalsIgnoreCase(this.defaultIRI().toString());
	}

	public boolean isModule()
	{
		return false;
	}
	
	public abstract IRI defaultIRI();
}
