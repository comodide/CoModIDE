package com.comodide.axiomatization;

public class MultipleMatchesException extends Exception
{
	/** Bookkeeping */
	private static final long serialVersionUID = 4174799684271995954L;
	
	public MultipleMatchesException(String message)
	{
		super(message);
	}
}
