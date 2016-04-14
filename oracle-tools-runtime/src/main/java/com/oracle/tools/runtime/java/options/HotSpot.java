/*
 * File: HotSpot.java
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

package com.oracle.tools.runtime.java.options;

import com.oracle.tools.Options;

import java.util.Collections;

/**
 * A Helper class for creating {@link JvmOption}s for HotSpot-based Java Virtual Machines.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class HotSpot
{
    /**
     * The {@link HotSpot} Mode (ie: -server or -client)
     */
    public enum Mode implements JvmOption
    {
        SERVER,
        CLIENT;

        @Override
        public Iterable<String> resolve(Options options)
        {
            return Collections.singletonList("-" + this.name().toLowerCase());
        }


        @Override
        public String toString()
        {
            return "HotSpot.Mode{" + this.name().toLowerCase() + "}";
        }
    }
}