/*
 * Copyright (c) 2000, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.bedrock.testsupport.deferred;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static com.oracle.bedrock.deferred.DeferredHelper.invoking;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThrows;

public class TimeoutTest
{
    private int count = 0;

    @Before
    public void setup()
    {
        count = 0;
    }

    @Test
    public void shouldTimeout()
    {
        assertThrows(AssertionError.class,
                     () ->  Eventually.assertThat(invoking(this).getCount(1000), is(10), Eventually.within(5, TimeUnit.SECONDS)));
    }

    @Test
    public void shouldNotTimeout()
    {
        Eventually.assertThat(invoking(this).getCount(1000), is(3), Eventually.within(10, TimeUnit.SECONDS));
    }

    public int getCount(long cMillis)
    {
        try
        {
            count++;
            Thread.sleep(cMillis);
            return count;
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }
}
