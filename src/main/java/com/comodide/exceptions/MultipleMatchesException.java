package com.comodide.exceptions;

public class MultipleMatchesException extends ComodideException
{
	/** Bookkeeping */
	private static final long serialVersionUID = 4174799684271995954L;
	
	public MultipleMatchesException(String message)
	{
		super(message);
	}
}
