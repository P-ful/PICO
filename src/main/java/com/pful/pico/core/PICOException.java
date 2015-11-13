package com.pful.pico.core;

/**
 * Created by daeyeon on 11/10/15.
 */
public class PICOException
		extends Exception
{
	/**
	 * An error-code raised in PICO
	 */
	private PICOErrorCode errorCode;

	public PICOException(final PICOErrorCode errorCode)
	{
		this.errorCode = errorCode;
	}

	public PICOException(final PICOErrorCode errorCode, final String message)
	{
		super(message);
		this.errorCode = errorCode;
	}

	public PICOException(final PICOErrorCode errorCode, final String message, final Throwable cause)
	{
		super(message, cause);
		this.errorCode = errorCode;
	}

	public PICOException(final PICOErrorCode errorCode, final Throwable cause)
	{
		super(cause);
		this.errorCode = errorCode;
	}

	public PICOException(final PICOErrorCode errorCode,
	                     final String message,
	                     final Throwable cause,
	                     final boolean enableSuppression,
	                     final boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.errorCode = errorCode;
	}

	public PICOErrorCode getErrorCode()
	{
		return errorCode;
	}
}
