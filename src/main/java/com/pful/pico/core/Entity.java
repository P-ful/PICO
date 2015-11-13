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

	Entity()
	{
	}

	Entity(final String appId, final String type, final Map<String, Object> properties, final long createdAt)
	{
		this(appId, type, properties);
		this.createdAt = createdAt;
		this.updatedAt = createdAt;
	}

	Entity(final String appId, final String type, final Map<String, Object> properties)
	{
		this.appId = appId;
		this.type = type;
		this.properties = properties;
	}

	Entity(final String appId, final String type, final Map<String, Object> properties, final long createdAt, final long updatedAt)
	{
		this(appId, type, properties);
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

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

		// 1. query to ApplicationContexts
		//If a document is inserted with an id, and a document with that id already exists, the insert will fail.
		//save no _id field
		Service.mongoClient.save(Mongo.COLLECTION_APPLICATION_CONTEXTS, new JsonObject(new Gson().toJson(context)), res -> {
			if (res.failed()) {
				res.cause()
				   .printStackTrace();
			}
			else {
				String id = res.result();
				System.out.println("Save application context wih id " + id);
			}
		});

		final Entity entity = new Entity(context.getAppId(), type, properties, Instant.now().getEpochSecond());
		final JsonObject entityInJsonObject = new JsonObject(new Gson().toJson(entity));
		Service.mongoClient.save(Mongo.COLLECTION_ENTITIES, entityInJsonObject,
		                         res -> {
			                         if (res.failed()) {
				                         callback.manipulated(PICOErrorCode.InternalError, null);
				                         return;
			                         }
			                         else {
				                         entity.setId(res.result());
				                         callback.manipulated(PICOErrorCode.Success, entity);
			                         }
		                         });
	}

	public static void read(final ApplicationContext context, final String id, final EntityManipulationCallback callback)
			throws PICOException
	{
		requireNonNull(context);
		requireNonNull(id);
		requireNonNull(callback);

		JsonObject query = new JsonObject().put("appId", context.getAppId())
		                                   .put("id", id);
		// should find a single doc
		Service.mongoClient.find(Mongo.COLLECTION_ENTITIES, query,
		                         res -> {
			                         if (res.failed()) {
				                         callback.manipulated(PICOErrorCode.InternalError, null);
				                         return;
			                         }
			                         else {

				                         if (res.result().isEmpty()) {
					                         callback.manipulated(PICOErrorCode.NotFound, null);
					                         return;
				                         }
				                         else if (res.result().size() != 1) {
					                         callback.manipulated(PICOErrorCode.InternalError, null);
					                         return;
				                         }
				                         else {
					                         String entityInString = res.result().get(0).toString();
					                         Entity entityFound = new Gson().fromJson(entityInString, Entity.class);
					                         callback.manipulated(PICOErrorCode.Success, entityFound);
				                         }

			                         }
		                         });
	}

	static void update(final ApplicationContext context,
	                   final String id,
	                   final String type,
	                   final Map<String, Object> properties,
	                   final EntityManipulationCallback callback)
			throws PICOException
	{
		requireNonNull(context);
		requireNonNull(id);
		requireNonNull(type);
		requireNonNull(properties);
		requireNonNull(callback);

		final JsonObject query = new JsonObject().put("app_id", context.getAppId())
		                                         .put("id", id)
		                                         .put("type", type);
		final long updatedAt = Instant.now().getEpochSecond();
		final JsonObject update = new JsonObject().put("$set", new JsonObject().put("properties", properties)
		                                                                       .put("updated_at", updatedAt));

		Service.mongoClient.update(Mongo.COLLECTION_ENTITIES, query, update,
		                           res -> {
			                           if (res.failed()) {
				                           callback.manipulated(PICOErrorCode.InternalError, null);
				                           return;
			                           }
			                           else {
				                           System.out.println(res.result()); // nothing received
				                           // TODO callback's 2nd param
				                           Entity entity = new Entity();
				                           entity.setUpdatedAt(updatedAt);
				                           callback.manipulated(PICOErrorCode.Success, entity);
			                           }
		                           });

	}

	static void delete(final ApplicationContext context, final String id, final EntityManipulationCallback callback)
			throws PICOException
	{
		requireNonNull(context);
		requireNonNull(id);
		requireNonNull(callback);

		final JsonObject query = new JsonObject().put("app_id", context.getAppId()).put("id", id);

		Service.mongoClient.removeOne(Mongo.COLLECTION_ENTITIES, query,
		                              res -> {
			                              if (res.failed()) {
				                              callback.manipulated(PICOErrorCode.InternalError, null);
				                              return;
			                              }
			                              else {
				                              callback.manipulated(PICOErrorCode.Success, null);
			                              }
		                              });
	}

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

		final JsonObject query = new JsonObject().put("app_id", context.getAppId()).put("type", type);
		final FindOptions option = new FindOptions().setSkip(offset).setLimit(limit);
		Service.mongoClient.findWithOptions(Mongo.COLLECTION_ENTITIES, query, option,
		                                    res -> {
			                                    if (res.failed()) {
				                                    callback.listed(PICOErrorCode.InternalError, null, null);
				                                    return;
			                                    }
			                                    else {
				                                    List<Entity> entityList = new ArrayList<>();
				                                    Gson gson = new Gson();
				                                    for (JsonObject entity : res.result()) {
					                                    entityList.add(gson.fromJson(String.valueOf(entity), Entity.class));
				                                    }
				                                    callback.listed(PICOErrorCode.Success, type, entityList);
			                                    }
		                                    });
	}

	public String getId()
	{
		return this.id;
	}

	private void setId(final String id)
	{
		this.id = id;
	}

	public long getCreatedAt()
	{
		return this.createdAt;
	}

	public void setCreatedAt(final long createdAt)
	{
		this.createdAt = createdAt;
	}

	public long getUpdatedAt()
	{
		return this.updatedAt;
	}

	public void setUpdatedAt(long updatedAt)
	{
		this.updatedAt = updatedAt;
	}

	public void setAppId(final String appId)
	{
		this.appId = appId;
	}
}