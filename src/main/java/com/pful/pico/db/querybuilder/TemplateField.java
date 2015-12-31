package com.pful.pico.db.querybuilder;

/**
 * Created by daeyeon on 12/28/15.
 */
public class TemplateField
{
	public static TemplateFieldOperation field(final String name)
	{
		return new TemplateFieldOperation(name);
	}
}
