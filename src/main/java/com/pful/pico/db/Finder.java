package com.pful.pico.db;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

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

	public SimpleQuery allOf(final JsonObject... objects)
	{
		return new SimpleQuery();
	}

	public SimpleQuery anyOf(final JsonObject... objects)
	{
		return new SimpleQuery();
	}

	public SimpleQuery noneOf(final JsonObject... objects)
	{
		return new SimpleQuery();
	}

	public ValueConnector field(final String name)
	{
		return new ValueConnector(name);
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
		public static JsonConnector field(final String name)
		{
			return new JsonConnector(name);
		}
	}

	public static class JsonConnector
			implements Connector<JsonObject>
	{
		private String name;

		public JsonConnector(final String name)
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

	public static class Executor
	{
		public void execute(final Handler<AsyncResult<List<JsonObject>>> resultHandler)
		{
			final JsonObject query = new JsonObject();

//			properties.forEach((key, value) -> {
//				query.put(key, value);
//			});

//			MongoDB.mongoClientSingleton.find(collectionName, query, resultHandler);
		}
	}

	public class SimpleQuery
	{
		public ConnectionConnector and()
		{
			return new ConnectionConnector();
		}

		public ConnectionConnector or()
		{
			return new ConnectionConnector();
		}

		public Executor inCollection(final String name)
		{
			return new Executor();
		}
	}

	public class ConnectionConnector
	{
		public SimpleQuery allOf(final JsonObject... objects)
		{
			return new SimpleQuery();
		}

		public SimpleQuery anyOf(final JsonObject... connectors)
		{
			return new SimpleQuery();
		}

		public SimpleQuery noneOf(final JsonObject... connectors)
		{
			return new SimpleQuery();
		}

		public ValueConnector field(final String name)
		{
			return new ValueConnector(name);
		}
	}

	public class ValueConnector
			implements Connector<Finder>
	{
		private String name;

		public ValueConnector(final String name)
		{
			this.name = name;
		}

		@Override
		public Finder is(final Object value)
		{
			return Finder.this;
		}

		@Override
		public Finder in(final Object value)
		{
			return Finder.this;
		}
	}
}
