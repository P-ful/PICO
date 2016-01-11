package com.pful.pico.resource;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.pful.pico.Service;
import com.pful.pico.core.ApplicationContext;
import com.pful.pico.core.PICOErrorCode;
import com.pful.pico.db.MongoDB;
import com.pful.pico.db.querybuilder.Finder;
import io.vertx.core.json.JsonObject;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.pful.pico.db.querybuilder.Field.field;

/**
 * Created by youngdocho on 12/8/15.
 */
public class GroupSetOperation
{
//	private static final String QUERY_GETTING_TWO_GROUPS = "QUERY_GETTING_TWO_GROUPS";

	/**
	 * @param context
	 * @param group1   group id referencing an entity
	 * @param group2   group id referencing an entity
	 * @param callback
	 */
	public static void union(final ApplicationContext context,
	                         final String group1,
	                         final String group2,
	                         final GroupSetOperationCallback callback)
	{
		checkArgument(context != null && !Strings.isNullOrEmpty(context.getAppId()),
		              "context shouldn't be null and valid.");
		checkArgument(!Strings.isNullOrEmpty(group1) && !Strings.isNullOrEmpty(group2),
		              "appId and groups shouldn't be null or empty.");

//		Query -> { 'app_id' : <#app_id>, $or : [ { 'groups' : <#group_1> }, { 'groups' : <#group_2> }}]}

		final JsonObject query = Finder.newQuery()
		                               .field(Entity.FIELD_APP_ID).is(context.getAppId())
		                               .anyOf(field(GroupManipulation.FIELD_GROUPS).is(group1),
		                                      field(GroupManipulation.FIELD_GROUPS).is(group2))
		                               .toJson();

		performQueryAndDeliver(callback, query);
	}

	private static void performQueryAndDeliver(final GroupSetOperationCallback callback, final JsonObject query)
	{
		Service.mongoClient.find(MongoDB.COLLECTION_ENTITIES, query,
		                         res -> {
			                         if (res.failed()) {
				                         callback.manipulated(PICOErrorCode.BadRequest, null);
				                         return;
			                         }

			                         final Set<Entity> elements = new HashSet<>();

			                         final Gson gson = new Gson();

			                         // TODO Is that better to convert the res to Entity from JsonObject?
			                         res.result()
			                            .stream()
			                            .forEach(e -> elements.add(gson.fromJson(e.toString(), Entity.class)));

			                         callback.manipulated(PICOErrorCode.Success, elements);
		                         });
	}

	/**
	 * @param context
	 * @param group1   group id referencing an entity
	 * @param group2   group id referencing an entity
	 * @param callback
	 */
	public static void intersection(final ApplicationContext context,
	                                final String group1,
	                                final String group2,
	                                final GroupSetOperationCallback callback)
	{
		checkArgument(context != null && !Strings.isNullOrEmpty(context.getAppId()),
		              "context shouldn't be null and valid.");
		checkArgument(!Strings.isNullOrEmpty(group1) && !Strings.isNullOrEmpty(group2),
		              "appId and groups shouldn't be null or empty.");

//		Query -> { 'app_id' : <#app_id>, 'groups' : { $all : [ <#group_1>, <#group_2> ]}}

		final JsonObject query = Finder.newQuery()
		                               .field(Entity.FIELD_APP_ID).is(context.getAppId())
		                               .field(GroupManipulation.FIELD_GROUPS).allInStrings(group1, group2)
		                               .toJson();

		performQueryAndDeliver(callback, query);
	}

	/**
	 * @param context
	 * @param group1   group id referencing an entity
	 * @param group2   group id referencing an entity
	 * @param callback
	 */
	public static void difference(final ApplicationContext context,
	                              final String group1,
	                              final String group2,
	                              final GroupSetOperationCallback callback)
	{
		checkArgument(context != null && !Strings.isNullOrEmpty(context.getAppId()),
		              "context shouldn't be null and valid.");
		checkArgument(!Strings.isNullOrEmpty(group1) && !Strings.isNullOrEmpty(group2),
		              "groups shouldn't be null or empty.");

		// Query -> { '$and' : [ { 'groups' : <#group1> } , { 'groups' : { $ne : <#groups2> }}]}

		final JsonObject query = Finder.newQuery()
		                               .field(Entity.FIELD_APP_ID).is(context.getAppId())
		                               .allOf(field(GroupManipulation.FIELD_GROUPS).is(group1),
		                                      field(GroupManipulation.FIELD_GROUPS).ne(group2))
		                               .toJson();

		performQueryAndDeliver(callback, query);
	}

	/**
	 * @param context
	 * @param group1   group id referencing an entity
	 * @param group2   group id referencing an entity
	 * @param callback
	 */
	public static void subset(final ApplicationContext context,
	                          final String group1,
	                          final String group2,
	                          final GroupLogicalSetOperationCallback callback)
	{
		checkArgument(context != null && !Strings.isNullOrEmpty(context.getAppId()),
		              "context shouldn't be null and valid.");
		checkArgument(!Strings.isNullOrEmpty(group1) && !Strings.isNullOrEmpty(group2),
		              "groups shouldn't be null or empty.");

		getElements(context, group1,
		            entitiesInGroup1 -> {
			            getElements(context, group2,
			                        entitiesInGroup2 -> {
				                        if (entitiesInGroup1 == null || entitiesInGroup2 == null) {
					                        callback.manipulated(PICOErrorCode.BadRequest, false);
					                        return;
				                        }

				                        // because of comparing entity-objects itself, containsAll returns "false".
				                        final Gson gson = new Gson();
				                        final Collection<String> groupSet1 = new HashSet<>();
				                        final Collection<String> groupSet2 = new HashSet<>();

				                        entitiesInGroup1.stream().forEach(e -> groupSet1.add(gson.toJson(e)));
				                        entitiesInGroup2.stream().forEach(e -> groupSet2.add(gson.toJson(e)));

				                        callback.manipulated(PICOErrorCode.Success, groupSet1.containsAll(groupSet2));
			                        });
		            });
	}

	/**
	 * @param context
	 * @param group group id referencing an entity
	 * @return
	 */
	// TODO : extract this as a general function for getting Entities in a specfic group
	private static void getElements(final ApplicationContext context,
	                                final String group,
	                                final GroupElementListCallback callback)
	{
		final JsonObject query = Finder.newQuery()
		                               .field(Entity.FIELD_APP_ID).is(context.getAppId())
		                               .field(GroupManipulation.FIELD_GROUPS).inStrings(group)
		                               .toJson();

//		final JsonObject query = new JsonObject().put(Entity.FIELD_APP_ID, appId)
//		                                         .put(GroupManipulation.FIELD_GROUPS,
//		                                              new JsonObject().put("$in", Arrays.asList(group)));

		Service.mongoClient.find(MongoDB.COLLECTION_ENTITIES, query,
		                         res -> {
			                         if (res.failed()) {
				                         callback.listed(null);
				                         return;
			                         }

			                         final Collection<Entity> elemsInCollection = new HashSet<>();

			                         final Gson gson = new Gson();

			                         res.result()
			                            .stream()
			                            .forEach(e -> elemsInCollection.add(gson.fromJson(e.toString(), Entity.class)));

			                         callback.listed(elemsInCollection);
		                         });
	}

	static {
//		Finder.registerTemplate("QUERY_GETTING_TWO_GROUPS")
//		      .templateField(Entity.FIELD_APP_ID).is()
//		      .anyOf(TemplateField.field(""))
	}

}
