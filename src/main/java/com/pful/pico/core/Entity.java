package com.pful.pico.core;

import com.google.gson.Gson;
import com.pful.pico.Service;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Entity is a class to represent the information that an user needs.
 * It is retrieved and stored in the database.
 */
public class Entity
{
	/**
	 * appId is an identifier to recognize which an application have the entity.
	 */
	private String appId;

	/**
	 * id is an unique identifier.
	 */
	private String id;

	/**
	 * type is a classification for what an entity is.
	 * Any string value could be given for it.
	 * No rule exists for the type.
	 * For example, 'person' represents the entity contains the information for a person.
	 * Also any hierarchical representation could be used such as 'animal/mammal'.
	 */
	private String type;

	/**
	 * properties is a collection to hold the information that the entity needs.
	 */
	private Map<String, Object> properties;

	/**
	 * createdAt is a unix timestamp representing when the entity was created.
	 */
	private long createdAt;

	/**
	 * updatedAt is a unix timestamp representing when the entity was updated.
	 */
	private long updatedAt;

	Entity(final String appId, final String type, final Map<String, Object> properties)
	{
		this.appId = appId;
		this.type = type;
		this.properties = properties;
	}

	/**
	 * Create an entity
	 *
	 * @param context    context contains the application-specific information such as an application identifier and token.
	 * @param type       an entity type in any string.
	 * @param properties the properties of the entity.
	 * @param callback   callback is an object that is called when the request has been completed. If the entity is created, PICO calls with a newly created entity instance as a second parameter, or will be null.
	 * @throws PICOException
	 * @throws RuntimeException
	 */
	public static void create(final ApplicationContext context,
	                          final String type,
	                          final Map<String, Object> properties,
	                          final EntityManipulationCallback callback)
			throws PICOException
	{
		requireNonNull(context);
		requireNonNull(type);
		requireNonNull(properties);
		requireNonNull(callback);

		final long createdAt = Instant.now().getEpochSecond();
		final Entity entity = new Entity(context.getAppId(), type, properties);

		entity.createdAt = createdAt;
		entity.updatedAt = createdAt;

		Service.mongoClient.save(MongoDB.COLLECTION_ENTITIES, entity.toJson(),
		                         res -> {
			                         if (res.failed()) {
				                         callback.manipulated(PICOErrorCode.InternalError, null);
				                         return;
			                         }

			                         entity.id = res.result();
			                         callback.manipulated(PICOErrorCode.Success, entity);
		                         });
	}

	/**
	 * Get an entity
	 *
	 * @param context  context contains the application-specific information such as an application identifier and token.
	 * @param id       an unique identifier for an entity
	 * @param callback callback is an object that be called when the request has been completed. If there is an entity related to the parameter 'id', PICO calls with the entity instance, or will be null.
	 * @throws PICOException
	 * @throws RuntimeException
	 */
	public static void read(final ApplicationContext context, final String id, final EntityManipulationCallback callback)
			throws PICOException
	{
		requireNonNull(context);
		requireNonNull(id);
		requireNonNull(callback);

		final JsonObject query = new JsonObject().put("appId", context.getAppId())
		                                         .put("_id", id);

		Service.mongoClient.find(MongoDB.COLLECTION_ENTITIES, query,
		                         res -> {
			                         if (res.failed()) {
				                         callback.manipulated(PICOErrorCode.InternalError, null);
				                         return;
			                         }
			                         else if (res.result().isEmpty()) {
				                         callback.manipulated(PICOErrorCode.NotFound, null);
				                         return;
			                         }

			                         final String entityInString = res.result().get(0).toString();
			                         final Entity entityFound = new Gson().fromJson(entityInString, Entity.class);

			                         callback.manipulated(PICOErrorCode.Success, entityFound);
		                         });
	}

	/**
	 * List entities
	 *
	 * @param context  context contains the application-specific information such as an application identifier and token.
	 * @param type     an entity type in any string.
	 * @param offset   A first position in the database of the list
	 * @param limit    An maximum number of entities that should be included into the list
	 * @param callback callback is an object that be called when the request has been completed. In any cases PICO calls with an valid List instance even there is no entity.
	 * @throws PICOException
	 * @throws RuntimeException
	 */
	static void list(final ApplicationContext context,
	                 final String type,
	                 final int offset,
	                 final int limit,
	                 final EntityListCallback callback)
			throws PICOException
	{
		requireNonNull(context);
		requireNonNull(type);
		requireNonNull(offset);
		requireNonNull(limit);
		requireNonNull(callback);

		final JsonObject query = new JsonObject().put("appId", context.getAppId())
		                                         .put("type", type);

		final FindOptions option = new FindOptions().setSkip(offset)
		                                            .setLimit(limit);

		Service.mongoClient.findWithOptions(MongoDB.COLLECTION_ENTITIES, query, option,
		                                    res -> {
			                                    if (res.failed()) {
				                                    callback.listed(PICOErrorCode.InternalError, null, null);
				                                    return;
			                                    }

			                                    final List<Entity> entityList = new ArrayList<>();

			                                    final Gson gson = new Gson();
			                                    for (final JsonObject entity : res.result()) {
				                                    entityList.add(gson.fromJson(String.valueOf(entity), Entity.class));
			                                    }

			                                    callback.listed(PICOErrorCode.Success, type, entityList);
		                                    });
	}

	/**
	 * Update the fields in the entity
	 *
	 * @param callback callback is an object that be called when the request has been completed. In any cases PICO calls with 'this' entity.
	 * @throws PICOException
	 * @throws RuntimeException
	 */
	void update(final EntityManipulationCallback callback)
			throws PICOException
	{
		requireNonNull(callback);

		final JsonObject query = getDBQuery().put("type", type);

		final long updatedAt = Instant.now().getEpochSecond();
		final JsonObject update = new JsonObject().put("$set", new JsonObject().put("properties", properties)
		                                                                       .put("updatedAt", updatedAt));

		final long currentUpdatedAt = Entity.this.updatedAt;
		Entity.this.updatedAt = updatedAt;

		Service.mongoClient.update(MongoDB.COLLECTION_ENTITIES, query, update,
		                           res -> {
			                           if (res.failed()) {
				                           callback.manipulated(PICOErrorCode.InternalError, null);
				                           Entity.this.updatedAt = currentUpdatedAt;
				                           return;
			                           }

			                           callback.manipulated(PICOErrorCode.Success, Entity.this);
		                           });

	}

	private JsonObject getDBQuery()
	{
		return new JsonObject().put("appId", appId)
		                       .put("id", id);
	}

	/**
	 * Delete the entity. After PICO called the callback object, The entity will be unable to use.
	 *
	 * @param callback callback is an object that be called when the request has been completed. In any cases PICO calls with 'this' entity.
	 * @throws PICOException
	 * @throws RuntimeException
	 */
	void delete(final EntityManipulationCallback callback)
			throws PICOException
	{
		requireNonNull(callback);

		Service.mongoClient.removeOne(MongoDB.COLLECTION_ENTITIES, getDBQuery(),
		                              res -> {
			                              if (res.failed()) {
				                              callback.manipulated(PICOErrorCode.InternalError, Entity.this);
				                              return;
			                              }

			                              callback.manipulated(PICOErrorCode.Success, Entity.this);
		                              });
	}

	public String getId()
	{
		return this.id;
	}

	public JsonObject toJson()
	{
		return new JsonObject(new Gson().toJson(this));
	}

	public void setType(final String type)
	{
		this.type = type;
	}

	public void setProperties(final Map<String, Object> properties)
	{
		this.properties = properties;
	}

	public long getCreatedAt()
	{
		return createdAt;
	}

	public long getUpdatedAt()
	{
		return updatedAt;
	}

	public String getAppId()
	{
		return appId;
	}

	public String getType()
	{
		return type;
	}

	public Map<String, Object> getProperties()
	{
		return properties;
	}
}