package com.pful.pico.core;

public enum PICOErrorCode
{
	Success(0),
	BadRequest(1),
	Unauthorized(2),
	NotFound(4),
	InternalError(5);

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
