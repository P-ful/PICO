package com.pful.pico.db;

import org.junit.Test;

/**
 * Created by daeyeon on 11/20/15.
 */
public class FinderTest
{
	@Test
	public void simpleQueryTest()
	{
		final Finder builder = new Finder();
		builder.field("A").is("a")
		       .inCollection("A")
		       .execute((results) -> {

		       });
	}

	@Test
	public void allOfQueryTest()
	{
		final Finder builder = new Finder();
		builder.allOf()
		       .field("key1").is("value1")
		       .field("key2").is("value2")
		       .inCollection("A")
		       .execute((results) -> {

		       });
	}

	@Test
	public void anyOfQueryTest()
	{
		final Finder builder = new Finder();
		builder.anyOf()
		       .field("key1").is("value1")
		       .field("key2").is("value2")
		       .inCollection("A")
		       .execute((results) -> {

		       });
	}
}