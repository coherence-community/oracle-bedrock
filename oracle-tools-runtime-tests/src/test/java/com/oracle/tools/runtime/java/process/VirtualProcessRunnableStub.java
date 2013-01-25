/*
 * File: VirtualProcessRunnableStub.java
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * - Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Neither the name of Oracle nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
     *Field description 
     */
    public static final String METHOD_STATIC_START = "staticStart";

    /** 
     *Field description 
     */
    public static final String METHOD_STATIC_START_NO_ARGS = "staticStartNoArgs";

    /** 
     *Field description 
     */
    public static final String METHOD_STATIC_STOP = "staticStop";

    /** 
     *Field description 
     */
    public static final String METHOD_START = "start";

    /** 
     *Field description 
     */
    public static final String METHOD_START_NO_ARGS = "startNoArgs";

    /** 
     *Field description 
     */
    public static final String METHOD_STOP = "stop";

    /** 
     *Field description 
     */
    public static final List<String> methodsCalled = new ArrayList<String>();

    /** 
     *Field description 
     */
    public static Object result;

    /** 
     *Field description 
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
