package com.pful.pico.resource;

import com.google.common.base.Strings;
import com.pful.pico.Service;
import com.pful.pico.core.PICOErrorCode;
import com.pful.pico.db.MongoDB;
import com.pful.pico.db.querybuilder.Finder;
import io.vertx.core.json.JsonObject;

import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Created by youngdocho on 12/8/15.
 */
public class GroupSetOperation
{
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

		getElements(appId, group1,
		            set1 -> {
			            getElements(appId, group2,
			                        set2 -> {
				                        if (set1 == null || set2 == null) {
					                        callback.manipulated(PICOErrorCode.BadRequest, null);
					                        return;
				                        }
				                        final Set union = new HashSet(set1);
				                        union.addAll(set2);
				                        callback.manipulated(PICOErrorCode.Success, union);
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

			                         final Set<String> elemsInSet = new HashSet<>();

			                         res.result()
			                            .stream()
			                            .forEach(e -> elemsInSet.add(e.getString(Entity.FIELD_ID)));

			                         callback.listed(elemsInSet);
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


		getElements(appId, group1,
		            set1 -> {
			            getElements(appId, group2,
			                        set2 -> {
				                        if (set1 == null || set2 == null) {
					                        callback.manipulated(PICOErrorCode.BadRequest, null);
					                        return;
				                        }

				                        final Set intersection = new HashSet(set1);
				                        intersection.retainAll(set2);
				                        callback.manipulated(PICOErrorCode.Success, intersection);
			                        });
		            });
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

		getElements(appId, group1,
		            set1 -> {
			            getElements(appId, group2,
			                        set2 -> {
				                        if (set1 == null || set2 == null) {
					                        callback.manipulated(PICOErrorCode.BadRequest, null);
					                        return;
				                        }

				                        final Set difference = new HashSet(set1);
				                        difference.removeAll(set2);
				                        callback.manipulated(PICOErrorCode.Success, difference);
			                        });
		            });
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

		getElements(appId, group1,
		            set1 -> {
			            getElements(appId, group2,
			                        set2 -> {
				                        if (set1 == null || set2 == null) {
					                        callback.manipulated(PICOErrorCode.BadRequest, false);
					                        return;
				                        }

				                        callback.manipulated(PICOErrorCode.Success, set1.containsAll(set2));
			                        });
		            });
	}

}
