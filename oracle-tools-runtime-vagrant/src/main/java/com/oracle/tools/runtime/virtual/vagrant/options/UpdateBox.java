/*
 * File: UpdateBox.java
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * The contents of this file are subject to the terms and conditions of
 * the Common Development and Distribution License 1.0 (the "License").
 *
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the License by consulting the LICENSE.txt file
 * distributed with this file, or by consulting https://oss.oracle.com/licenses/CDDL
 *
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file LICENSE.txt.
 *
 * MODIFICATIONS:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 */

package com.oracle.tools.runtime.virtual.vagrant.options;

import com.oracle.tools.Option;
import com.oracle.tools.Options;

/**
 * An {@link Option} to specify a Vagrant Box should up updated prior to starting.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class UpdateBox implements Option
{
    /**
     * Is updating the Vagrant Box enabled prior to starting?
     */
    private boolean enabled;


    /**
     * Constructs a {@link UpdateBox} with a specific setting.
     *
     * @param enabled  is updating enabled?
     */
    private UpdateBox(boolean enabled)
    {
        this.enabled = enabled;
    }


    /**
     * Determine if updating is enabled?
     *
     * @return  <code>true</code> if enabled, <code>false</code> otherwise
     */
    public boolean isEnabled()
    {
        return enabled;
    }


    /**
     * Obtains an {@link UpdateBox} based on the specified value.
     *
     * @param update  if updating is enabled
     *
     * @return  an {@link UpdateBox} for the specified value
     */
    public static UpdateBox enabled(boolean update)
    {
        return new UpdateBox(update);
    }


    /**
     * Obtains an {@link UpdateBox} such that {@link UpdateBox#isEnabled()}
     * returns <code>true</code>.
     *
     * @return  an {@link UpdateBox}
     */
    public static UpdateBox yes()
    {
        return enabled(true);
    }


    /**
     * Obtains an {@link UpdateBox} such that {@link UpdateBox#isEnabled()}
     * returns <code>false</code>.
     *
     * @return  an {@link UpdateBox}
     */
    @Options.Default
    public static UpdateBox no()
    {
        return enabled(false);
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof UpdateBox))
        {
            return false;
        }

        UpdateBox that = (UpdateBox) o;

        return enabled == that.enabled;

    }


    @Override
    public int hashCode()
    {
        return (enabled ? 1 : 0);
    }
}
