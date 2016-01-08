package com.pful.pico.resource;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.pful.pico.Service;
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
	 * @param appId    application id
	 * @param group1   group id referencing an entity
	 * @param group2   group id referencing an entity
	 * @param callback
	 */
	public static void union(final String appId,
	                         final String group1,
	                         final String group2,
	                         final GroupSetOperationCallback callback)
	{
		checkArgument(!Strings.isNullOrEmpty(appId) && !Strings.isNullOrEmpty(group1) && !Strings.isNullOrEmpty(group2),
		              "appId and groups shouldn't be null or empty.");

//		Query -> { 'app_id' : <#app_id>, $or : [ { 'groups' : <#group_1> }, { 'groups' : <#group_2> }}]}

		final JsonObject query = Finder.newQuery()
		                               .field(Entity.FIELD_APP_ID).is(appId)
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
	 * @param appId    application id
	 * @param group1   group id referencing an entity
	 * @param group2   group id referencing an entity
	 * @param callback
	 */
	public static void intersection(final String appId,
	                                final String group1,
	                                final String group2,
	                                final GroupSetOperationCallback callback)
	{
		checkArgument(!Strings.isNullOrEmpty(appId) && !Strings.isNullOrEmpty(group1) && !Strings.isNullOrEmpty(group2),
		              "appId and groups shouldn't be null or empty.");

//		Query -> { 'app_id' : <#app_id>, 'groups' : { $all : [ <#group_1>, <#group_2> ]}}

		final JsonObject query = Finder.newQuery()
		                               .field(Entity.FIELD_APP_ID).is(appId)
		                               .field(GroupManipulation.FIELD_GROUPS).allInStrings(group1, group2)
		                               .toJson();

		performQueryAndDeliver(callback, query);
	}

	/**
	 * @param appId    application id
	 * @param group1   group id referencing an entity
	 * @param group2   group id referencing an entity
	 * @param callback
	 */
	public static void difference(final String appId,
	                              final String group1,
	                              final String group2,
	                              final GroupSetOperationCallback callback)
	{
		checkArgument(!Strings.isNullOrEmpty(appId) && !Strings.isNullOrEmpty(group1) && !Strings.isNullOrEmpty(group2),
		              "appId and groups shouldn't be null or empty.");

		// Query -> { '$and' : [ { 'groups' : <#group1> } , { 'groups' : { $ne : <#groups2> }}]}

		final JsonObject query = Finder.newQuery()
		                               .field(Entity.FIELD_APP_ID).is(appId)
		                               .allOf(field(GroupManipulation.FIELD_GROUPS).is(group1),
		                                      field(GroupManipulation.FIELD_GROUPS).ne(group2))
		                               .toJson();

		performQueryAndDeliver(callback, query);
	}

	/**
	 * @param appId    application id
	 * @param group1   group id referencing an entity
	 * @param group2   group id referencing an entity
	 * @param callback
	 */
	public static void subset(final String appId,
	                          final String group1,
	                          final String group2,
	                          final GroupLogicalSetOperationCallback callback)
	{
		checkArgument(!Strings.isNullOrEmpty(appId) && !Strings.isNullOrEmpty(group1) && !Strings.isNullOrEmpty(group2),
		              "appId and groups shouldn't be null or empty.");

		System.out.println(group1);
		System.out.println(group2);

		getElements(appId, group1,
		            entitiesInGroup1 -> {
			            getElements(appId, group2,
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
	 * @param appId application id
	 * @param group group id referencing an entity
	 * @return
	 */
	private static void getElements(final String appId,
	                                final String group,
	                                final GroupElementListCallback callback)
	{
		final JsonObject query = Finder.newQuery()
		                               .field(Entity.FIELD_APP_ID).is(appId)
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
