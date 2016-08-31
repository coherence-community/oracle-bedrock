/*
 * File: ParentApplication.java
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

package classloader.applications;

import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.java.JavaApplication;
import com.oracle.bedrock.runtime.java.options.ClassName;
import com.oracle.bedrock.runtime.java.options.SystemProperty;
import com.oracle.bedrock.runtime.options.Console;
import com.oracle.bedrock.runtime.options.DisplayName;
import com.oracle.bedrock.runtime.options.Orphanable;

import java.io.IOException;

/**
 * An application that starts a {@link ChildApplication}.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class ParentApplication
{
    /**
     * Entry Point of the Application.
     *
     * @param arguments
     */
    public static void main(String[] arguments) throws IOException, InterruptedException
    {
        System.out.printf("%s started\n", ParentApplication.class.getName());

        System.out.printf("server.address  : %s\n", System.getProperty("server.address"));
        System.out.printf("server.port     : %s\n", System.getProperty("server.port"));
        System.out.printf("orphan.children : %s\n", System.getProperty("orphan.children"));

        try (JavaApplication application = LocalPlatform.get().launch(JavaApplication.class,
                                                                      DisplayName.of("client"),
                                                                      ClassName.of(ChildApplication.class),
                                                                      SystemProperty.of("server.address",
                                                                                        System.getProperty("server.address")),
                                                                      SystemProperty.of("server.port",
                                                                                        System.getProperty("server.port")),
                                                                      Orphanable.enabled(Boolean.getBoolean("orphan.children")),
                                                                      Console.system()))
        {
            application.waitFor();
        }
    }
}
