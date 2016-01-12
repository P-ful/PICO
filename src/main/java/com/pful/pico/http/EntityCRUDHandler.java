package com.pful.pico.http;

import com.google.gson.Gson;
import com.pful.pico.core.ApplicationContext;
import com.pful.pico.core.PICOErrorCode;
import com.pful.pico.core.PICOException;
import com.pful.pico.core.PICOResponseBuilder;
import com.pful.pico.resource.Entity;
import com.pful.pico.resource.EntityManipulationCallback;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * EntityCRUDHandler is a handler that receives a request from an user and produces a response for it.
 */
public class EntityCRUDHandler
{
	private static final String PARAM_ENTITY_ID = "id";
	private static final String PARAM_TYPE = "type";
	private static final String DATA_FILED_TYPE = "type";
	private static final String DATA_FIELD_PROPERTIES = "properties";
	private static final String PARAM_OFFSET = "offset";
	private static final String PARAM_LIMIT = "limit";

	/**
	 * Installs the routers for the Entity class.
	 *
	 * @param router router is a Router instance provided by Vert.x
	 */
	public static void installRouters(final Router router)
	{
		router.route().handler(BodyHandler.create());

		router.post("/entities/:type").handler(EntityCRUDHandler::handle);
		router.get("/entities/:id").handler(EntityCRUDHandler::handle);
		router.put("/entities/:id").handler(EntityCRUDHandler::handle);
		router.delete("/entities/:id").handler(EntityCRUDHandler::handle);
		router.get("/:type/list").handler(EntityCRUDHandler::handle);
	}

	/**
	 * Entry method for handling requests.
	 *
	 * @param routingContext RoutingContext instance provided by Vert.x
	 */
	public static void handle(final RoutingContext routingContext)
	{
		try {
			switch (routingContext.request().method()) {
			case POST:
				create(routingContext);
				return;

			case GET:
				read(routingContext);
				return;

			case PUT:
				update(routingContext);
				return;

			case DELETE:
				delete(routingContext);
				return;

			default:
				new PICOResponseBuilder(routingContext.response()).forFailure(405, PICOErrorCode.BadRequest)
				                                                  .end();
			}
		}
		catch (Exception e) {
			handleException(routingContext.response(), e);
		}
	}

	/**
	 * create method makes an entity
	 *
	 * @param routingContext RoutingContext instance provided by Vert.x
	 * @throws PICOException
	 * @throws RuntimeException
	 */
	private static void create(final RoutingContext routingContext)
			throws PICOException
	{
		Entity.create(//ApplicationContext.defaultContext(routingContext),
		              routingContext.request().getParam(PARAM_TYPE),
		              routingContext.getBodyAsJson().getMap(),
		              new EntityManipulationCallbackWrapper(routingContext.response())
		              {
			              @Override
			              public void manipulated(final Entity entity)
			              {
				              final JsonObject body = new JsonObject().put(Entity.FIELD_ID, entity.getId())
				                                                      .put(Entity.FIELD_CREATED_AT, entity.getCreatedAt());

				              new PICOResponseBuilder(routingContext.response()).forSuccess()
				                                                                .end(body.encodePrettily());
			              }
		              });
	}

	/**
	 * read method obtains an entity from the database.
	 *
	 * @param routingContext RoutingContext instance provided by Vert.x
	 * @throws PICOException
	 * @throws RuntimeException
	 */
	private static void read(final RoutingContext routingContext)
			throws PICOException
	{
		Entity.read(//	ApplicationContext.defaultContext(routingContext),
		            routingContext.request().getParam(PARAM_ENTITY_ID),
		            new EntityManipulationCallbackWrapper(routingContext.response())
		            {
			            @Override
			            public void manipulated(final Entity entity)
			            {
				            new PICOResponseBuilder(routingContext.response()).forSuccess()
				                                                              .end(entity.toJson().encodePrettily());
			            }
		            });
	}

	/**
	 * update method changes informations in an entity.
	 *
	 * @param routingContext RoutingContext instance provided by Vert.x
	 * @throws PICOException
	 * @throws RuntimeException
	 */
	private static void update(final RoutingContext routingContext)
			throws PICOException
	{
		final ApplicationContext appContext = ApplicationContext.defaultContext(routingContext);

		final String entityId = routingContext.request().getParam(PARAM_ENTITY_ID);
		checkArgument(entityId != null, "entityId shouldn't be null.");

		final JsonObject body = routingContext.getBodyAsJson();

		final String type = body.getString(DATA_FILED_TYPE);
		final Map<String, Object> properties = body.containsKey(DATA_FIELD_PROPERTIES) ?
				body.getJsonObject(DATA_FIELD_PROPERTIES).getMap() : null;

		// FIXME properties.size has a possibility to raise an NullpointerException

		checkArgument(type != null || properties.size() > 0, "at least, type or properties should be given.");

		final Entity entity = Entity.bind(appContext.getAppId(), entityId);

		if (type != null) {
			entity.setType(type);
		}

		if (properties != null) {
			// TODO Merge existing properties and the given properties
			entity.setProperties(properties);
		}

		entity.update(
				new EntityManipulationCallbackWrapper(routingContext.response())
				{
					@Override
					public void manipulated(final Entity entity)
					{
						final JsonObject body = new JsonObject().put(Entity.FIELD_UPDATED_AT, entity.getUpdatedAt());

						new PICOResponseBuilder(routingContext.response()).forSuccess()
						                                                  .end(body.encodePrettily());
					}
				});
	}

	/**
	 * delete method removes an entity.
	 *
	 * @param routingContext RoutingContext instance provided by Vert.x
	 * @throws PICOException
	 * @throws RuntimeException
	 */
	private static void delete(final RoutingContext routingContext)
			throws PICOException
	{
		final ApplicationContext appContext = ApplicationContext.defaultContext(routingContext);

		final String entityId = routingContext.request().getParam(PARAM_ENTITY_ID);
		checkArgument(entityId != null, "id shouldn't be null.");

		final Entity entity = Entity.bind(appContext.getAppId(), entityId);

		entity.delete(
				new EntityManipulationCallbackWrapper(routingContext.response())
				{
					@Override
					public void manipulated(final Entity entity)
					{
						new PICOResponseBuilder(routingContext.response()).forSuccess()
						                                                  .end();
					}
				});
	}

	/**
	 * handleException is an entry to process any exceptions raised in PICO.
	 *
	 * @param response An instance of HttpServerResponse object provided by Vert.x
	 * @param e        A raised exception
	 */
	private static void handleException(final HttpServerResponse response, final Exception e)
	{
		if (e instanceof PICOException) {
			handlePICOException(response, 500, (PICOException) e);
		}
		else {
			handleCheckedAndUncheckedException(response, e);
		}
	}

	/**
	 * handleCheckedAndUncheckedException produces a response for a checked or an unchecked exceptions.
	 *
	 * @param response An instance of HttpServerResponse object provided by Vert.x
	 * @param e        A raised exception
	 */
	private static void handleCheckedAndUncheckedException(final HttpServerResponse response, final Exception e)
	{
		new PICOResponseBuilder(response).forFailure(500, PICOErrorCode.Unexpected, e.getMessage()).end();
	}

	/**
	 * handlePICOException produces a response for a PICOException class.
	 *
	 * @param response       An instance of HttpServerResponse object provided by Vert.x
	 * @param httpStatusCode A state of HTTP protocol, refers to https://en.wikipedia.org/wiki/List_of_HTTP_status_codes
	 * @param e              A raised exception
	 */
	private static void handlePICOException(final HttpServerResponse response, final int httpStatusCode, final PICOException e)
	{
		respondForError(response, httpStatusCode, e.getErrorCode(), e.getMessage());
	}

	/**
	 * respondForError produces a response for an error
	 *
	 * @param response         An instance of HttpServerResponse object provided by Vert.x
	 * @param httpStatusCode   A state of HTTP protocol, refers to https://en.wikipedia.org/wiki/List_of_HTTP_status_codes
	 * @param errorCode        An error-code
	 * @param errorDescription A description for the error-code
	 */
	private static void respondForError(final HttpServerResponse response,
	                                    final int httpStatusCode,
	                                    final PICOErrorCode errorCode,
	                                    final String errorDescription)
	{
		new PICOResponseBuilder(response).forFailure(httpStatusCode, errorCode, errorDescription).end();
	}

	/**
	 * list method makes a list of the given type of an entity.
	 *
	 * @param routingContext RoutingContext instance provided by Vert.x
	 * @throws PICOException
	 * @throws RuntimeException
	 */
	public static void list(RoutingContext routingContext)
			throws PICOException
	{
//		final ApplicationContext appContext = ApplicationContext.defaultContext(routingContext);

		final String type = routingContext.request().getParam(PARAM_TYPE);
		final int offset = Integer.parseInt(routingContext.request().getParam(PARAM_OFFSET));
		final int limit = Integer.parseInt(routingContext.request().getParam(PARAM_LIMIT));

		Entity.list(//appContext,
		            type, offset, limit,
		            (errorCode, typePassed, entityList) -> {
			            if (errorCode != PICOErrorCode.Success) {
				            respondForError(routingContext.response(), 500, errorCode);
				            return;
			            }


			            // FIXME keep converting json to object and vice versa... -_-..
			            Gson gson = new Gson();
			            JsonArray responseBody = null;
			            if (errorCode.equals(PICOErrorCode.Success)) {
				            for (Entity e : entityList) {
					            responseBody.add(gson.toJson(e));
				            }
			            }

			            // FIXME responseBody.add and encodePrettily have a possibility to raise an NullpointerException

			            new PICOResponseBuilder(routingContext.response()).forSuccess()
			                                                              .end(responseBody.encodePrettily());
		            });
	}

	/**
	 * respondForError produces a response for an error
	 *
	 * @param response       An instance of HttpServerResponse object provided by Vert.x
	 * @param httpStatusCode A state of HTTP protocol, refers to https://en.wikipedia.org/wiki/List_of_HTTP_status_codes
	 * @param errorCode      An error-code
	 */
	private static void respondForError(final HttpServerResponse response, final int httpStatusCode, final PICOErrorCode errorCode)
	{
		respondForError(response, httpStatusCode, errorCode, null);
	}

	/**
	 * EntityManipulationCallbackWrapper is a wrapper for EntityManipulationCallback interface.
	 * This is used to produce the common error responses in a centralized way.
	 */
	private static abstract class EntityManipulationCallbackWrapper
			implements EntityManipulationCallback
	{
		private HttpServerResponse response;

		public EntityManipulationCallbackWrapper(final HttpServerResponse response)
		{
			this.response = response;
		}

		@Override
		public void manipulated(final PICOErrorCode errorCode, final Entity entity)
		{
			if (errorCode != PICOErrorCode.Success) {
				respondForError(response, 500, errorCode);
				return;
			}

			manipulated(entity);
		}

		/**
		 * @param entity A manipulated entity
		 */
		public abstract void manipulated(final Entity entity);
	}
}

