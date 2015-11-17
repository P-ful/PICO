package com.pful.pico;

import com.pful.pico.db.MongoDB;
import com.pful.pico.http.EntityCRUDHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;

public class Service
		extends AbstractVerticle
{

	public static final String CONNECTION_STRING = "mongodb://104.155.221.148:8282";
	public static final String DB_NAME = "pico";
	public static MongoClient mongoClient;

	@Override
	public void start()
	{
		init(vertx);

		final Router router = Router.router(vertx);

		EntityCRUDHandler.installRouters(router);

		vertx.createHttpServer()
		     .requestHandler(router::accept)
		     .listen(8080);
	}


	private void init(final Vertx vertx)
	{
		mongoClient = MongoDB.getInstance(vertx, new JsonObject().put("connection_string", CONNECTION_STRING)
		                                                         .put("db_name", DB_NAME));
	}

}
