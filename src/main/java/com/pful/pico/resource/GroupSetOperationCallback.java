package com.pful.pico.resource;

import com.pful.pico.core.PICOErrorCode;

import java.util.Set;

/**
 * Created by youngdocho on 12/8/15.
 */
interface GroupSetOperationCallback
{
	void manipulated(final PICOErrorCode errorCode, final Set result);
}
