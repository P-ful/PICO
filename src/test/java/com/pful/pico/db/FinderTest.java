package com.pful.pico.db;

import com.pful.pico.Service;
import io.vertx.core.Vertx;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static com.pful.pico.db.Finder.Field.field;

/**
 * Created by daeyeon on 11/20/15.
 */
public class FinderTest
{
	private static Vertx vertx;

	@BeforeClass
	public static void startMongoDB()
	{
		vertx = Vertx.vertx();
		vertx.deployVerticle(Service.class.getName());
	}

	@AfterClass
	public static void stopMongoDb()
	{
		vertx.close();
	}

	@Test
	public void simpleQueryTest()
	{
		final CountDownLatch latch = new CountDownLatch(1);

		final Finder builder = new Finder();
		builder.field("name").is("111")
		       .inCollection("Entities")
		       .execute((results) -> {
			       System.out.println(results.result());
			       latch.countDown();
		       });

		try {
			latch.await();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void simpleQueryTest2()
	{
		Finder.open()
		      .allOf(
				      field("a").is("F"),
				      field("b").is("V"))
		      .and()
		      .field("").is("A")
		      .field("c").is("G")
		      .inCollection("")
		      .execute(result -> {

		      });
	}

	@Test
	public void simpleQueryTest3()
	{
		Finder.open()
		      .inCollection("")
		      .execute(result -> {
		      });
	}

	@Test
	public void simpleQueryTest4() // valid queries cannot be created.
	{
		Finder.open()
		      .allOf(
				      field("A").is("a")
		      )
		      .inCollection("")
		      .execute(result -> {
		      });
	}

	@Test
	public void simpleQueryTest5()
	// weird queries, such as { $or : [ { $and : [{}, {}] } ] }
	// can be created as follows. developer's fault?
	{
		Finder.open()
		      .allOf(
				      field("").is(""),
				      field("").is("")
		      )
		      .or() // .and()
		      .inCollection("")
		      .execute(result -> {
		      });
	}

	
//	@Test
//	public void allOfQueryTest()
//	{
//		final Finder builder = new Finder();
//		builder.allOf()
//		       .field("key1").is("value1")
//		       .field("key2").is("value2")
//		       .inCollection("A")
//		       .execute((results) -> {
//
//		       });
//	}
//
//	@Test
//	public void anyOfQueryTest()
//	{
//		final Finder builder = new Finder();
//		builder.anyOf()
//		       .field("key1").is("value1")
//		       .field("key2").is("value2")
//		       .inCollection("A")
//		       .execute((results) -> {
//
//		       });
//	}
}