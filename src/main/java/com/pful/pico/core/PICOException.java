package com.pful.pico.core;

public class PICOException
		extends Exception
{
	private int errorCode;

	public PICOException(final int errorCode)
	{
		this.errorCode = errorCode;
	}
}
