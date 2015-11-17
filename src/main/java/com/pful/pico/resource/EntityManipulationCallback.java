package com.pful.pico.resource;

import com.pful.pico.core.PICOErrorCode;

/**
 * EntityManipulationCallback is used to notify the result of any asynchronous CRUD operation provided by EntityLifeCycle interface.
 */
public interface EntityManipulationCallback
{
	/**
	 * @param errorCode error-code
	 * @param entity A manipulated entity
	 */
	void manipulated(final PICOErrorCode errorCode, final Entity entity);
}
