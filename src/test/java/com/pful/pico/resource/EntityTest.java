package com.pful.pico.resource;

import com.google.gson.Gson;
import com.pful.pico.Service;
import com.pful.pico.core.ApplicationContext;
import com.pful.pico.core.PICOErrorCode;
import com.pful.pico.core.PICOException;
import com.pful.pico.db.MongoDB;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.bson.types.ObjectId;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Created by youngdocho on 11/18/15.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EntityTest
{
	private static final String PROPERTIES_IN_STRING = "{field0 : {sub_field0 : sub_value0, sub_field1: sub_value1}, field1 : {sub_field0 : sub_value0}}";
	private static final String TYPE_FOR_UPDATE = "update_test_type";
	private static final Map<String, Object> PROPERTIES_FOR_UPDATE = new HashMap<>();
	private static Map<String, Object> PROPERTIES =
			new Gson().fromJson(PROPERTIES_IN_STRING, new HashMap<String, Object>().getClass());

	private static Vertx vertx = Vertx.vertx();
	private static Entity entityBound;
	private static ApplicationContext context =
			new ApplicationContext(TestConstants.VALUE_APP_ID, TestConstants.VALUE_APP_TOKEN);

	@BeforeClass
	public static void setUpBefore()
			throws InterruptedException
	{
		final CountDownLatch latch = new CountDownLatch(1);

		vertx.deployVerticle(Service.class.getName(), res -> {
			latch.countDown();
		});

		latch.await();
	}

	@AfterClass
	public static void closeAfter()
			throws InterruptedException
	{
		final CountDownLatch latch = new CountDownLatch(1);

		MongoDB.mongoClientSingleton.remove(MongoDB.COLLECTION_ENTITIES, new JsonObject(), res -> {
			if (res.succeeded()) {
				latch.countDown();
			}
		});

		latch.await();
		vertx.close();
	}

	@Before
	public void before()
			throws Exception
	{
	}

	@After
	public void after()
			throws Exception
	{
	}

	/**
	 * Method: create(final ApplicationContext context, final String type, final Map<String, Object> properties, final EntityManipulationCallback callback)
	 */
	@Test(timeout = 5000)
	public void test1create()
			throws Exception
	{
		final CountDownLatch latch = new CountDownLatch(1);
		final PICOErrorCode[] errorCodePassed = new PICOErrorCode[1];
		final Entity[] entityPassed = new Entity[1];

		try {
			Entity.create(context, TestConstants.VALUE_TYPE, PROPERTIES, (errorCode, entity) -> {
				try {
					errorCodePassed[0] = errorCode;
					entityPassed[0] = entity;
				}
				catch (Exception e) {
					throw e;
				}
				finally {
					latch.countDown();
				}
			});

			latch.await();

			testSuccess(errorCodePassed[0], entityPassed[0]);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void testSuccess(final PICOErrorCode picoErrorCode, final Entity entity)
	{
		Assert.assertEquals(PICOErrorCode.Success, picoErrorCode);
		Assert.assertNotNull(entity);
		Assert.assertNotNull(entity.getId());
		Assert.assertNotNull(entity.getAppId());
		Assert.assertNotNull(entity.getType());
		Assert.assertNotNull(entity.getProperties());
		Assert.assertNotNull(entity.getCreatedAt());
		Assert.assertNotNull(entity.getUpdatedAt());
		Assert.assertEquals(TestConstants.VALUE_APP_ID, entity.getAppId());
	}

	@Test
	public void test2readExistingId()
	{
		final CountDownLatch latch = new CountDownLatch(1);
		final Entity[] entityPassed = new Entity[1];
		final PICOErrorCode[] errorCodePassed = new PICOErrorCode[1];

		try {
			MongoDB.mongoClientSingleton.find(MongoDB.COLLECTION_ENTITIES, new JsonObject().put("app_id", TestConstants.VALUE_APP_ID),
			                                  res -> {
				                                  if (res.succeeded()) {
					                                  final String id = String.valueOf(res.result()
					                                                                      .get(0)
					                                                                      .getString("_id"));
					                                  try {
						                                  Entity.read(context, id,
						                                              (errorCode, entity) -> {
							                                              entityBound = entity;

							                                              errorCodePassed[0] = errorCode;
							                                              entityPassed[0] = entity;

							                                              latch.countDown();
						                                              });
					                                  }
					                                  catch (PICOException e) {
						                                  e.printStackTrace();
					                                  }
				                                  }
			                                  });
			latch.await();

			testSuccess(errorCodePassed[0], entityPassed[0]);
			Assert.assertEquals(TestConstants.VALUE_TYPE, entityPassed[0].getType());
			Assert.assertEquals(PROPERTIES, entityPassed[0].getProperties());
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method: read(final ApplicationContext context, final String id, final EntityManipulationCallback callback)
	 */
	@Test
	public void test3readNonExistingId()
	{
		final CountDownLatch latch = new CountDownLatch(1);
		final Entity[] entityPassed = new Entity[1];
		final PICOErrorCode[] errorCodePassed = new PICOErrorCode[1];

		try {
			Entity.read(context, new ObjectId().toString(), (errorCode, entity) -> {
				try {
					errorCodePassed[0] = errorCode;
					entityPassed[0] = entity;
					latch.countDown();
				}
				catch (Exception e) {
					throw e;
				}
			});

			latch.await();

			testNotFound(errorCodePassed[0], entityPassed[0]);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		catch (PICOException e) {
			e.printStackTrace();
		}
	}

	private void testNotFound(final PICOErrorCode picoErrorCode, final Entity entity)
	{
		Assert.assertEquals(PICOErrorCode.NotFound, picoErrorCode);
		Assert.assertEquals(null, entity);
	}

	@Test
	public void test4update()
			throws Exception
	{
		final CountDownLatch latch = new CountDownLatch(1);
		final Entity[] entityPassed = new Entity[1];
		final PICOErrorCode[] errorCodePassed = new PICOErrorCode[1];

		try {
			entityBound.setType(TYPE_FOR_UPDATE);
			entityBound.setProperties(PROPERTIES_FOR_UPDATE);
			entityBound.update(
					(errorCode, entity) -> {
						try {
							errorCodePassed[0] = errorCode;
							entityPassed[0] = entity;

							System.out.println(entity);
							latch.countDown();
						}
						catch (Exception e) {
							throw e;
						}
					});

			latch.await();

			testSuccess(errorCodePassed[0], entityPassed[0]);
			Assert.assertEquals(TYPE_FOR_UPDATE, entityPassed[0].getType());
			Assert.assertEquals(PROPERTIES_FOR_UPDATE, entityPassed[0].getProperties());
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test5delete()
			throws Exception
	{
		final CountDownLatch latch = new CountDownLatch(1);
		final Entity[] entityPassed = new Entity[1];
		final PICOErrorCode[] errorCodePassed = new PICOErrorCode[1];

		try {
			entityBound.delete(
					(errorCode, entity) -> {
						try {
							errorCodePassed[0] = errorCode;
							entityPassed[0] = entity;

							System.out.println(entity);
							latch.countDown();
						}
						catch (Exception e) {
							throw e;
						}
					});

			latch.await();

			testSuccess(errorCodePassed[0], entityPassed[0]);
			Assert.assertEquals(PICOErrorCode.Success, errorCodePassed[0]);
			Assert.assertNotNull(entityPassed[0].getId());
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
