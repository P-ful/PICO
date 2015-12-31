package com.pful.pico.db.querybuilder;

/**
 * Created by daeyeon on 12/28/15.
 */
public class QueryBuilderException
		extends Exception
{
	public QueryBuilderException()
	{
	}

	public QueryBuilderException(final String message)
	{
		super(message);
	}

	public QueryBuilderException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public QueryBuilderException(final Throwable cause)
	{
		super(cause);
	}

	public QueryBuilderException(final String message,
	                             final Throwable cause,
	                             final boolean enableSuppression,
	                             final boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
