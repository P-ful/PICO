package com.pful.pico.core;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

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

	/**
	 * A constructor
	 *
	 * @param appId
	 * @param token
	 */
	public ApplicationContext(final String appId, final String token)
	{
		this.appId = appId;
		this.token = token;
	}

	/**
	 * @param context
	 * @return Creates a default ApplicationContext instance
	 */
	public static ApplicationContext defaultContext(final RoutingContext context)
	{
		return new ApplicationContext(context.request().getHeader("PICO-App-Id"),
		                              context.request().getHeader("PICO-Access-Token"));
	}

	/**
	 * @return
	 */
	public String getAppId()
	{
		return this.appId;
	}

	/**
	 * @return
	 */
	public JsonObject toJson()
	{
		return new JsonObject().put("app_id", appId)
		                       .put("token", token);
	}
}
