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
	private static final String HEADER_FIELD_PICO_ERROR_CODE = "PICO-Error-Code";
	private static final String HEADER_FIELD_PICO_ERROR_DESCRIPTION = "PICO-Error-Description";
	private static final String DATA_FILED_TYPE = "type";
	private static final String DATA_FIELD_PROPERTIES = "properties";
	private static final String PARAM_OFFSET = "offset";
	private static final String PARAM_LIMIT = "limit";

	public static void create(RoutingContext routingContext)
	{
		final ApplicationContext appContext = ApplicationContext.defaultContext(routingContext);

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
		catch (RuntimeException e) {
			Response.render(PICOErrorCode.Unexpected, null, routingContext.response());
		}
		catch (PICOException e) {
			Response.render(e.getErrorCode(), null, routingContext.response());
		}

	}

	public static void read(RoutingContext routingContext)
	{
		final ApplicationContext appContext = ApplicationContext.defaultContext(routingContext);

		final String id = routingContext.request().getParam(PARAM_ID);

		try {
			Entity.read(appContext, id,
			            (errorCode, entity) -> {
							Response.render(PICOErrorCode.Success, entity.toJson(), routingContext.response());
			            });
		}
		catch (RuntimeException e) {
			Response.render(PICOErrorCode.Unexpected, null, routingContext.response());
		}
		catch (PICOException e) {
			Response.render(e.getErrorCode(), null, routingContext.response());
		}
	}

	public static void update(RoutingContext routingContext)
	{
		final ApplicationContext appContext = ApplicationContext.defaultContext(routingContext);

		final String id = routingContext.request().getParam(PARAM_ID);
		final String type = (String) routingContext.data().get(DATA_FILED_TYPE);
		final Map<String, Object> properties = (Map<String, Object>) routingContext.data().get(DATA_FIELD_PROPERTIES);

		try {
			// TODO Consider the use of RxJava
			Entity.read(appContext, id, (errorCode, entity) -> {
				// TODO Improve efficiency for updating process

				try {
					if (type == null && (properties == null || properties.isEmpty())) {
						// TODO PICOErrorCode.Unexpected should be replaced with more suitable error code.
						throw new PICOException(PICOErrorCode.Unexpected);
					}

					if (type != null) {
						entity.setType(type);
					}

					if (properties != null) {
						// TODO Merge existing properties and the given properties
						entity.setProperties(properties);
					}

					entity.update((errorCode1, entity1) -> {
						JsonObject responseBody = (errorCode.equals(PICOErrorCode.Success)) ?
								new JsonObject().put("updated_at", entity.getUpdatedAt()) : null;

						Response.render(errorCode, responseBody, routingContext.response());
					});
				}
				catch (RuntimeException e) {
					Response.render(PICOErrorCode.Unexpected, null, routingContext.response());
				}
				catch (PICOException e) {
					Response.render(e.getErrorCode(), null, routingContext.response());
				}
			});
		}
		catch (RuntimeException e) {
			Response.render(PICOErrorCode.Unexpected, null, routingContext.response());
		}
		catch (PICOException e) {
			Response.render(e.getErrorCode(), null, routingContext.response());
		}
	}

	public static void delete(RoutingContext routingContext)
	{
		final ApplicationContext appContext = ApplicationContext.defaultContext(routingContext);

		final String id = routingContext.request().getParam(PARAM_ID);

		try {
			// TODO Consider the use of RxJava
			Entity.read(appContext, id, (errorCode, entity) -> {
				// TODO Improve efficiency for updating process

				try {
					if (id == null) {
						// TODO PICOErrorCode.Unexpected should be replaced with more suitable error code.
						throw new PICOException(PICOErrorCode.Unexpected);
					}

					entity.delete(
							(errorCode1, entity1) -> {
								Response.render(errorCode, null, routingContext.response());
							});
				}
				catch (RuntimeException e) {
					Response.render(PICOErrorCode.Unexpected, null, routingContext.response());
				}
				catch (PICOException e) {
					Response.render(e.getErrorCode(), null, routingContext.response());
				}
			});
		}
		catch (PICOException e) {
			Response.render(e.getErrorCode(), null, routingContext.response());
		}
	}

	public static void list(RoutingContext routingContext)
	{
		final ApplicationContext appContext = ApplicationContext.defaultContext(routingContext);

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
				            Response.renderArray(errorCode, responseBody, routingContext.response());
			            });
		}
		catch (RuntimeException e) {
			Response.render(PICOErrorCode.Unexpected, null, routingContext.response());
		}
		catch (PICOException e) {
			Response.render(e.getErrorCode(), null, routingContext.response());
		}
	}

}
