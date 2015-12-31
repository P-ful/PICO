package com.pful.pico.db.querybuilder;

/**
 * Created by daeyeon on 12/28/15.
 */
interface TemplateValueOperator<TReturn>
{
	TReturn is();

	TReturn is(final String variableName);

	TReturn eq();

	TReturn eq(final String variableName);

	TReturn ne();

	TReturn ne(final String variableName);

	TReturn gt();

	TReturn gt(final String variableName);

	TReturn gte();

	TReturn gte(final String variableName);

	TReturn lt();

	TReturn lt(final String variableName);

	TReturn lte();

	TReturn lte(final String variableName);

	TReturn in();

	TReturn in(final String variableName);

	TReturn nin();

	TReturn nin(final String variableName);

	TReturn exists();

	TReturn exists(final String variableName);
}
