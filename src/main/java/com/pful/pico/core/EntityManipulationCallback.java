package com.pful.pico.core;

/**
 * EntityManipulationCallback is used to notify the result of any asynchronous CRUD operation provided by EntityLifeCycle interface.
 */
public interface EntityManipulationCallback
{
	void manipulated(final PICOErrorCode errorCode, final Entity entity);
}
