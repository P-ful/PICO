package com.pful.pico.db.querybuilder;

public class Field
{
	public static FieldOperation field(final String name)
	{
		return new FieldOperation(name);
	}
}