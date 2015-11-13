package com.pful.pico.core;

/**
 * ApplicationContext keeps the common information for the application object.
 */
public class ApplicationContext
{
	private String appId;
	private String token;

	public ApplicationContext(final String appId, final String token)
	{
		this.appId = appId;
		this.token = token;
	}

	public String getAppId()
	{
		return this.appId;
	}
}
