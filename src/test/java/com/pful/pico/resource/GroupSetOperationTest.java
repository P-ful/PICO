package com.pful.pico.resource;

import com.pful.pico.Service;
import com.pful.pico.core.PICOErrorCode;
import com.pful.pico.db.MongoDB;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static org.hamcrest.core.Is.is;

/**
 * ----------------------------------
 * group0 [elem0, elem1, elem2, elem3]
 * group1 [elem2, elem3, elem4]
 * group2 [elem0, elem3]
 * ----------------------------------
 * the above data set is created as follows:
 * entity0 [entity3, entity4, entity5, entity6]
 * entity1 [entity5, entity7, entity7]
 * entity2 [entity3, entity6]
 * <p>
 * Created by youngdocho on 12/8/15.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GroupSetOperationTest
{
	private static boolean isElemSettingDone;
	private static Vertx vertx = Vertx.vertx();

	private static List<String> GROUP_LIST = new ArrayList<>();
	private static List<String> ELEMS_IN_GROUP0 = new ArrayList<>();
	private static List<String> ELEMS_IN_GROUP1 = new ArrayList<>();
	private static List<String> ELEMS_IN_GROUP2 = new ArrayList<>();

	@BeforeClass
	public static void makeTestGroup()
	{
		final CountDownLatch latch = new CountDownLatch(3);

		try {
			vertx.deployVerticle(Service.class.getName(),
			                     res -> {
				                     try {
					                     TestGroupMaker group1 = new TestGroupMaker(latch);
					                     new Thread(group1).start();
					                     Thread.sleep(500);

					                     TestGroupMaker group2 = new TestGroupMaker(latch);
					                     new Thread(group2).start();
					                     Thread.sleep(500);

					                     TestGroupMaker group3 = new TestGroupMaker(latch);
					                     new Thread(group3).start();
					                     Thread.sleep(500);

				                     }
				                     catch (InterruptedException e) {
					                     e.printStackTrace();
				                     }
			                     });

			latch.await();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	@AfterClass
	public static void closeMongodb()
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
	public void makeTestElems()
	{
		final CountDownLatch latch = new CountDownLatch(5);

		final List<String> elemList0 = Arrays.asList(GROUP_LIST.get(0), GROUP_LIST.get(2));
		final List<String> elemList1 = Arrays.asList(GROUP_LIST.get(0));
		final List<String> elemList2 = Arrays.asList(GROUP_LIST.get(0), GROUP_LIST.get(1));
		final List<String> elemList3 = Arrays.asList(GROUP_LIST.get(0), GROUP_LIST.get(1), GROUP_LIST.get(2));
		final List<String> elemList4 = Arrays.asList(GROUP_LIST.get(1));

		if (!isElemSettingDone) {
			try {
				final TestGroupElementMaker elem0 = new TestGroupElementMaker(latch, elemList0);
				final TestGroupElementMaker elem1 = new TestGroupElementMaker(latch, elemList1);
				final TestGroupElementMaker elem2 = new TestGroupElementMaker(latch, elemList2);
				final TestGroupElementMaker elem3 = new TestGroupElementMaker(latch, elemList3);
				final TestGroupElementMaker elem4 = new TestGroupElementMaker(latch, elemList4);

				new Thread(elem0).start();
				new Thread(elem1).start();
				new Thread(elem2).start();
				new Thread(elem3).start();
				new Thread(elem4).start();

				latch.await();
				isElemSettingDone = true;
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Test
	public void test1Union()
			throws Exception
	{
		final CountDownLatch latch = new CountDownLatch(1);
		final PICOErrorCode[] errorCodePassed = new PICOErrorCode[1];
		final Set[] resultInSetPassed = new Set[1];

		GroupSetOperation.union(TestConstants.VALUE_APP_ID, GROUP_LIST.get(0), GROUP_LIST.get(1),
		                        (errorCode, result) -> {
			                        errorCodePassed[0] = errorCode;
			                        resultInSetPassed[0] = result;
			                        latch.countDown();
		                        });
		latch.await();

		testSuccess(errorCodePassed[0], resultInSetPassed[0]);
		Assert.assertThat(resultInSetPassed[0].size(), is(5));
		Assert.assertTrue(resultInSetPassed[0].containsAll(ELEMS_IN_GROUP0));
		Assert.assertTrue(resultInSetPassed[0].containsAll(ELEMS_IN_GROUP1));
		System.out.println("Union: " + resultInSetPassed[0]);
	}

	private void testSuccess(final PICOErrorCode picoErrorCode, final Set resultInSet)
	{
		Assert.assertEquals(PICOErrorCode.Success, picoErrorCode);
		Assert.assertNotNull(resultInSet);
	}

	@Test
	public void test2Intersection()
			throws Exception
	{
		final CountDownLatch latch = new CountDownLatch(1);
		final PICOErrorCode[] errorCodePassed = new PICOErrorCode[1];
		final Set[] resultInSetPassed = new Set[1];

		GroupSetOperation.intersection(TestConstants.VALUE_APP_ID, GROUP_LIST.get(0), GROUP_LIST.get(1),
		                               (errorCode, result) -> {
			                               errorCodePassed[0] = errorCode;
			                               resultInSetPassed[0] = result;
			                               latch.countDown();
		                               });
		latch.await();
		testSuccess(errorCodePassed[0], resultInSetPassed[0]);
		Assert.assertThat(resultInSetPassed[0].size(), is(2));
		System.out.println("Interaction: " + resultInSetPassed[0]);
	}

	@Test
	public void test3Difference1()
			throws Exception
	{
		final CountDownLatch latch = new CountDownLatch(1);

		final PICOErrorCode[] errorCodePassed = new PICOErrorCode[1];
		final Set[] resultInSetPassed = new Set[1];

		GroupSetOperation.difference(TestConstants.VALUE_APP_ID, GROUP_LIST.get(1), GROUP_LIST.get(2),
		                             (errorCode, result) -> {
			                             errorCodePassed[0] = errorCode;
			                             resultInSetPassed[0] = result;
			                             latch.countDown();
		                             });
		latch.await();
		testSuccess(errorCodePassed[0], resultInSetPassed[0]);
		Assert.assertThat(resultInSetPassed[0].size(), is(2));
		System.out.println("Difference1: " + resultInSetPassed[0]);

	}

	@Test
	public void test3Difference2()
			throws Exception
	{
		final CountDownLatch latch = new CountDownLatch(1);

		final PICOErrorCode[] errorCodePassed = new PICOErrorCode[1];
		final Set[] resultInSetPassed = new Set[1];

		GroupSetOperation.difference(TestConstants.VALUE_APP_ID, GROUP_LIST.get(2), GROUP_LIST.get(1),
		                             (errorCode, result) -> {
			                             errorCodePassed[0] = errorCode;
			                             resultInSetPassed[0] = result;
			                             latch.countDown();
		                             });
		latch.await();
		testSuccess(errorCodePassed[0], resultInSetPassed[0]);
		Assert.assertThat(resultInSetPassed[0].size(), is(1));
		System.out.println("Difference2: " + resultInSetPassed[0]);

	}

	@Test
	public void test4Subset1()
			throws Exception
	{
		final CountDownLatch latch = new CountDownLatch(1);

		final PICOErrorCode[] errorCodePassed = new PICOErrorCode[1];
		final boolean[] resultInBoolPassed = new boolean[1];

		GroupSetOperation.subset(TestConstants.VALUE_APP_ID, GROUP_LIST.get(0), GROUP_LIST.get(2),
		                         (errorCode, result) -> {
			                         errorCodePassed[0] = errorCode;
			                         resultInBoolPassed[0] = result;
			                         latch.countDown();
		                         });

		latch.await();
		Assert.assertThat(errorCodePassed[0], is(PICOErrorCode.Success));
		Assert.assertThat(resultInBoolPassed[0], is(true));
		System.out.println("Subset1: " + resultInBoolPassed[0]);
	}

	@Test
	public void test4Subset2()
			throws Exception
	{
		final CountDownLatch latch = new CountDownLatch(1);

		final PICOErrorCode[] errorCodePassed = new PICOErrorCode[1];
		final boolean[] resultInBoolPassed = new boolean[1];

		GroupSetOperation.subset(TestConstants.VALUE_APP_ID, GROUP_LIST.get(2), GROUP_LIST.get(0),
		                         (errorCode, result) -> {
			                         errorCodePassed[0] = errorCode;
			                         resultInBoolPassed[0] = result;
			                         latch.countDown();
		                         });

		latch.await();
		Assert.assertThat(errorCodePassed[0], is(PICOErrorCode.Success));
		Assert.assertThat(resultInBoolPassed[0], is(false));
		System.out.println("Subset2: " + resultInBoolPassed[0]);
	}

	static class TestGroupMaker
			implements Runnable
	{
		private final long createdAt = Instant.now()
		                                      .getEpochSecond();
		private final JsonObject entity = new JsonObject().put(Entity.FIELD_APP_ID, TestConstants.VALUE_APP_ID)
		                                                  .put(Entity.FIELD_TYPE, TestConstants.VALUE_TYPE)
		                                                  .put(Entity.FIELD_CREATED_AT, createdAt)
		                                                  .put(Entity.FIELD_UPDATED_AT, createdAt);
		private CountDownLatch latch = null;

		TestGroupMaker(final CountDownLatch latch)
		{
			this.latch = latch;
		}

		@Override
		public void run()
		{
			MongoDB.mongoClientSingleton.save(MongoDB.COLLECTION_ENTITIES, entity,
			                                  resInserted -> {
				                                  GROUP_LIST.add(resInserted.result());
				                                  latch.countDown();
			                                  });

		}
	}

	static class TestGroupElementMaker
			implements Runnable
	{
		private final long createdAt = Instant.now()
		                                      .getEpochSecond();
		private final JsonObject entity = new JsonObject().put(Entity.FIELD_APP_ID, TestConstants.VALUE_APP_ID)
		                                                  .put(Entity.FIELD_TYPE, TestConstants.VALUE_TYPE)
		                                                  .put(Entity.FIELD_CREATED_AT, createdAt)
		                                                  .put(Entity.FIELD_UPDATED_AT, createdAt);
		private CountDownLatch latch = null;
		private List<String> groupList = new ArrayList<>();

		TestGroupElementMaker(final CountDownLatch latch, final List<String> groupList)
		{
			this.latch = latch;
			this.groupList.addAll(groupList);
			entity.put(GroupManipulation.FIELD_GROUPS, this.groupList);
		}

		@Override
		public void run()
		{
			MongoDB.mongoClientSingleton.save(MongoDB.COLLECTION_ENTITIES, entity,
			                                  resInserted -> {
				                                  if (this.groupList.contains(GROUP_LIST.get(0))) {
					                                  ELEMS_IN_GROUP0.add(resInserted.result());
				                                  }
				                                  if (this.groupList.contains(GROUP_LIST.get(1))) {
					                                  ELEMS_IN_GROUP1.add(resInserted.result());
				                                  }
				                                  if (this.groupList.contains(GROUP_LIST.get(2))) {
					                                  ELEMS_IN_GROUP2.add(resInserted.result());
				                                  }
				                                  latch.countDown();
			                                  });
		}
	}

}
