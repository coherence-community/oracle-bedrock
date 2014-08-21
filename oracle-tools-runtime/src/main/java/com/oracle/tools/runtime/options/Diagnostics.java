/*
 * File: Diagnostics.java
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

package com.oracle.tools.runtime.options;

import com.oracle.tools.Option;

import com.oracle.tools.runtime.Settings;

/**
 * An {@link Option} to define the level of Oracle Tools runtime diagnostics.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Diagnostics implements Option
{
    /**
     * Is diagnostics output enabled?
     */
    private boolean enabled;


    /**
     * Privately construct a {@link Diagnostics}
     *
     * @param enabled  is diagnostics enabled
     */
    private Diagnostics(boolean enabled)
    {
        this.enabled = enabled;
    }


    /**
     * Determines if {@link Diagnostics} is enabled.
     *
     * @return  <code>true</code> if {@link Diagnostics} is enabled
     */
    public boolean isEnabled()
    {
        return enabled;
    }


    /**
     * Constructs a {@link Diagnostics} being enabled.
     *
     * @return  an enabled {@link Diagnostics}
     */
    public static Diagnostics enabled()
    {
        return new Diagnostics(true);
    }


    /**
     * Constructs a {@link Diagnostics} being disabled.
     *
     * @return  a disabled {@link Diagnostics}
     */
    public static Diagnostics disabled()
    {
        return new Diagnostics(false);
    }


    /**
     * Constructs a {@link Diagnostics}.
     *
     * @param  enabled  should diagnostics be enabled?
     *
     * @return  a {@link Diagnostics}
     */
    public static Diagnostics enabled(boolean enabled)
    {
        return new Diagnostics(enabled);
    }


    /**
     * Constructs a {@link Diagnostics} based on auto-detected configuration.
     *
     * @return  a {@link Diagnostics}
     */
    public static Diagnostics autoDetect()
    {
        return new Diagnostics(Settings.isDiagnosticsEnabled(false));
    }
}
