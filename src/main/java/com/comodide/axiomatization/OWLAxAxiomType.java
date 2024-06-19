package com.comodide.axiomatization;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public enum OWLAxAxiomType
{
	//@formatter:off
	GLOBAL_DOMAIN("Global domain"),
	SCOPED_DOMAIN("Scoped domain"),
	GLOBAL_RANGE("Global Range"),
	SCOPED_RANGE("Scoped Range"),
	EXISTENTIAL("Existential"),
	INVERSE_EXISTENTIAL("Inverse Existential"),
	FUNCTIONAL_ROLE("Functional Role"),
	QUALIFIED_FUNCTIONAL_ROLE("Qualified Functional Role"),
	SCOPED_FUNCTIONAL_ROLE("Scoped Functional Role"),
	QUALIFIED_SCOPED_FUNCTIONAL_ROLE("Qualified Scoped Functional Role"),
	INVERSE_FUNCTIONAL_ROLE("Inverse Functional Role"),
	INVERSE_QUALIFIED_FUNCTIONAL_ROLE("Inverse Qualified Functional Role"),
	INVERSE_SCOPED_FUNCTIONAL_ROLE("Inverse Scoped Functional Role"),
	INVERSE_QUALIFIED_SCOPED_FUNCTIONAL_ROLE("Inverse Qualified Scoped Functional Role"),
	STRUCTURAL_TAUTOLOGY("Structural Tautology");
	//@formatter:on
	
	private String axiomType;

	private static Map<String, OWLAxAxiomType> axiomTypeMap = new HashMap<>();
	static {
		for (OWLAxAxiomType axiomType : values())
			axiomTypeMap.put(axiomType.axiomType, axiomType);
	}

	OWLAxAxiomType(String axiomType)
	{
		this.axiomType = axiomType;
	}

	public String getAxiomType()
	{
		return this.axiomType;
	}

	public static OWLAxAxiomType fromString(@Nonnull String owlAxAxiomType)
	{
		OWLAxAxiomType correspondingAxiomType = axiomTypeMap.get(owlAxAxiomType);
		if (correspondingAxiomType == null)
			throw new IllegalArgumentException("The OWLAxAxiomType was not found: " + owlAxAxiomType);
		return correspondingAxiomType;
	}
}
