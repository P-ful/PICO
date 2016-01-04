package com.pful.pico.db.querybuilder;

import java.util.Collection;

/**
 * Created by daeyeon on 12/28/15.
 */
public interface ValueOperator<TReturn>
{
	TReturn is(final String value);

	TReturn is(final Number value);

	TReturn eq(final Number value);

	TReturn eq(final String value);

	TReturn ne(final Number value);

	TReturn ne(final String value);

	TReturn gt(final Number value);

	TReturn gte(final Number value);

	TReturn lt(final Number value);

	TReturn lte(final Number value);

	TReturn inStringCollection(final Collection<String> collection);

	TReturn inStringArray(final String[] strings);

	TReturn inStrings(final String... strings);

	TReturn inNumberCollection(final Collection<Number> collection);

	TReturn inNumberArray(final Number[] numbers);

	TReturn inNumbers(final Number... numbers);

	TReturn ninStringCollection(final Collection<String> collection);

	TReturn ninStringArray(final String[] strings);

	TReturn ninStrings(final String... strings);

	TReturn ninNumberCollection(final Collection<Number> collection);

	TReturn ninNumberArray(final Number[] numbers);

	TReturn ninNumbers(final Number... numbers);

	TReturn exists();

	TReturn allInStringArray(final String[] strings);

	TReturn allInStrings(final String... strings);

	TReturn allInStringCollection(final Collection<String> collection);

	TReturn allInNumberArray(final Number[] numbers);

	TReturn allInNumbers(final Number... numbers);

	TReturn allInNumberCollection(final Collection<Number> collection);
}
