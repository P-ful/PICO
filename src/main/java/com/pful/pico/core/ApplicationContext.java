package com.pful.pico.core;

import com.google.common.base.Strings;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import static com.google.common.base.Preconditions.checkArgument;

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
	 * @param appId application-id
	 * @param token access-token issued by PICO
	 */
	public ApplicationContext(final String appId, final String token)
	{
		checkArgument(!Strings.isNullOrEmpty(appId), "appId shouldn't be null or empty.");
		checkArgument(!Strings.isNullOrEmpty(token), "token shouldn't be null or empty.");
		// TODO token validation

		this.appId = appId;
		this.token = token;
	}

	/**
	 * @param context RoutingContext instance provided by Vert.x
	 * @return Creates a default ApplicationContext instance
	 */
	public static ApplicationContext defaultContext(final RoutingContext context)
	{
		checkArgument(context != null, "context shouldn't be null.");
		return new ApplicationContext(context.request().getHeader("PICO-App-Id"),
		                              context.request().getHeader("PICO-Access-Token"));
	}

	/**
	 * @return An application-id
	 */
	public String getAppId()
	{
		return this.appId;
	}

	/**
	 * @return JsonObject representing this entity
	 */
	public JsonObject toJson()
	{
		return new JsonObject().put("app_id", appId)
		                       .put("token", token);
	}
}
