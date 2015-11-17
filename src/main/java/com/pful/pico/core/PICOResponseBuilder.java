package com.pful.pico.core;

import io.vertx.core.http.HttpServerResponse;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * PICOResponseBuilder set the HttpServerResponse to have a common field for PICO service.
 */
public class PICOResponseBuilder
{
	/**
	 * HttpServerResponse of Vert.x
	 */
	private HttpServerResponse vertxResponse;

	/**
	 * A constructor
	 * @param vertxResponse A response object provided by Vert.x
	 */
	public PICOResponseBuilder(final HttpServerResponse vertxResponse)
	{
		this.vertxResponse = vertxResponse;
	}

	/**
	 * @return HttpServerResponse instance
	 * @throws RuntimeException
	 */
	public HttpServerResponse forSuccess()
	{
		return forSuccess(200);
	}

	/**
	 * @param httpStatusCode A status code for HTTP protocol that should be in a range between 200 and 299
	 * @return HttpServerResponse instance
	 * @throws RuntimeException
	 */
	public HttpServerResponse forSuccess(final int httpStatusCode)
	{
		checkArgument(httpStatusCode >= 200 && httpStatusCode < 300,
		              "httpStatusCode for a success should be in a range between 200 and 299.");

		vertxResponse.setStatusCode(httpStatusCode);
		return vertxResponse;
	}

	/**
	 * @param httpStatusCode A status code for HTTP protocol that should be in a range between 200 and 299
	 * @param errorCodeDetail A more specific error code against the parameter 'httpStatusCode'
	 * @return HttpServerResponse instance
	 * @throws RuntimeException
	 */
	public HttpServerResponse forFailure(final int httpStatusCode, final PICOErrorCode errorCodeDetail)
	{
		checkArgument(httpStatusCode >= 400 && httpStatusCode < 600,
		              "httpStatusCode for a failure should be in a range between 400 and 599.");

		return forFailure(httpStatusCode, errorCodeDetail, null);
	}

	/**
	 * @param httpStatusCode A status code for HTTP protocol that should be in a range between 200 and 299
	 * @param errorCodeDetail A more specific error code against the parameter 'httpStatusCode'
	 * @param errorDescription A null-able description for the parameter 'errorCodeDetail'.
	 * @return HttpServerResponse instance
	 * @throws RuntimeException
	 */
	public HttpServerResponse forFailure(final int httpStatusCode, final PICOErrorCode errorCodeDetail, final String errorDescription)
	{
		checkArgument(httpStatusCode >= 400 && httpStatusCode < 600,
		              "httpStatusCode for a failure should be in a range between 400 and 599.");

		vertxResponse.setStatusCode(httpStatusCode)
		             .putHeader("PICO-Error-Code", errorCodeDetail.toString());

		if (errorDescription != null) {
			vertxResponse.putHeader("PICO-Error-Description", errorDescription);
		}

		return vertxResponse;
	}
}
