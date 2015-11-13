package com.pful.pico.core;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class Response
{
	private static final String HEADER_FIELD_PICO_ERROR_CODE = "PICO-Error-Code";
	private static final String HEADER_FIELD_PICO_ERROR_DESCRIPTION = "PICO-Error-Description";

	public static void render(final PICOErrorCode errorCode, final JsonArray body, final HttpServerResponse response)
	{
		if (errorCode.equals(PICOErrorCode.Success)) {
			sendSuccess(body, response);
		}
		else {
			sendErrors(errorCode, response);
		}
	}

	private static void sendErrors(final PICOErrorCode errorCode, final HttpServerResponse response)
	{
		switch (errorCode) {
		case InternalError:
			sendError(500, response);
			break;
		case BadRequest:
			sendError(400, response);
			break;
		case Unauthorized:
			sendError(401, "Unauthorized", response);
			break;
		case NotFound:
			sendError(404, response);
			break;
		}
	}

	private static void sendError(final int errorCode, final HttpServerResponse response)
	{
		response.putHeader(HEADER_FIELD_PICO_ERROR_CODE, String.valueOf(errorCode))
		        .setStatusCode(errorCode)
		        .end();
	}

	private static void sendError(final int errorCode, final String errorDescription, final HttpServerResponse response)
	{
		response.putHeader(HEADER_FIELD_PICO_ERROR_CODE, String.valueOf(errorCode))
		        .putHeader(HEADER_FIELD_PICO_ERROR_DESCRIPTION, errorDescription)
		        .setStatusCode(errorCode)
		        .end();
	}

	private static void sendSuccess(final JsonArray body, final HttpServerResponse response)
	{
		response.end((body != null) ? body.encodePrettily() : null);
	}

	public static void render(final PICOErrorCode errorCode, final JsonObject body, final HttpServerResponse response)
	{
		if (errorCode.equals(PICOErrorCode.Success)) {
			sendSuccess(body, response);
		}
		else {
			sendErrors(errorCode, response);
		}
	}

	private static void sendSuccess(final JsonObject body, final HttpServerResponse response)
	{
		response.end((body != null) ? body.encodePrettily() : null);
	}

}
