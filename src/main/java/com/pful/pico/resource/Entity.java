package com.pful.pico.resource;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.pful.pico.Service;
import com.pful.pico.core.ApplicationContext;
import com.pful.pico.core.PICOErrorCode;
import com.pful.pico.core.PICOException;
import com.pful.pico.db.MongoDB;
import com.pful.pico.db.querybuilder.Finder;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Entity is a class to represent the information that an user needs.
 * It is retrieved and stored in the database.
 */
public class Entity
{
	public static final String FIELD_APP_ID = "app_id";
	public static final String FIELD_ID = "_id";
	public static final String FIELD_TYPE = "type";
	public static final String FIELD_PROPERTIES = "properties";
	public static final String FIELD_UPDATED_AT = "updated_at";
	public static final String FIELD_CREATED_AT = "created_at";

	/**
	 * appId is an identifier to recognize which an application have the entity.
	 */
	@SerializedName(FIELD_APP_ID)
	private String appId;

	/**
	 * id is an unique identifier.
	 */
	@SerializedName(FIELD_ID)
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
	@SerializedName(FIELD_CREATED_AT)
	private long createdAt;

	/**
	 * updatedAt is a unix timestamp representing when the entity was updated.
	 */
	@SerializedName(FIELD_UPDATED_AT)
	private long updatedAt;

	/**
	 * groups is an optional field representing where the entity is included.
	 */
	private List<String> groups;

	/**
	 * dirty is a state representing whether the entity should be synchronized with the database.
	 * if dirty is true, every field variables excepts appId and id should be reloaded from the database.
	 */
	private transient boolean dirty = false;

	/**
	 * A constructor
	 */
	private Entity()
	{
	}

	/**
	 * A constructor that is used for bind() method.
	 *
	 * @param appId    An application-id
	 * @param entityId An entity-id
	 */
	private Entity(final String appId, final String entityId)
	{
		checkArgument(!Strings.isNullOrEmpty(appId) && !Strings.isNullOrEmpty(entityId),
		              "appId and entityId shouldn't be null or empty.");

		this.appId = appId;
		this.id = entityId;
		this.dirty = true;
	}

	/**
	 * A constructor that is used for create() method.
	 *
	 * @param appId      An application-id
	 * @param entityId   An entity-id
	 * @param type       A type of an entity
	 * @param properties Properties of an entity
	 */
	private Entity(final String appId, final String entityId, final String type, final Map<String, Object> properties)
	{
		checkArgument(!Strings.isNullOrEmpty(appId) && !Strings.isNullOrEmpty(entityId),
		              "appId and type shouldn't be null or empty.");

		this.appId = appId;
		this.id = entityId;
		this.type = type;
		this.properties = properties;
		this.dirty = false;
	}

	/**
	 * bind make an entity instance depending on the parameters 'appId' and 'entityId'.
	 * But the bound entity have only appId and entityId so that it must be synchronized from the database.
	 *
	 * @param appId    An application-id
	 * @param entityId An entity-id
	 * @return A entity bound with the parameters 'appId' and 'entityId'
	 */
	public static Entity bind(final String appId, final String entityId)
	{
		return new Entity(appId, entityId, null, null);
	}

	/**
	 * bind make an entity instance depending on the parameters 'appId', 'entityId', and 'properties'.
	 * But the bound entity have only appId and entityId so that it must be synchronized from the database.
	 *
	 * @param appId    An application-id
	 * @param entityId An entity-id
	 * @param type     An entity type in any string.
	 * @return
	 */
	public static Entity bind(final String appId, final String entityId, final String type)
	{
		return new Entity(appId, entityId, type, null);
	}

	/**
	 * bind make an entity instance depending on the parameters 'appId', 'entityId', and 'properties'.
	 * But the bound entity have only appId and entityId so that it must be synchronized from the database.
	 *
	 * @param appId      An application-id
	 * @param entityId   An entity-id
	 * @param properties The properties of the entity.
	 * @return
	 */
	public static Entity bind(final String appId, final String entityId, final Map<String, Object> properties)
	{
		return new Entity(appId, entityId, null, properties);
	}

	/**
	 * bind make an entity instance depending on the parameters 'appId', 'entityId', 'type', and 'properties'.
	 * But the bound entity have only appId and entityId so that it must be synchronized from the database.
	 *
	 * @param appId      An application-id
	 * @param entityId   An entity-id
	 * @param type       An entity type in any string.
	 * @param properties The properties of the entity.
	 * @return
	 */
	public static Entity bind(final String appId, final String entityId, final String type, final Map<String, Object> properties)
	{
		return new Entity(appId, entityId, type, properties);
	}

	/**
	 * Create an entity
	 *
	 * @param context    context contains the application-specific information such as an application identifier and token.
	 * @param type       An entity type in any string.
	 * @param properties The properties of the entity or null. If it is null, it means the entity has no properties.
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
		checkArgument(context != null && !Strings.isNullOrEmpty(context.getAppId()), "context shouldn't be null and valid.");
		checkArgument(!Strings.isNullOrEmpty(type), "type shouldn't be null and empty.");
		checkArgument(callback != null, "callback shouldn't be null.");

		final long createdAt = Instant.now().getEpochSecond();
		final Entity entity = new Entity();

		entity.appId = context.getAppId();
		entity.type = type;
		entity.properties = properties;

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

	public JsonObject toJson()
	{
		return new JsonObject(new Gson().toJson(this));
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
		checkArgument(context != null && !Strings.isNullOrEmpty(context.getAppId()), "context shouldn't be null and valid.");
		checkArgument(!Strings.isNullOrEmpty(id), "id shouldn't be null or empty.");
		checkArgument(callback != null, "callback shouldn't be null.");

//		final JsonObject query = new JsonObject().put(FIELD_APP_ID, context.getAppId())
//		                                         .put(FIELD_ID, id);

		final JsonObject query = Finder.newQuery()
		                               .field(FIELD_APP_ID).is(context.getAppId())
		                               .field(FIELD_ID).is(id)
		                               .toJson();

		Service.mongoClient.find(MongoDB.COLLECTION_ENTITIES,
		                         query,
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
	public static void list(final ApplicationContext context,
	                        final String type,
	                        final int offset,
	                        final int limit,
	                        final EntityListCallback callback)
			throws PICOException
	{
		checkArgument(context != null && !Strings.isNullOrEmpty(context.getAppId()), "context shouldn't be null and valid.");
		checkArgument(!Strings.isNullOrEmpty(type), "type shouldn't be null and empty.");
		checkArgument(offset >= 0, "offset should greater than equal to zero.");
		checkArgument(limit > 0, "offset should greater than zero.");
		checkArgument(callback != null, "callback shouldn't be null.");

//		final JsonObject query = new JsonObject().put(FIELD_APP_ID, context.getAppId())
//		                                         .put(FIELD_TYPE, type);

		final JsonObject query = Finder.newQuery()
		                               .field(FIELD_APP_ID).is(context.getAppId())
		                               .field(FIELD_TYPE).is(type)
		                               .toJson();

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
	public void update(final EntityManipulationCallback callback)
			throws PICOException
	{
		checkArgument(callback != null, "callback shouldn't be null.");

		final long newUpdatedAt = Instant.now().getEpochSecond();

		final JsonObject fields = new JsonObject();
		fields.put(FIELD_UPDATED_AT, newUpdatedAt);

		if (!Strings.isNullOrEmpty(type)) {
			fields.put(FIELD_TYPE, type);
		}

		if (properties != null) {
			fields.put(FIELD_PROPERTIES, properties);
		}

		final JsonObject update = new JsonObject();
		update.put("$set", fields);

		Service.mongoClient.update(MongoDB.COLLECTION_ENTITIES,
		                           getDBQuery(),
		                           update,
		                           res -> {
			                           if (res.failed()) {
				                           callback.manipulated(PICOErrorCode.InternalError, Entity.this);
				                           return;
			                           }

			                           Entity.this.updatedAt = newUpdatedAt;
			                           callback.manipulated(PICOErrorCode.Success, Entity.this);
		                           });

	}

	private JsonObject getDBQuery()
	{
//		return new JsonObject().put(FIELD_APP_ID, appId)
//		                       .put(FIELD_ID, id);
		return Finder.newQuery()
		             .field(FIELD_APP_ID).is(appId)
		             .field(FIELD_ID).is(id)
		             .toJson();
	}

	/**
	 * Delete the entity. After PICO called the callback object, The entity will be unable to use.
	 *
	 * @param callback callback is an object that be called when the request has been completed. In any cases PICO calls with 'this' entity.
	 * @throws PICOException
	 * @throws RuntimeException
	 */
	public void delete(final EntityManipulationCallback callback)
			throws PICOException
	{
		checkArgument(callback != null, "callback shouldn't be null.");

		Service.mongoClient.removeOne(MongoDB.COLLECTION_ENTITIES,
		                              getDBQuery(),
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

	public void setType(final String type)
	{
		this.type = type;
	}

	public Map<String, Object> getProperties()
	{
		return properties;
	}

	public void setProperties(final Map<String, Object> properties)
	{
		this.properties = properties;
	}
}