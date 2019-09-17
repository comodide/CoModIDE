package com.comodide.deprecated;

@SuppressWarnings("serial")
@Deprecated
public class UnparseableAxiomException extends Exception
{
	public UnparseableAxiomException()
	{
		super();
	}
	
	public UnparseableAxiomException(String msg)
	{
		super(msg);
	}
}
