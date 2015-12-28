package com.pful.pico.core;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;

/**
 * Created by daeyeon on 11/16/15.
 */
public class PICOResponseBuilderTest
{

	@Test
	public void testForSuccess()
			throws Exception
	{
		final HttpServerResponse response = new PICOResponseBuilder(new DummyHttpServerResponse()).forSuccess();
		Assert.assertThat(response.getStatusCode(), is(200));
	}

	@Test
	public void testForSuccess1()
			throws Exception
	{
		final HttpServerResponse response = new PICOResponseBuilder(new DummyHttpServerResponse()).forSuccess(201);
		Assert.assertThat(response.getStatusCode(), is(201));
	}

	@Test
	public void testForFailure()
			throws Exception
	{
		final HttpServerResponse response = new PICOResponseBuilder(new DummyHttpServerResponse()).forFailure(404, PICOErrorCode.Unexpected);
		Assert.assertThat(response.getStatusCode(), is(404));
		Assert.assertThat(response.headers().get("PICO-Error-Code"), allOf(is(notNullValue()), is(PICOErrorCode.Unexpected.toString())));
		Assert.assertThat(response.headers().get("PICO-Error-Description"), is(nullValue()));
	}

	@Test
	public void testForFailure1()
			throws Exception
	{
		final String simpleErrorMessage = "Sample Error Message";

		final HttpServerResponse response = new PICOResponseBuilder(new DummyHttpServerResponse()).forFailure(404, PICOErrorCode.Unexpected, simpleErrorMessage);
		Assert.assertThat(response.getStatusCode(), is(404));
		Assert.assertThat(response.headers().get("PICO-Error-Code"), allOf(is(notNullValue()), is(PICOErrorCode.Unexpected.toString())));
		Assert.assertThat(response.headers().get("PICO-Error-Description"), allOf(is(notNullValue()), is(simpleErrorMessage)));
	}
}

class DummyHttpServerResponse
		implements HttpServerResponse
{
	private int statusCode;
	private MultiMap headers = MultiMap.caseInsensitiveMultiMap();

	@Override
	public HttpServerResponse exceptionHandler(final Handler<Throwable> handler)
	{
		return null;
	}

	@Override
	public HttpServerResponse write(final Buffer data)
	{
		return null;
	}

	@Override
	public HttpServerResponse setWriteQueueMaxSize(final int maxSize)
	{
		return null;
	}

	@Override
	public HttpServerResponse drainHandler(final Handler<Void> handler)
	{
		return null;
	}

	@Override
	public int getStatusCode()
	{
		return statusCode;
	}

	@Override
	public HttpServerResponse setStatusCode(final int statusCode)
	{
		this.statusCode = statusCode;
		return this;
	}

	@Override
	public String getStatusMessage()
	{
		return null;
	}

	@Override
	public HttpServerResponse setStatusMessage(final String statusMessage)
	{
		return null;
	}

	@Override
	public HttpServerResponse setChunked(final boolean chunked)
	{
		return null;
	}

	@Override
	public boolean isChunked()
	{
		return false;
	}

	@Override
	public MultiMap headers()
	{
		return headers;
	}

	@Override
	public boolean writeQueueFull()
	{
		return false;
	}

	@Override
	public HttpServerResponse putHeader(final String name, final String value)
	{
		headers.add(name, value);
		return this;
	}

	@Override
	public HttpServerResponse putHeader(final CharSequence name, final CharSequence value)
	{
		return putHeader(name.toString(), value.toString());
	}

	@Override
	public HttpServerResponse putHeader(final String name, final Iterable<String> values)
	{
		return putHeader(name, values.toString());
	}

	@Override
	public HttpServerResponse putHeader(final CharSequence name, final Iterable<CharSequence> values)
	{
		return putHeader(name.toString(), values.toString());
	}

	@Override
	public MultiMap trailers()
	{
		return null;
	}

	@Override
	public HttpServerResponse putTrailer(final String name, final String value)
	{
		return null;
	}

	@Override
	public HttpServerResponse putTrailer(final CharSequence name, final CharSequence value)
	{
		return null;
	}

	@Override
	public HttpServerResponse putTrailer(final String name, final Iterable<String> values)
	{
		return null;
	}

	@Override
	public HttpServerResponse putTrailer(final CharSequence name, final Iterable<CharSequence> value)
	{
		return null;
	}

	@Override
	public HttpServerResponse closeHandler(final Handler<Void> handler)
	{
		return null;
	}

	@Override
	public HttpServerResponse write(final String chunk, final String enc)
	{
		return null;
	}

	@Override
	public HttpServerResponse write(final String chunk)
	{
		return null;
	}

	@Override
	public HttpServerResponse writeContinue()
	{
		return null;
	}

	@Override
	public void end(final String chunk)
	{

	}

	@Override
	public void end(final String chunk, final String enc)
	{

	}

	@Override
	public void end(final Buffer chunk)
	{

	}

	@Override
	public void end()
	{

	}

	@Override
	public HttpServerResponse sendFile(final String filename, final long offset, final long length)
	{
		return null;
	}

	@Override
	public HttpServerResponse sendFile(final String filename,
	                                   final long offset,
	                                   final long length,
	                                   final Handler<AsyncResult<Void>> resultHandler)
	{
		return null;
	}

	@Override
	public void close()
	{

	}

	@Override
	public boolean ended()
	{
		return false;
	}

	@Override
	public boolean closed()
	{
		return false;
	}

	@Override
	public boolean headWritten()
	{
		return false;
	}

	@Override
	public HttpServerResponse headersEndHandler(final Handler<Void> handler)
	{
		return null;
	}

	@Override
	public HttpServerResponse bodyEndHandler(final Handler<Void> handler)
	{
		return null;
	}

	@Override
	public long bytesWritten()
	{
		return 0;
	}
}
