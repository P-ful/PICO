package com.pful.pico.resource;

import com.pful.pico.core.PICOErrorCode;

import java.util.List;

/**
 * EntityListCallback is used to give back the list of entities
 */
public interface EntityListCallback
{
	/**
	 * @param errorCode error-code
	 * @param type An entity type in a string
	 * @param entityList a list of the entities in the type
	 */
	void listed(final PICOErrorCode errorCode, final String type, final List<Entity> entityList);
}
