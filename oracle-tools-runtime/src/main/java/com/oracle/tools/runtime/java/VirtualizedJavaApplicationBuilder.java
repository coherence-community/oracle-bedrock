/*
 * File: VirtualizedJavaApplicationBuilder.java
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

import com.oracle.tools.runtime.ApplicationConsole;
import com.oracle.tools.runtime.java.process.JavaProcessBuilder;
import com.oracle.tools.runtime.java.process.VirtualProcessBuilder;

/**
 * An {@link VirtualizedJavaApplicationBuilder} is a {@link JavaApplicationBuilder}
 * that realizes {@link JavaApplication}s as pseudo virtual processes, in that
 * they are run in the Java Virtual Machine hosting said builder.
 * <p>
 * Isolation of the pseudo process occurs through the use of a
 * {@link VirtualProcessBuilder} that of which uses a specializes child-first
 * class loading technique.
 * <p>
 * <strong>Caution:</strong> Care should be taken using this
 * {@link JavaApplicationBuilder} as all classes are re-loaded in the Perm
 * Generation.  Without a large Perm Generation, out-of-memory exceptions may be
 * thrown.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class VirtualizedJavaApplicationBuilder<A extends JavaApplication<A>, S extends JavaApplicationSchema<A, S>>
    extends AbstractJavaApplicationBuilder<A, S> implements JavaApplicationBuilder<A, S>
{
    /**
     * Constructs a {@link VirtualizedJavaApplicationBuilder}.
     */
    public VirtualizedJavaApplicationBuilder()
    {
        super();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected JavaProcessBuilder createJavaProcessBuilder(S                  schema,
                                                          String             applicationName,
                                                          ApplicationConsole console)
    {
        return new VirtualProcessBuilder(applicationName,
                                         schema.getApplicationClassName(),
                                         schema.getStartMethodName(),
                                         schema.getStopMethodName());
    }
}
