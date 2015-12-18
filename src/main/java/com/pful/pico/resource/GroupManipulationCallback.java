package com.pful.pico.resource;

import com.pful.pico.core.PICOErrorCode;
import io.vertx.core.json.JsonObject;

/**
 * Created by youngdocho on 12/4/15.
 */
// TODO decide 2nd parameter what to be returned.
public interface GroupManipulationCallback
{
	void manipulated(final PICOErrorCode errorCode, final JsonObject result);
}
