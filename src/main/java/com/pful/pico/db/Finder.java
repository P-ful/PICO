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

	public BinaryConnector allOf(final JsonObject... objects)
	{
		return new BinaryConnector();
	}

	public BinaryConnector anyOf(final JsonObject... objects)
	{
		return new BinaryConnector();
	}

	public BinaryConnector noneOf(final JsonObject... objects)
	{
		return new BinaryConnector();
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

		// comparison operators
		TReturn eq(final Object value);

		TReturn ne(final Object value);

		TReturn gt(final Number value);

		TReturn gte(final Number value);

		TReturn lt(final Number value);

		TReturn lte(final Number value);

		TReturn in(final Object... values);

		TReturn nin(final Object... values);

		//element operators
		TReturn exists(final boolean value);
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
		public JsonObject eq(final Object value)
		{
			return null;
		}

		@Override
		public JsonObject gt(final Number value)
		{
			return null;
		}

		@Override
		public JsonObject gte(final Number value)
		{
			return null;
		}

		@Override
		public JsonObject lt(final Number value)
		{
			return null;
		}

		@Override
		public JsonObject lte(final Number value)
		{
			return null;
		}

		@Override
		public JsonObject ne(final Object value)
		{
			return null;
		}

		@Override
		public JsonObject in(final Object... values)
		{
			return null;
		}

		@Override
		public JsonObject nin(final Object... values)
		{
			return null;
		}

		@Override
		public JsonObject exists(final boolean value)
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

	public class BinaryConnector
	{
		public LogicalConnector and()
		{
			return new LogicalConnector();
		}

		public LogicalConnector or()
		{
			return new LogicalConnector();
		}

		public Executor inCollection(final String name)
		{
			return new Executor();
		}
	}

	public class LogicalConnector
	{
		public BinaryConnector allOf(final JsonObject... objects)
		{
			return new BinaryConnector();
		}

		public BinaryConnector anyOf(final JsonObject... connectors)
		{
			return new BinaryConnector();
		}

		public BinaryConnector noneOf(final JsonObject... connectors)
		{
			return new BinaryConnector();
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
		public Finder eq(final Object value)
		{
			return Finder.this;
		}

		@Override
		public Finder gt(final Number value)
		{
			return Finder.this;
		}

		@Override
		public Finder gte(final Number value)
		{
			return Finder.this;
		}

		@Override
		public Finder lt(final Number value)
		{
			return Finder.this;
		}

		@Override
		public Finder lte(final Number value)
		{
			return Finder.this;
		}

		@Override
		public Finder ne(final Object value)
		{
			return Finder.this;
		}

		@Override
		public Finder in(final Object... values)
		{
			return Finder.this;
		}

		@Override
		public Finder nin(final Object... values)
		{
			return Finder.this;
		}

		@Override
		public Finder exists(final boolean value)
		{
			return Finder.this;
		}
	}
}
