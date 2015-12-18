package com.pful.pico.resource;

import com.pful.pico.core.PICOErrorCode;

/**
 * Created by youngdocho on 12/8/15.
 */
public interface GroupLogicalSetOperationCallback
{
	void manipulated(final PICOErrorCode errorCode, final boolean isTrue);
}
