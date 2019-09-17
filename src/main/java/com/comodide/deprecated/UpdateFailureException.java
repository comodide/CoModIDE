package com.comodide.deprecated;

@Deprecated
public class UpdateFailureException extends Exception
{
    /** Bookkeeping */
    private static final long serialVersionUID = 1L;

    public UpdateFailureException(String message)
    {
        super(message);
    }
}
