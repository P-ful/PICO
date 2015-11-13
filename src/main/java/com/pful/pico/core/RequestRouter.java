package com.pful.pico.core;

import com.google.gson.Gson;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.Map;

public class RequestRouter
{
	private static final String PARAM_ID = "id";
	private static final String PARAM_TYPE = "type";
	private static final String HEADER_FIELD_PICO_APP_ID = "PICO-App-Id";
	private static final String HEADER_FIELD_PICO_ACCESS_TOKEN = "PICO-Access-Token";
	private static final String DATA_FILED_TYPE = "type";
	private static final String DATA_FIELD_PROPERTIES = "properties";
	private static final String PARAM_OFFSET = "offset";
	private static final String PARAM_LIMIT = "limit";

	public static void create(RoutingContext routingContext)
	{
		final ApplicationContext appContext = new ApplicationContext(routingContext.request().getHeader(HEADER_FIELD_PICO_APP_ID),
		                                                             routingContext.request().getHeader(HEADER_FIELD_PICO_ACCESS_TOKEN));
		final String type = routingContext.request().getParam(PARAM_TYPE);
		final Map<String, Object> properties = routingContext.data();

		try {
			Entity.create(appContext, type, properties,
			              (errorCode, entity) -> {
				              JsonObject responseBody = (errorCode.equals(PICOErrorCode.Success)) ?
						              new JsonObject().put("id", entity.getId())
						                              .put("created_at", entity.getCreatedAt()) : null;
				              Response.render(errorCode, responseBody, routingContext.response());
			              });
		}
		catch (PICOException e) {
//			Response.render(...)
			e.printStackTrace();
		}

	}

	public static void read(RoutingContext routingContext)
	{

		final ApplicationContext appContext = new ApplicationContext(routingContext.request().getHeader(HEADER_FIELD_PICO_APP_ID),
		                                                             routingContext.request().getHeader(HEADER_FIELD_PICO_ACCESS_TOKEN));
		final String id = routingContext.request().getParam(PARAM_ID);

		try {
			Entity.read(appContext, id,
			            (errorCode, entity) -> {

			            });
		}
		catch (PICOException e) {
			e.printStackTrace();
		}
	}

	public static void update(RoutingContext routingContext)
	{

		final ApplicationContext appContext = new ApplicationContext(routingContext.request().getHeader(HEADER_FIELD_PICO_APP_ID),
		                                                             routingContext.request().getHeader(HEADER_FIELD_PICO_ACCESS_TOKEN));
		final String id = routingContext.request().getParam(PARAM_ID);
		final String type = (String) routingContext.data().get(DATA_FILED_TYPE);
		final Map<String, Object> properties = (Map<String, Object>) routingContext.data().get(DATA_FIELD_PROPERTIES);

		try {
			Entity.update(appContext, id, type, properties,
			              (errorCode, entity) -> {
				              JsonObject responseBody = (errorCode.equals(PICOErrorCode.Success)) ?
						              new JsonObject().put("updated_at", entity.getUpdatedAt()) : null;
				              Response.render(errorCode, responseBody, routingContext.response());
			              });
		}
		catch (PICOException e) {
			e.printStackTrace();
		}

	}

	public static void delete(RoutingContext routingContext)
	{

		final ApplicationContext appContext = new ApplicationContext(routingContext.request().getHeader(HEADER_FIELD_PICO_APP_ID),
		                                                             routingContext.request().getHeader(HEADER_FIELD_PICO_ACCESS_TOKEN));
		final String id = routingContext.request().getParam(PARAM_ID);

		try {
			Entity.delete(appContext, id,
			              (errorCode, entity) -> {
				              Response.render(errorCode, (JsonObject) null, routingContext.response());
			              });
		}
		catch (PICOException e) {
			e.printStackTrace();
		}
	}

	public static void list(RoutingContext routingContext)
	{

		final ApplicationContext appContext = new ApplicationContext(routingContext.request().getHeader(HEADER_FIELD_PICO_APP_ID),
		                                                             routingContext.request().getHeader(HEADER_FIELD_PICO_ACCESS_TOKEN));
		final String type = routingContext.request().getParam(PARAM_TYPE);
		final int offset = Integer.parseInt(routingContext.request().getParam(PARAM_OFFSET));
		final int limit = Integer.parseInt(routingContext.request().getParam(PARAM_LIMIT));

		try {
			Entity.list(appContext, type, offset, limit,
			            (errorCode, typePassed, entityList) -> {
				            // FIXME keep converting json to object and vice versa... -_-..
				            Gson gson = new Gson();
				            JsonArray responseBody = null;
				            if (errorCode.equals(PICOErrorCode.Success)) {
					            for (Entity e : entityList) {
						            responseBody.add(gson.toJson(e));
					            }
				            }
				            Response.render(errorCode, responseBody, routingContext.response());
			            });
		}
		catch (PICOException e) {
			e.printStackTrace();
		}
	}

}
