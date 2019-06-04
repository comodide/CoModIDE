package com.comodide.rendering.sdont.model;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

public class SDNode
{
	private static final ShortFormProvider shortFormProvider = new SimpleShortFormProvider();

	private String  label;
	private boolean isDatatype;
	private double positionX;
	private double positionY;

	private OWLEntity owlEntity;

	public SDNode(OWLEntity owlEntity, boolean isDatatype, double positionX, double positionY)
	{
		this.isDatatype = isDatatype;
		this.owlEntity = owlEntity;
		this.positionX = positionX;
		this.positionY = positionY;
	}

	public String toString()
	{
		return shortFormProvider.getShortForm(owlEntity);
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

	public double getPositionX() {
		return positionX;
	}

	public void setPositionX(double positionX) {
		this.positionX = positionX;
	}

	public double getPositionY() {
		return positionY;
	}

	public void setPositionY(double positionY) {
		this.positionY = positionY;
	}
}
