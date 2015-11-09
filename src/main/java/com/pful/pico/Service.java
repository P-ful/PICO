package com.pful.pico;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;

public class Service extends AbstractVerticle {

    @Override
    public void start() {

         vertx.createHttpServer().requestHandler(req -> {
             req.response().end("hello.");
         }).listen(8080);
    }

}
