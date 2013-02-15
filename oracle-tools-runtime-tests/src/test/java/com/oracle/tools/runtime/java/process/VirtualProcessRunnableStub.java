/*
 * File: VirtualProcessRunnableStub.java
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

package com.oracle.tools.runtime.java.process;

import java.util.ArrayList;
import java.util.List;

/**
 * A Virtual Process Stub.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class VirtualProcessRunnableStub
{
    /**
     * Field description
     */
    public static final String METHOD_STATIC_START = "staticStart";

    /**
     * Field description
     */
    public static final String METHOD_STATIC_START_NO_ARGS = "staticStartNoArgs";

    /**
     * Field description
     */
    public static final String METHOD_STATIC_STOP = "staticStop";

    /**
     * Field description
     */
    public static final String METHOD_START = "start";

    /**
     * Field description
     */
    public static final String METHOD_START_NO_ARGS = "startNoArgs";

    /**
     * Field description
     */
    public static final String METHOD_STOP = "stop";

    /**
     * Field description
     */
    public static final List<String> methodsCalled = new ArrayList<String>();

    /**
     * Field description
     */
    public static Object result;

    /**
     * Field description
     */
    public static String[] argsUsed;


    /**
     * Method description
     *
     * @param args
     *
     * @return
     */
    public static Object staticStart(String[] args)
    {
        methodsCalled.add(METHOD_STATIC_START);
        argsUsed = args;

        return result;
    }


    /**
     * Method description
     *
     * @return
     */
    public static Object staticStartNoArgs()
    {
        methodsCalled.add(METHOD_STATIC_START_NO_ARGS);

        return result;
    }


    /**
     * Method description
     *
     * @return
     */
    public static Object staticStop()
    {
        methodsCalled.add(METHOD_STATIC_STOP);

        return result;
    }


    /**
     * Method description
     *
     * @param args
     *
     * @return
     */
    public Object start(String[] args)
    {
        methodsCalled.add(METHOD_START);
        argsUsed = args;

        return result;
    }


    /**
     * Method description
     *
     * @return
     */
    public Object startNoArgs()
    {
        methodsCalled.add(METHOD_START_NO_ARGS);

        return result;
    }


    /**
     * Method description
     *
     * @return
     */
    public Object stop()
    {
        methodsCalled.add(METHOD_STOP);

        return result;
    }
}
