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
	public void queryTest()
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
	public void queryTest2()
	{
		Finder.open()
		      .allOf(
				      field("a").is("F"),
				      field("b").is("V"))
		      .and()
		      .field("").is("A")
		      .and()
		      .field("c").is("G")
		      .inCollection("")
		      .execute(results -> {

		      });
	}

	@Test
	public void queryTest3()
	{
		Finder.open()
		      .inCollection("")
		      .execute(result -> {

		      });
	}

	@Test
	public void queryTest4()
	{
		Finder.open()
		      .allOf(
				      field("A").is("a")
		      )
		      .inCollection("")
		      .execute(results -> {
		      });


	}

	@Test
	public void queryTest5()
	{
		//{ $and: [ { price: { $ne: 1.99 } }, { price: { $exists: true } } ] }
		Finder.open()
		      .allOf(
				      field("price").ne(1.99),
				      field("price").exists(true)
		      )
		      .inCollection("")
		      .execute(result -> {

		      });

		Finder.open()
		      .field("price")
		      .ne(1.99)
		      .and()
		      .field("price")
		      .exists(true)
		      .inCollection("")
		      .execute(results -> {

		      });
	}

	@Test
	public void queryTest6()
	{
		// { $and : [{ qty : {$in : [5, 15]}}, { tags : { $in : ["appliance", "school"]}}]}
		Finder.open()
		      .field("qty")
		      .in(5, 15)
		      .and()
		      .field("tags")
		      .in("appliances", "school")
		      .inCollection("A")
		      .execute(results -> {

		      });
	}

	@Test
	public void queryTest7()
	{
		// price: { $not: { $gt: 1.99 } } }
		Finder.open()
		      .field("price")
		      .lte(1.99)
		      .inCollection("A")
		      .execute(results -> {

		      });
	}

	@Test
	public void queryTest8()
	{
		// { $and : [
		//  { $or : [ { price : 0.99 }, { price : 1.99 } ] },
		//  { $or : [ { sale : true }, { qty : { $lt : 20 } } ] }
		//  ]}
		Finder.open()
		      .anyOf(
				      field("price").is(0.99),
				      field("price").is(1.99)
		      )
		      .and()
		      .anyOf(
				      field("sale").is(true),
				      field("qty").lt(20)
		      )
		      .inCollection("")
		      .execute(results -> {

		      });
	}

}