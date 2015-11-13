package com.pful.pico;

import com.pful.pico.core.MongoDB;
import com.pful.pico.core.RequestRouter;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class Service
		extends AbstractVerticle
{

	public static final String CONNECTION_STRING = "mongodb://localhost:27017";
	public static final String DB_NAME = "pico";
	public static MongoClient mongoClient;

	@Override
	public void start()
	{

		init(vertx);

		Router router = Router.router(vertx);

		router.route().handler(BodyHandler.create());
		router.post("/entities/:type").handler(RequestRouter::create);
		router.get("/entities/:id").handler(RequestRouter::read);
		router.put("/entities/:id").handler(RequestRouter::update);
		router.delete("/entities/:id").handler(RequestRouter::delete);
		router.get("/:type/list").handler(RequestRouter::list);

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
