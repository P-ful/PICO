package com.pful.pico.resource;

import com.google.common.base.Strings;
import com.pful.pico.Service;
import com.pful.pico.core.ApplicationContext;
import com.pful.pico.core.PICOErrorCode;
import com.pful.pico.core.PICOException;
import com.pful.pico.db.MongoDB;
import com.pful.pico.db.querybuilder.Finder;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Created by youngdocho on 12/2/15.
 */
public class GroupManipulation
{
	public static final String FIELD_GROUPS = "groups";

	private static final String DB_METHOD_FIND_AND_MODIFY = "findAndModify";
	private static final String DB_METHOD_UPDATE = "update";
	private static final String FIND_AND_MODIFY_FIELD_QUERY = "query";
	private static final String FIND_AND_MODIFY_FIELD_UPDATE = "update";
	private static final String FIND_AND_MODIFY_FIELD_REMOVE = "remove";
	private static final String FIND_AND_MODIFY_FIELD_NEW = "new";

	private static final String UPDATE_FIELD_UPDATES = "updates";
	private static final String UPDATE_UPDATES_FIELD_QUERY = "q";
	private static final String UPDATE_UPDATES_FIELD_UPDATE = "u";
	private static final String UPDATE_UPDATES_FIELD_MULTI = "multi";

	/**
	 * Create a group in the specified entity
	 *
	 * @param context  A context
	 * @param entityId An entity-id
	 * @param group    A group that will be inserted in the list of the groups field
	 * @param callback
	 * @throws PICOException
	 */
	public static void create(final ApplicationContext context,
	                          final String entityId,
	                          final String group,
	                          final GroupManipulationCallback callback)
			throws PICOException
	{
		checkArgument(context != null && !Strings.isNullOrEmpty(context.getAppId()),
		              "context shouldn't be null and valid.");
		checkArgument(!Strings.isNullOrEmpty(entityId) && !Strings.isNullOrEmpty(group),
		              "entityId, and group shouldn't be null or empty.");
		checkArgument(callback != null, "callback shouldn't be null.");

		final JsonObject condition = Finder.newQuery()
		                                   .field(Entity.FIELD_APP_ID).is(context.getAppId())
		                                   .field(Entity.FIELD_ID).is(entityId)
		                                   .field(FIELD_GROUPS).ninStrings(group)
		                                   .toJson();

//		final JsonObject condition = new JsonObject().put(Entity.FIELD_APP_ID, appId)
//		                                             .put(Entity.FIELD_ID, entityId)
//		                                             .put(FIELD_GROUPS, new JsonObject().put("$nin", new ArrayList(Arrays.asList(group))));

		final JsonObject update = new JsonObject().put("$push", new JsonObject().put(FIELD_GROUPS, group))
		                                          .put("$set",
		                                               new JsonObject().put(Entity.FIELD_UPDATED_AT, Instant.now().getEpochSecond()));

		final JsonObject command = new JsonObject().put(DB_METHOD_FIND_AND_MODIFY, MongoDB.COLLECTION_ENTITIES)
		                                           .put(FIND_AND_MODIFY_FIELD_QUERY, condition)
		                                           .put(FIND_AND_MODIFY_FIELD_UPDATE, update)
		                                           .put(FIND_AND_MODIFY_FIELD_NEW, true);

		Service.mongoClient.runCommand(DB_METHOD_FIND_AND_MODIFY, command,
		                               res -> {
			                               if (res.failed()) {
				                               callback.manipulated(PICOErrorCode.InternalError, null);
				                               return;
			                               }
			                               else if (res.result()
			                                           .getJsonObject("value") == null) {
				                               callback.manipulated(PICOErrorCode.Unexpected, null); //
				                               return;
			                               }

			                               // TODO
			                               // res.result() - {value : updated doc, lastErrorObject: {updatedExisting: true, n: 1}, ok: 1.0}
			                               final JsonObject lastErrorObject = res.result()
			                                                                     .getJsonObject("lastErrorObject");
			                               // success : lastErrorObject - { updatedExisting: true, n : 1 }
			                               callback.manipulated(PICOErrorCode.Success,
			                                                    res.result()
			                                                       .getJsonObject("value"));
		                               });
	}

	/**
	 * Get all the groups contained in the specified application-id
	 *
	 * @param context    An application-id
	 * @param callback
	 * @throws PICOException
	 */
	public static void read(final ApplicationContext context,
	                        final GroupManipulationCallback callback)
			throws PICOException
	{
		checkArgument(context != null && !Strings.isNullOrEmpty(context.getAppId()),
		              "context shouldn't be null and valid.");
		checkArgument(callback != null, "callback shouldn't be null.");

//		final JsonObject query = new JsonObject().put(Entity.FIELD_APP_ID, appId);

		final JsonObject query = Finder.newQuery().field(Entity.FIELD_APP_ID).is(context.getAppId()).toJson();

		Service.mongoClient.find(MongoDB.COLLECTION_ENTITIES, query,
		                         res -> {
			                         if (res.failed()) {
				                         callback.manipulated(PICOErrorCode.InternalError, null);
				                         return;
			                         }

			                         final List<String> groupsInList = new ArrayList<>();

			                         res.result()
			                            .stream()
			                            .filter(e -> e.containsKey(FIELD_GROUPS))
			                            .forEach(e -> {
				                            groupsInList.addAll(e.getJsonArray(FIELD_GROUPS)
				                                                 .getList());
			                            });

			                         final Set<String> groupsInSet = new HashSet<>(groupsInList);

			                         final JsonArray groupsInJsonArray = new JsonArray();

			                         groupsInSet.stream()
			                                    .forEach(e -> groupsInJsonArray.add(e));

			                         callback.manipulated(PICOErrorCode.Success, new JsonObject().put(FIELD_GROUPS, groupsInJsonArray));
		                         });
	}

	/**
	 * Get all the groups in the specified entity
	 *
	 * @param context  An application-id
	 * @param entityId An entity-id
	 * @param callback
	 * @throws PICOException
	 */
	public static void read(final ApplicationContext context,
	                        final String entityId,
	                        final GroupManipulationCallback callback)
			throws PICOException
	{
		checkArgument(context != null && !Strings.isNullOrEmpty(context.getAppId()),
		              "context shouldn't be null and valid.");
		checkArgument(!Strings.isNullOrEmpty(entityId),
		              "entityId shouldn't be null or empty.");
		checkArgument(callback != null, "callback shouldn't be null.");

		final JsonObject query = Finder.newQuery()
		                               .field(Entity.FIELD_APP_ID).is(context.getAppId())
		                               .field(Entity.FIELD_ID).is(entityId)
		                               .field(FIELD_GROUPS).exists()
		                               .toJson();

//		final JsonObject query = new JsonObject().put(Entity.FIELD_APP_ID, appId)
//		                                         .put(Entity.FIELD_ID, entityId)
//		                                         .put(FIELD_GROUPS, new JsonObject().put("$exists", true));

		Service.mongoClient.find(MongoDB.COLLECTION_ENTITIES, query,
		                         res -> {
			                         if (res.failed()) {
				                         callback.manipulated(PICOErrorCode.InternalError, null);
				                         return;
			                         }

			                         final Set groupsInSet = new HashSet();
			                         final JsonArray groupsInJsonArray = new JsonArray();

			                         res.result()
			                            .stream()
			                            .forEach(e -> groupsInSet.addAll(e.getJsonArray(FIELD_GROUPS)
			                                                              .getList()));
			                         groupsInSet.stream()
			                                    .forEach(e -> groupsInJsonArray.add(e));

			                         callback.manipulated(PICOErrorCode.Success, new JsonObject().put(FIELD_GROUPS, groupsInJsonArray));
		                         });
	}

	/**
	 * Get all the entities included in the specified group of the application-id
	 *
	 * @param context
	 * @param group    A group name
	 * @param callback
	 * @throws PICOException
	 */
	// TODO method name
	public static void readEntities(final ApplicationContext context,
	                                final String group,
	                                final GroupManipulationCallback callback)
			throws PICOException
	{
		checkArgument(context != null && !Strings.isNullOrEmpty(context.getAppId()),
		              "context shouldn't be null and valid.");
		checkArgument(!Strings.isNullOrEmpty(group),
		              "group shouldn't be null or empty.");
		checkArgument(callback != null, "callback shouldn't be null.");

		final JsonObject query = Finder.newQuery()
		                               .field(Entity.FIELD_APP_ID).is(context.getAppId())
		                               .field(FIELD_GROUPS).inStrings(group)
		                               .toJson();

//		final JsonObject query = new JsonObject().put(Entity.FIELD_APP_ID, appId)
//		                                         .put(FIELD_GROUPS,
//		                                              new JsonObject().put("$in", new ArrayList<>(Arrays.asList(group))));

		Service.mongoClient.find(MongoDB.COLLECTION_ENTITIES, query,
		                         res -> {
			                         if (res.failed()) {
				                         callback.manipulated(PICOErrorCode.InternalError, null);
				                         return;
			                         }

			                         final JsonArray entitiesInJsonArray = new JsonArray();

			                         res.result()
			                            .stream()
			                            .forEach(e -> entitiesInJsonArray.add(e.getString(Entity.FIELD_ID)));

			                         callback.manipulated(PICOErrorCode.Success, new JsonObject().put("elemsInGroup", entitiesInJsonArray));
		                         });
	}

	/**
	 * replace the current group with the new group through the specified the application
	 *
	 * @param context
	 * @param originalGroup
	 * @param newGroup
	 * @param callback
	 */
	public static void update(final ApplicationContext context,
	                          final String originalGroup,
	                          final String newGroup,
	                          final GroupManipulationCallback callback)
	{
		checkArgument(context != null && !Strings.isNullOrEmpty(context.getAppId()),
		              "context shouldn't be null and valid.");
		checkArgument(!Strings.isNullOrEmpty(originalGroup) && !Strings.isNullOrEmpty(newGroup),
		              "groups shouldn't be null or empty.");
		checkArgument(callback != null, "callback shouldn't be null.");

		final JsonObject condition = Finder.newQuery()
		                                   .field(Entity.FIELD_APP_ID).is(context.getAppId())
		                                   .field(FIELD_GROUPS).is(originalGroup)
		                                   .toJson();

//		final JsonObject condition = new JsonObject().put(Entity.FIELD_APP_ID, appId)
//		                                             .put(FIELD_GROUPS, originalGroup);

		final JsonObject update = new JsonObject().put("$set",
		                                               new JsonObject().put(FIELD_GROUPS + ".$", newGroup)
		                                                               .put(Entity.FIELD_UPDATED_AT,
		                                                                    Instant.now()
		                                                                           .getEpochSecond()));

		final JsonArray updates = new JsonArray().add(new JsonObject().put(UPDATE_UPDATES_FIELD_QUERY, condition)
		                                                              .put(UPDATE_UPDATES_FIELD_UPDATE, update)
		                                                              .put(UPDATE_UPDATES_FIELD_MULTI, true));

		final JsonObject query = new JsonObject().put(DB_METHOD_UPDATE, MongoDB.COLLECTION_ENTITIES)
		                                         .put(UPDATE_FIELD_UPDATES, updates);

		Service.mongoClient.runCommand(DB_METHOD_UPDATE, query,
		                               res -> {
			                               if (res.failed()) {
				                               callback.manipulated(PICOErrorCode.InternalError, null);
				                               return;
			                               }
			                               else if (!res.result()
			                                            .getInteger("nModified")
			                                            .equals(res.result()
			                                                       .getInteger("n"))) {
				                               callback.manipulated(PICOErrorCode.Unexpected, null); // TODO
				                               return;
			                               }

			                               callback.manipulated(PICOErrorCode.Success, res.result());
		                               });
	}

	/**
	 * replace the current group with the new group in the specified entity.
	 *
	 * @param context
	 * @param entityId
	 * @param originalGroup
	 * @param newGroup
	 * @param callback
	 */
	public static void update(final ApplicationContext context,
	                          final String entityId,
	                          final String originalGroup,
	                          final String newGroup,
	                          final GroupManipulationCallback callback)
	{
		checkArgument(context != null && !Strings.isNullOrEmpty(context.getAppId()),
		              "context shouldn't be null and valid.");
		checkArgument(!Strings.isNullOrEmpty(entityId) && !Strings.isNullOrEmpty(originalGroup)
				              && !Strings.isNullOrEmpty(newGroup),
		              "entityId and groups shouldn't be null or empty.");
		checkArgument(callback != null, "callback shouldn't be null.");

		final JsonObject condition = Finder.newQuery()
		                                   .field(Entity.FIELD_APP_ID).is(context.getAppId())
		                                   .field(Entity.FIELD_ID).is(entityId)
		                                   .field(FIELD_GROUPS).inStrings(originalGroup)
		                                   .toJson();

//		final JsonObject condition = new JsonObject().put(Entity.FIELD_APP_ID, appId)
//		                                             .put(Entity.FIELD_ID, entityId)
//		                                             .put(FIELD_GROUPS,
//		                                                  new JsonObject().put("$in", new ArrayList(Arrays.asList(originalGroup))));

		final JsonObject update = new JsonObject().put("$set",
		                                               new JsonObject().put(FIELD_GROUPS + ".$", newGroup)
		                                                               .put(Entity.FIELD_UPDATED_AT,
		                                                                    Instant.now()
		                                                                           .getEpochSecond()));

		final JsonObject command = new JsonObject().put(DB_METHOD_FIND_AND_MODIFY, MongoDB.COLLECTION_ENTITIES)
		                                           .put(FIND_AND_MODIFY_FIELD_QUERY, condition)
		                                           .put(FIND_AND_MODIFY_FIELD_UPDATE, update)
		                                           .put(FIND_AND_MODIFY_FIELD_NEW, false)
		                                           .put(FIND_AND_MODIFY_FIELD_NEW, true);

		Service.mongoClient.runCommand(DB_METHOD_FIND_AND_MODIFY, command,
		                               res -> {
			                               if (res.failed()) {
				                               callback.manipulated(PICOErrorCode.InternalError, null);
				                               return;
			                               }
			                               else if (res.result()
			                                           .getJsonObject("value") == null) {
				                               callback.manipulated(PICOErrorCode.Unexpected, null); //
				                               return;
			                               }

			                               // TODO
			                               // res.result() - {value : updated doc, lastErrorObject: {updatedExisting: true, n: 1}, ok: 1.0}
			                               final JsonObject lastErrorObject = res.result()
			                                                                     .getJsonObject("lastErrorObject");
			                               // success : lastErrorObject - { updatedExisting: true, n : 1 }
			                               callback.manipulated(PICOErrorCode.Success,
			                                                    res.result()
			                                                       .getJsonObject("value"));
		                               });
	}

	/**
	 * delete the specified group in the application
	 *
	 * @param context
	 * @param group
	 * @param callback
	 */
	public static void delete(final ApplicationContext context,
	                          final String group,
	                          final GroupManipulationCallback callback)
	{
		checkArgument(context != null && !Strings.isNullOrEmpty(context.getAppId()),
		              "context shouldn't be null and valid.");
		checkArgument(!Strings.isNullOrEmpty(group),
		              "group shouldn't be null or empty.");
		checkArgument(callback != null, "callback shouldn't be null.");

		final JsonObject condition = Finder.newQuery()
		                                   .field(Entity.FIELD_APP_ID).is(context.getAppId())
		                                   .field(FIELD_GROUPS).inStrings(group)
		                                   .toJson();

//		final JsonObject condition = new JsonObject().put(Entity.FIELD_APP_ID, appId)
//		                                             .put(FIELD_GROUPS,
//		                                                  new JsonObject().put("$in",
//		                                                                       new ArrayList(Arrays.asList(group))));

		final JsonObject update = new JsonObject().put("$pull",
		                                               new JsonObject().put(FIELD_GROUPS, group))
		                                          .put("$set",
		                                               new JsonObject().put(Entity.FIELD_UPDATED_AT,
		                                                                    Instant.now()
		                                                                           .getEpochSecond()));

		final JsonObject updatesInJsonObject = new JsonObject().put(UPDATE_UPDATES_FIELD_QUERY, condition)
		                                                       .put(UPDATE_UPDATES_FIELD_UPDATE, update)
		                                                       .put(UPDATE_UPDATES_FIELD_MULTI, true);
		final JsonArray updates = new JsonArray().add(updatesInJsonObject);

		final JsonObject query = new JsonObject().put(DB_METHOD_UPDATE, MongoDB.COLLECTION_ENTITIES)
		                                         .put(UPDATE_FIELD_UPDATES, updates);

		Service.mongoClient.runCommand(DB_METHOD_UPDATE, query,
		                               res -> {
			                               if (res.failed()) {
				                               callback.manipulated(PICOErrorCode.InternalError, null);
				                               return;
			                               }

			                               callback.manipulated(PICOErrorCode.Success, res.result());
		                               });
	}

	/**
	 * delete the specified group in the entity
	 *
	 * @param context
	 * @param entityId
	 * @param group
	 * @param callback
	 */
	public static void delete(final ApplicationContext context,
	                          final String entityId,
	                          final String group,
	                          final GroupManipulationCallback callback)
	{
		checkArgument(context != null && !Strings.isNullOrEmpty(context.getAppId()),
		              "context shouldn't be null and valid.");
		checkArgument(!Strings.isNullOrEmpty(entityId) && !Strings.isNullOrEmpty(group),
		              "entityId and group shouldn't be null or empty.");
		checkArgument(callback != null, "callback shouldn't be null.");

		final JsonObject condition = Finder.newQuery()
		                                   .field(Entity.FIELD_APP_ID).is(context.getAppId())
		                                   .field(Entity.FIELD_ID).is(entityId)
		                                   .toJson();

//		final JsonObject condition = new JsonObject().put(Entity.FIELD_APP_ID, appId)
//		                                             .put(Entity.FIELD_ID, entityId);

		final JsonObject update = new JsonObject().put("$pull",
		                                               new JsonObject().put(FIELD_GROUPS, group))
		                                          .put("$set",
		                                               new JsonObject().put(Entity.FIELD_UPDATED_AT,
		                                                                    Instant.now()
		                                                                           .getEpochSecond()));

		final JsonObject query = new JsonObject().put(DB_METHOD_FIND_AND_MODIFY, MongoDB.COLLECTION_ENTITIES)
		                                         .put(FIND_AND_MODIFY_FIELD_QUERY, condition)
		                                         .put(FIND_AND_MODIFY_FIELD_UPDATE, update)
		                                         .put(FIND_AND_MODIFY_FIELD_NEW, true);

		Service.mongoClient.runCommand(DB_METHOD_FIND_AND_MODIFY, query,
		                               res -> {
			                               if (res.failed()) {
				                               callback.manipulated(PICOErrorCode.InternalError, null);
				                               return;
			                               }

			                               // res.result() - {value : modified Object in JsonObject, lastErrorObject: {}, ok: 1.0}
			                               callback.manipulated(PICOErrorCode.Success,
			                                                    res.result()
			                                                       .getJsonObject("lastErrorObject"));
		                               });
	}
}
