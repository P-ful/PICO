package com.pful.pico.core;

import io.vertx.core.json.JsonObject;

/**
 * ApplicationContext keeps the common information for the application object.
 */
public class ApplicationContext
{
	/**
	 * An identifier for an Application object
	 */
	private String appId;

	/**
	 * A token is a session
	 */
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

	public JsonObject toJson()
	{
		return new JsonObject().put("app_id", appId)
		                       .put("token", token);
	}
}
