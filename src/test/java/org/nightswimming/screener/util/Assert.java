package org.nightswimming.screener.util;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import org.hamcrest.MatcherAssert;

public class Assert {
	
	public static <T> void assertThrown(Runnable thrower, Class<T> exceptionClsss){
		Throwable thrown = null;
	    try { thrower.run(); } catch (Throwable e) {
	    	thrown = e;
	    }
	    MatcherAssert.assertThat(thrown, instanceOf(exceptionClsss));   
    }
 }
