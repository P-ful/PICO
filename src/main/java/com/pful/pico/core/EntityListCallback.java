package com.pful.pico.core;

import java.util.List;

/**
 * EntityListCallback is used to give back the list of entities
 */
public interface EntityListCallback
{
	/**
	 * @param errorCode
	 * @param entityList
	 */
	void listed(final PICOErrorCode errorCode, final String type, final List<Entity> entityList);
}
