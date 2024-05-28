/*
 * File: MavenTest.java
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

package com.oracle.bedrock.maven;

import com.oracle.bedrock.testsupport.deferred.Eventually;
import com.oracle.bedrock.options.Diagnostics;
import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.console.CapturingApplicationConsole;
import com.oracle.bedrock.runtime.java.JavaApplication;
import com.oracle.bedrock.runtime.java.options.ClassName;
import com.oracle.bedrock.runtime.options.Console;
import org.junit.Test;

import static com.oracle.bedrock.deferred.DeferredHelper.invoking;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;

/**
 * Integration tests for the {@link Maven}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class MavenTest
{
    /**
     * Ensure that {@link Maven} can resolve a single artifact (without a transitive dependency).
     */
    @Test
    public void shouldLaunchCoherenceCacheServer() throws Exception
    {
        LocalPlatform               platform = LocalPlatform.get();

        CapturingApplicationConsole console  = new CapturingApplicationConsole();

        try (JavaApplication application = platform.launch(JavaApplication.class,
                                                           ClassName.of("com.tangosol.net.Coherence"),
                                                           Maven.artifact("com.oracle.coherence.ce",
                                                                          "coherence",
                                                                          "21.12.4"),
                                                           Console.of(console),
                                                           Diagnostics.enabled()))
        {
            Eventually.assertThat(invoking(console).getCapturedErrorLines(), hasItem(containsString("21.12")));
        }
    }
}
