package com.pful.pico.resource;

import com.pful.pico.Service;
import com.pful.pico.core.PICOErrorCode;
import com.pful.pico.db.MongoDB;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.core.Is.is;

/*
 * Created by youngdocho on 12/4/15.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GroupManipulationTest
{
	private static String entityIdBeforeClass;
	private static List<String> entityIdsInBeforeMethod = new ArrayList<>();
	private static String targetEntityIdForCreateTest;
	private static Vertx vertx = Vertx.vertx();

	/**
	 * deploy a PICO Service and
	 * save a base entity without the groups field, whose _id is contained in entityIdBeforeClass,
	 * and an additional entity without it, which is going to be used in testing create()
	 *
	 * @throws InterruptedException
	 */
	@BeforeClass
	public static void setUpBefore()
			throws InterruptedException
	{
		final CountDownLatch latch = new CountDownLatch(1);

		final long createdAt = Instant.now()
		                              .getEpochSecond();
		final JsonObject entity = new JsonObject().put(Entity.FIELD_APP_ID, TestConstants.VALUE_APP_ID)
		                                          .put(Entity.FIELD_TYPE, TestConstants.VALUE_TYPE)
		                                          .put(Entity.FIELD_CREATED_AT, createdAt)
		                                          .put(Entity.FIELD_UPDATED_AT, createdAt);

		vertx.deployVerticle(Service.class.getName(), res -> {
			MongoDB.mongoClientSingleton.save(MongoDB.COLLECTION_ENTITIES, entity,
			                                  resInserted -> {
				                                  entityIdBeforeClass = resInserted.result();
//				                                  System.out.println(entityIdBeforeClass);

				                                  final long newCreatedAt = Instant.now()
				                                                                   .getEpochSecond();
				                                  final JsonObject newEntity = new JsonObject().put(Entity.FIELD_APP_ID,
				                                                                                    TestConstants.VALUE_APP_ID)
				                                                                               .put(Entity.FIELD_TYPE,
				                                                                                    TestConstants.VALUE_TYPE)
				                                                                               .put(Entity.FIELD_CREATED_AT, newCreatedAt)
				                                                                               .put(Entity.FIELD_UPDATED_AT, newCreatedAt);

				                                  MongoDB.mongoClientSingleton.save(MongoDB.COLLECTION_ENTITIES, newEntity,
				                                                                    resultInserted -> {
					                                                                    if (resInserted.failed()) {
						                                                                    System.out.println(resultInserted.cause());
						                                                                    Assert.fail();
						                                                                    System.exit(1);
					                                                                    }
					                                                                    targetEntityIdForCreateTest = resultInserted.result();
//					                                                                    System.out.println(resultInserted.result());
					                                                                    latch.countDown();
				                                                                    });
			                                  });
		});

		latch.await();
	}

	/**
	 * remove all the documents created while testing.
	 *
	 * @throws InterruptedException
	 */
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

	/**
	 * save a new entity with the groups field
	 * whenever before starting a new test.
	 * The created _ids are added in the list, entityIdsInBeforeMethod.
	 *
	 * @throws Exception
	 */
	@Before
	public void before()
			throws Exception
	{
		final CountDownLatch latch = new CountDownLatch(1);
		final long createdAt = Instant.now()
		                              .getEpochSecond();
		final JsonObject entity = new JsonObject().put(Entity.FIELD_APP_ID, TestConstants.VALUE_APP_ID)
		                                          .put(Entity.FIELD_TYPE, TestConstants.VALUE_TYPE)
		                                          .put(Entity.FIELD_CREATED_AT, createdAt)
		                                          .put(Entity.FIELD_UPDATED_AT, createdAt)
		                                          .put(GroupManipulation.FIELD_GROUPS, new ArrayList(Arrays.asList(entityIdBeforeClass)));

		vertx.deployVerticle(Service.class.getName(), res -> {
			MongoDB.mongoClientSingleton.save(MongoDB.COLLECTION_ENTITIES, entity,
			                                  resInserted -> {
				                                  if (resInserted.failed()) {
					                                  System.out.println(resInserted.cause());
					                                  Assert.fail();
					                                  System.exit(1);
				                                  }
				                                  entityIdsInBeforeMethod.add(resInserted.result());
				                                  latch.countDown();
			                                  });
		});

		latch.await();
	}

	@After
	public void after()
			throws Exception
	{
	}

	/**
	 * create a group
	 * targetEntityId : targetEntityIdForCreateTest
	 * groupEntityId to be added : entityIdBeforeClass
	 *
	 * @throws Exception
	 */
	@Test
	public void test01create()
			throws Exception
	{
		final CountDownLatch latch = new CountDownLatch(1);
		final PICOErrorCode[] errorCodePassed = new PICOErrorCode[1];
		final JsonObject[] resultPassed = new JsonObject[1];

		GroupManipulation.create(TestConstants.VALUE_APP_ID, targetEntityIdForCreateTest, entityIdBeforeClass,
		                         (errorCode, result) -> {
			                         errorCodePassed[0] = errorCode;
			                         resultPassed[0] = result;
			                         latch.countDown();
		                         });

		latch.await();

		Assert.assertEquals(PICOErrorCode.Success, errorCodePassed[0]);
//		System.out.println(resultPassed[0]);
		Assert.assertNotNull(resultPassed[0]);
	}

	/**
	 * read all the list of the groups in the app.
	 * it should return the json object result in the format of {groups: [id...]}.
	 *
	 * @throws Exception
	 */
	@Test
	public void test02getAllGroupsInApp()
			throws Exception
	{
		final CountDownLatch latch = new CountDownLatch(1);
		final PICOErrorCode[] errorCodePassed = new PICOErrorCode[1];
		final JsonObject[] resultPassed = { new JsonObject() };

		GroupManipulation.read(TestConstants.VALUE_APP_ID,
		                       (errorCode, result) -> {
			                       errorCodePassed[0] = errorCode;
			                       resultPassed[0] = result;

			                       latch.countDown();
		                       });
		latch.await();

		List<String> groupList = getList(GroupManipulation.FIELD_GROUPS, resultPassed[0]);
		Assert.assertEquals(PICOErrorCode.Success, errorCodePassed[0]);
		Assert.assertThat(groupList, hasItem(entityIdBeforeClass));
	}

	private List<String> getList(final String field, final JsonObject in)
	{
		List<String> list = new ArrayList<>();

		JsonArray array = in.getJsonArray(field);
		System.out.println(field);
		System.out.println(array);
		for (Object s : array) {
			list.add((String) s);
		}
		return list;
	}

	/**
	 * read all the list of the ids contained in a specified group.
	 * it should return the json object result in the format of {elemsInGroup: [id...]}.
	 *
	 * @throws Exception
	 */
	@Test
	public void test03readAllEntitiesContainedInAGroup()
			throws Exception
	{
		final CountDownLatch latch = new CountDownLatch(1);
		final PICOErrorCode[] errorCodePassed = new PICOErrorCode[1];
		final JsonObject[] resultPassed = new JsonObject[1];

		GroupManipulation.readElems(TestConstants.VALUE_APP_ID, entityIdBeforeClass,
		                            (errorCode, result) -> {
			                            errorCodePassed[0] = errorCode;
			                            resultPassed[0] = result;

			                            latch.countDown();
		                            });

		latch.await();

		List<String> elemsInGroup = getList("elemsInGroup", resultPassed[0]);
		Assert.assertEquals(PICOErrorCode.Success, errorCodePassed[0]);
		Assert.assertThat(elemsInGroup, hasItem(entityIdsInBeforeMethod.get(0)));
		Assert.assertThat(elemsInGroup, hasItem(entityIdsInBeforeMethod.get(1)));
		Assert.assertThat(elemsInGroup, hasItem(entityIdsInBeforeMethod.get(2)));

	}

	/**
	 * read a group list in a single entity with no group.
	 * it should return {groups : []}.
	 *
	 * @throws Exception
	 */
	@Test
	public void test05readGroupsInSingleEntity()
			throws Exception
	{
		final CountDownLatch latch = new CountDownLatch(1);
		final PICOErrorCode[] errorCodePassed = new PICOErrorCode[1];
		final JsonObject[] resultPassed = new JsonObject[1];

		GroupManipulation.read(TestConstants.VALUE_APP_ID, entityIdBeforeClass,
		                       (errorCode, result) -> {
			                       errorCodePassed[0] = errorCode;
			                       resultPassed[0] = result;

			                       latch.countDown();
		                       });

		latch.await();

		List<String> groups = getList("groups", resultPassed[0]);
		Assert.assertEquals(PICOErrorCode.Success, errorCodePassed[0]);
		Assert.assertThat(groups.size(), is(0));
	}

	/**
	 * read a group list in a single entity which has one group.
	 * it should return {groups: [id of entityIdBeforeClass]}
	 *
	 * @throws Exception
	 */
	@Test
	public void test06readGroupsInSingleEntity()
			throws Exception
	{
		final CountDownLatch latch = new CountDownLatch(1);
		final PICOErrorCode[] errorCodePassed = new PICOErrorCode[1];
		final JsonObject[] resultPassed = new JsonObject[1];

//		System.out.println(entityIdsInBeforeMethod.get(4));
		GroupManipulation.read(TestConstants.VALUE_APP_ID, entityIdsInBeforeMethod.get(4),
		                       (errorCode, result) -> {
			                       errorCodePassed[0] = errorCode;
			                       resultPassed[0] = result;

			                       latch.countDown();
		                       });

		latch.await();

		List<String> groups = getList("groups", resultPassed[0]);
		Assert.assertEquals(PICOErrorCode.Success, errorCodePassed[0]);
		Assert.assertThat(groups, hasItem(entityIdBeforeClass));
		Assert.assertThat(groups.size(), is(1));
	}

	/**
	 * update all entities that contains entityIdBeforeClass as a group
	 * by replacing it with new id.
	 * total 7 documents should be updated.
	 * result format
	 * {ok:1, nModified: int, n: int}
	 *
	 * @throws Exception
	 */
	@Test
	public void test07updateGroup()
			throws Exception
	{
		final CountDownLatch latch = new CountDownLatch(1);
		final PICOErrorCode[] errorCodePassed = new PICOErrorCode[1];
		final JsonObject[] resultPassed = new JsonObject[1];
		final String newGroup = entityIdsInBeforeMethod.get(entityIdsInBeforeMethod.size() - 1);

		System.out.println("original: " + entityIdBeforeClass);
		System.out.println("new: " + newGroup);

		GroupManipulation.update(TestConstants.VALUE_APP_ID, entityIdBeforeClass, newGroup,
		                         (errorCode, result) -> {
			                         errorCodePassed[0] = errorCode;
			                         resultPassed[0] = result;

			                         latch.countDown();
		                         });

		latch.await();

		Assert.assertSame(PICOErrorCode.Success, errorCodePassed[0]);
		Assert.assertNotNull(resultPassed[0]);
		Assert.assertThat(resultPassed[0].getInteger("nModified"), is(7));
		Assert.assertThat(resultPassed[0].getInteger("n"), is(7));
	}

	/**
	 * update a specified entity's group
	 * It returns the updated document.
	 *
	 * @throws Exception
	 */
	@Test
	public void test08updateGroupInEntity()
			throws Exception
	{
		final CountDownLatch latch = new CountDownLatch(1);
		final PICOErrorCode[] errorCodePassed = new PICOErrorCode[1];
		final JsonObject[] resultPassed = new JsonObject[1];
		final String targetEntityId = entityIdsInBeforeMethod.get(entityIdsInBeforeMethod.size() - 1);

		GroupManipulation.update(TestConstants.VALUE_APP_ID, targetEntityId, entityIdBeforeClass, targetEntityIdForCreateTest,
		                         (errorCode, result) -> {
			                         errorCodePassed[0] = errorCode;
			                         resultPassed[0] = result;

			                         latch.countDown();
		                         });

		latch.await();

	/*	System.out.println("update group in an entity" +
				                   "\nwhere: " + targetEntityId +
				                   "\nold : " + entityIdBeforeClass +
				                   "\nnew : " + targetEntityIdForCreateTest);*/

		List<String> groupList = getList(GroupManipulation.FIELD_GROUPS, resultPassed[0]);
		Assert.assertSame(PICOErrorCode.Success, errorCodePassed[0]);
		Assert.assertThat(groupList, hasItem(targetEntityIdForCreateTest));
	}

	/**
	 * delete a specified group through the application.
	 * <p>
	 * the result format
	 * { ok: 1, nModified: int , n: int}
	 *
	 * @throws Exception
	 */
	@Test
	public void test09deleteGroupInApp()
			throws Exception
	{
		final CountDownLatch latch = new CountDownLatch(1);
		final PICOErrorCode[] errorCodePassed = new PICOErrorCode[1];
		final JsonObject[] resultPassed = new JsonObject[1];
		GroupManipulation.delete(TestConstants.VALUE_APP_ID, entityIdBeforeClass,
		                         (errorCode, result) -> {
			                         errorCodePassed[0] = errorCode;
			                         resultPassed[0] = result;

			                         latch.countDown();
		                         });

		latch.await();

		Assert.assertEquals(PICOErrorCode.Success, errorCodePassed[0]);
		Assert.assertThat(resultPassed[0].getInteger("nModified"), is(1));
		Assert.assertThat(resultPassed[0].getInteger("n"), is(1));
	}

	/**
	 * delete a group in an entity.
	 * <p>
	 * the result format
	 * {"updatedExisting":true,"n":1}
	 *
	 * @throws Exception
	 */
	@Test
	public void test10deleteGroupInAnEntity()
			throws Exception
	{
		final CountDownLatch latch = new CountDownLatch(1);
		final PICOErrorCode[] errorCodePassed = new PICOErrorCode[1];
		final JsonObject[] resultPassed = new JsonObject[1];
		final String targetEntityId = entityIdsInBeforeMethod.get(entityIdsInBeforeMethod.size() - 1);
		GroupManipulation.delete(TestConstants.VALUE_APP_ID, targetEntityId, entityIdBeforeClass,
		                         (errorCode, result) -> {
			                         errorCodePassed[0] = errorCode;
			                         resultPassed[0] = result;

			                         latch.countDown();
		                         });

		latch.await();

		Assert.assertEquals(PICOErrorCode.Success, errorCodePassed[0]);
		Assert.assertNotNull(resultPassed[0]);
		Assert.assertThat(resultPassed[0].getBoolean("updatedExisting"), is(true));
		Assert.assertThat(resultPassed[0].getInteger("n"), is(1));
	}

}