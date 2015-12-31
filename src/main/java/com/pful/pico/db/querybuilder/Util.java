package com.pful.pico.db.querybuilder;

import io.vertx.core.json.JsonArray;

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

}
