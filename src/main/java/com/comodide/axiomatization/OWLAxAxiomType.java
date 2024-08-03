package com.comodide.axiomatization;

import java.util.Arrays;

public enum OWLAxAxiomType
{
	//@formatter:off
	GLOBAL_DOMAIN("Global domain",true,false),
	SCOPED_DOMAIN("Scoped domain",true,false),
	GLOBAL_RANGE("Global Range",true,true),
	SCOPED_RANGE("Scoped Range",true,true),
	EXISTENTIAL("Existential",true,true),
	INVERSE_EXISTENTIAL("Inverse Existential",true,false),
	FUNCTIONAL_ROLE("Functional Role",true,true),
	QUALIFIED_FUNCTIONAL_ROLE("Qualified Functional Role",true,true),
	SCOPED_FUNCTIONAL_ROLE("Scoped Functional Role",true,true),
	QUALIFIED_SCOPED_FUNCTIONAL_ROLE("Qualified Scoped Functional Role",true,true),
	INVERSE_FUNCTIONAL_ROLE("Inverse Functional Role",true,false),
	INVERSE_QUALIFIED_FUNCTIONAL_ROLE("Inverse Qualified Functional Role",true,false),
	INVERSE_SCOPED_FUNCTIONAL_ROLE("Inverse Scoped Functional Role",true,false),
	INVERSE_QUALIFIED_SCOPED_FUNCTIONAL_ROLE("Inverse Qualified Scoped Functional Role",true,false),
	STRUCTURAL_TAUTOLOGY("Structural Tautology",true,true);
	//@formatter:on

	private String axiomType;
	private boolean isObjectProperty;
	private boolean isDataProperty;

	OWLAxAxiomType(String axiomType, boolean ifObjectProperty, boolean isDataProperty)
	{
		this.axiomType = axiomType;
		this.isObjectProperty =ifObjectProperty;
		this.isDataProperty=isDataProperty;
	}

	public String getAxiomType()
	{
		return this.axiomType;
	}

	public boolean isObjectProperty() {
		return isObjectProperty;
	}

	public boolean isDataProperty() {
		return isDataProperty;
	}

	public static OWLAxAxiomType fromString(String owlAxAxiomType)
	{
		for (OWLAxAxiomType oaat : OWLAxAxiomType.values())
			if (oaat.getAxiomType().equalsIgnoreCase(owlAxAxiomType))
				return oaat;

		throw new IllegalArgumentException("The OWLAxAxiomType was not found: " + owlAxAxiomType);
	}

	public static OWLAxAxiomType[] getValidDataProperties()
	{
		return Arrays.stream(values())
				.filter(OWLAxAxiomType::isDataProperty)
				.toArray(OWLAxAxiomType[]::new);
	}
}