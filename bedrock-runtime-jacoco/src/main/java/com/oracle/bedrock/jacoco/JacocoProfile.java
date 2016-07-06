/*
 * File: JacocoProfile.java
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

package com.oracle.bedrock.jacoco;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.Application;
import com.oracle.bedrock.runtime.MetaClass;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.Profile;
import com.oracle.bedrock.runtime.java.ClassPath;
import com.oracle.bedrock.runtime.java.JavaApplication;
import com.oracle.bedrock.runtime.java.options.JavaAgent;
import org.jacoco.agent.rt.RT;

import java.util.concurrent.CompletableFuture;

/**
 * The Java Code Coverage (JaCoCo) {@link Profile}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class JacocoProfile implements Profile, Option
{
    /**
     * The parameters provided to the {@link JacocoProfile}.
     */
    private String parameters;


    /**
     * Constructs a {@link JacocoProfile}.
     *
     * @param parameters   the parameters provided to the {@link JacocoProfile}
     */
    @OptionsByType.Default
    public JacocoProfile(String parameters)
    {
        this.parameters = parameters;
    }


    @Override
    public void onLaunching(Platform      platform,
                            MetaClass     metaClass,
                            OptionsByType optionsByType)
    {
        if (metaClass != null
            && JavaApplication.class.isAssignableFrom(metaClass.getImplementationClass(platform, optionsByType)))
        {
            try
            {
                // determine the classpath of the JaCoCo runtime agent jar (should be something like jacocoagent-x.y.z.jar)
                ClassPath jacocoPath = ClassPath.ofClass(RT.class);

                // define a JavaAgent for JaCoCo
                JavaAgent javaAgent = JavaAgent.using(jacocoPath.toString(), parameters);

                optionsByType.add(javaAgent);
            }
            catch (Exception e)
            {
            }
        }
    }


    @Override
    public void onLaunched(Platform      platform,
                           Application   application,
                           OptionsByType optionsByType)
    {
        // there's nothing to after an application has been realized
    }


    @Override
    public void onClosing(Platform      platform,
                          Application   application,
                          OptionsByType optionsByType)
    {
        // prior to closing a JavaApplication we request the JaCoCo telemetry to be dumped
        if (application instanceof JavaApplication)
        {
            JavaApplication         javaApplication = (JavaApplication) application;

            CompletableFuture<Void> future          = javaApplication.submit(new Dump());

            // Wait for the Dump to complete
            future.join();
        }
    }
}
