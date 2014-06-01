/*
 * File: FluentJavaApplicationSchema.java
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

package com.oracle.tools.runtime.java;

import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.ApplicationConsole;
import com.oracle.tools.runtime.ApplicationSchema;
import com.oracle.tools.runtime.FluentApplicationSchema;

import java.util.Properties;

/**
 * A {@link FluentJavaApplicationSchema} is a Java specific {@link ApplicationSchema}.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @param <A>  the type of {@link JavaApplication} that can be configured by the {@link FluentJavaApplicationSchema}
 * @param <S>  the type of {@link FluentJavaApplicationSchema} that will be returned from fluent methods
 */
public interface FluentJavaApplicationSchema<A extends JavaApplication, S extends FluentJavaApplicationSchema<A, S>>
    extends FluentApplicationSchema<A, S>,
            JavaApplicationSchema<A>
{
}
