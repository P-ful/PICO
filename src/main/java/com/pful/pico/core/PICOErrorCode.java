package com.pful.pico.core;

/**
 * Created by daeyeon on 11/10/15.
 */
public enum PICOErrorCode
{
	Success(0);

	private int errorCode;

	PICOErrorCode(final int errorCode)
	{
		this.errorCode = errorCode;
	}

	public int toInt()
	{
		return errorCode;
	}
}
