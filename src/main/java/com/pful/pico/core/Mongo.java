package com.pful.pico.core;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class Mongo
{

	public static final String COLLECTION_APPLICATION_CONTEXTS = "ApplicationContexts";
	public static final String COLLECTION_ENTITIES = "Entities";
	public static MongoClient mongoClientSingleton;

	public static MongoClient getInstance(final Vertx vertx, final JsonObject config)
	{
		if (mongoClientSingleton == null) {

			mongoClientSingleton = MongoClient.createShared(vertx, config);

			mongoClientSingleton.createCollection(COLLECTION_APPLICATION_CONTEXTS, response -> {
				if (response.succeeded()) {
					System.out.println(COLLECTION_APPLICATION_CONTEXTS + " has been created.");
				}
				else {
					System.out.println(response.cause().getMessage());
				}
			});

			mongoClientSingleton.createCollection(COLLECTION_ENTITIES, response -> {
				if (response.succeeded()) {
					System.out.println(COLLECTION_ENTITIES + " has been created.");
				}
				else {
					System.out.println(response.cause().getMessage());
				}
			});
		}
		return mongoClientSingleton;
	}
}
