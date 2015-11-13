package com.pful.pico;

import io.vertx.core.Vertx;

public class PICO
{

	public static void main(String[] args)
	{
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(Service.class.getName());
	}
}
