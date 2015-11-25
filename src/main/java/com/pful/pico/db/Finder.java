package com.pful.pico.db;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Created by daeyeon on 11/20/15.
 */
public class Finder
{
	private String collectionName;
	private Map<String, Object> properties = new HashMap<>();

	private Stack<JsonObject> jsonObjectStack = new Stack<>();

	private JsonObject query = new JsonObject();

	public static Finder open()
	{
		return new Finder();
	}

	public BinaryConnector allOf(final JsonObject... jsonObjects)
	{
		checkArgument(jsonObjects.length > 0, "");

		final JsonArray jsonArray = new JsonArray();

		for (final JsonObject jsonObject : jsonObjects) {
			jsonArray.add(jsonObject);
		}

		query.put("$and", jsonArray);
		return new BinaryConnector();
	}

	public BinaryConnector anyOf(final JsonObject... jsonObjects)
	{
		for (final JsonObject jsonObject : jsonObjects) {

		}

		return new BinaryConnector();
	}

	public BinaryConnector noneOf(final JsonObject... jsonObjects)
	{
		for (final JsonObject jsonObject : jsonObjects) {

		}

		return new BinaryConnector();
	}

	public SimpleQueryConnector field(final String name)
	{
		return new SimpleQueryConnector(name);
	}

	public Executor inCollection(final String name)
	{
		this.collectionName = name;
		return new Executor();
	}

	interface Connector<TReturn>
	{
		TReturn is(final Object value);

		TReturn in(final Object value);
	}

	public static class Field
	{
		public static FieldConnector field(final String name)
		{
			return new FieldConnector(name);
		}
	}

	public static class FieldConnector
			implements Connector<JsonObject>
	{
		private String name;

		public FieldConnector(final String name)
		{
			this.name = name;
		}

		@Override
		public JsonObject is(final Object value)
		{
			return null;
		}

		@Override
		public JsonObject in(final Object value)
		{
			return null;
		}
	}

	public class BinaryConnector
	{
		public Finder and()
		{
			return Finder.this;
		}

		public Finder or()
		{
			return Finder.this;
		}
	}

	public class SimpleQueryConnector
			implements Connector<Finder>
	{
		private String name;

		public SimpleQueryConnector(final String name)
		{
			this.name = name;
		}

		@Override
		public Finder is(final Object value)
		{
			properties.put(name, value);
			return Finder.this;
		}

		@Override
		public Finder in(final Object value)
		{
			return Finder.this;
		}
	}

	public class Executor
	{
		public void execute(final Handler<AsyncResult<List<JsonObject>>> resultHandler)
		{
			final JsonObject query = new JsonObject();

			properties.forEach((key, value) -> {
				query.put(key, value);
			});

			MongoDB.mongoClientSingleton.find(collectionName, query, resultHandler);
		}
	}
}
