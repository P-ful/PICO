package com.pful.pico.db.querybuilder;

import io.vertx.core.json.JsonArray;

import java.util.Collection;

/**
 * Created by daeyeon on 12/31/15.
 */
public class Util
{
	public static <T> JsonArray makeArrayToJsonArrayObject(final T[] array)
	{
		final JsonArray jsonArray = new JsonArray();
		for (final T v : array) {
			jsonArray.add(v);
		}
		return jsonArray;
	}

	public static <T> JsonArray makeCollectionToJsonArrayObject(final Collection<T> collection)
	{
		final JsonArray jsonArray = new JsonArray();
		collection.forEach(e -> jsonArray.add(e));
		return jsonArray;
	}

}
