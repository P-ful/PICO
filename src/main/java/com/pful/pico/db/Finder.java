package com.pful.pico.db;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by daeyeon on 11/20/15.
 */
public class Finder
{
	private String collectionName;
	private Map<String, Object> properties = new HashMap<>();

	public MultipleQuery allOf()
	{
		return new MultipleQuery();
	}

	public MultipleQuery anyOf()
	{
		return new MultipleQuery();
	}

	public SimpleQuery field(final String name)
	{
		return new SimpleQuery(name);
	}

	public class MultipleQuery
	{
		public MultipleValueSetter field(final String name)
		{
			return new MultipleValueSetter(name);
		}

		public void execute(final Handler<AsyncResult<List<JsonObject>>> resultHandler)
		{

		}

		public Executor inCollection(final String name)
		{
			return new Executor(null);
		}

		public class MultipleValueSetter
		{
			private String fieldName;

			public MultipleValueSetter(final String fieldName)
			{
				this.fieldName = fieldName;
			}

			public MultipleQuery is(final Object value)
			{
				return MultipleQuery.this;
			}
		}
	}

	public class SimpleQuery
	{

		private String name;

		public SimpleQuery(final String name)
		{
			this.name = name;
		}

		public Executor is(final Object value)
		{
			properties.put(name, value);
			return new Executor(null);
		}
	}

	public class Executor
	{
		private JsonObject query;

		public Executor(final JsonObject query)
		{
			this.query = query;
		}

		public void execute(final Handler<AsyncResult<List<JsonObject>>> resultHandler)
		{

		}

		public Executor inCollection(final String name)
		{
			return this;
		}
	}
}
