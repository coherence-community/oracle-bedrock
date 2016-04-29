/*
 * File: AbstractFunctionalTest.java
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

package com.oracle.tools.runtime.docker;

import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.console.CapturingApplicationConsole;
import com.oracle.tools.runtime.console.Console;
import com.oracle.tools.runtime.docker.commands.Build;
import com.oracle.tools.runtime.docker.commands.Images;
import com.oracle.tools.runtime.docker.options.ImageCloseBehaviour;
import com.oracle.tools.runtime.options.Argument;
import com.oracle.tools.runtime.options.WorkingDirectory;
import org.junit.Assume;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Base class for functional tests.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class AbstractFunctionalTest
{
    /**
     * The name to use for the Java base image.
     */
    public static final String javaImageName = "oracletoolsjava:1.0";


    /**
     * Determine whether Docker Machine is installed.
     *
     * @return  true if Docker Machine is installed locally
     */
    public static boolean hasDockerMachine()
    {
        try (Application application = LocalPlatform.get().launch("docker-machine", Argument.of("--version")))
        {
            return application.waitFor() == 0;
        }
        catch (Exception e)
        {
            return false;
        }
    }


    public static void removeMachine(String name)
    {
        try (Application application = LocalPlatform.get().launch("docker-machine",
                                                                  Argument.of("rm"),
                                                                  Argument.of("--force"),
                                                                  Argument.of(name)))
        {
            application.waitFor();
        }
    }


    public static void killMachine(String name)
    {
        try (Application application = LocalPlatform.get().launch("docker-machine",
                                                                  Argument.of("kill"),
                                                                  Argument.of(name)))
        {
            application.waitFor();
        }
    }


    public static List<String> listMachines()
    {
        CapturingApplicationConsole console = new CapturingApplicationConsole();

        try (Application list = LocalPlatform.get().launch("docker-machine",
                                                           Argument.of("ls"),
                                                           Argument.of("--format"),
                                                           Argument.of("{{.Name}}"),
                                                           Console.of(console)))
        {
            assertThat(list.waitFor(), is(0));

            return console.getCapturedOutputLines().stream().filter((line) -> !"(terminated)".equals(line))
            .collect(Collectors.toList());
        }
    }


    public static String status(String name)
    {
        CapturingApplicationConsole console = new CapturingApplicationConsole();

        try (Application list = LocalPlatform.get().launch("docker-machine",
                                                           Argument.of("status"),
                                                           Argument.of(name),
                                                           Console.of(console)))
        {
            assertThat(list.waitFor(), is(0));

            return console.getCapturedOutputLines().poll();
        }
    }


    /**
     * Ensure that a Java image can be created on the target platform
     * <p>
     * This method requires the <code>oracle.tools.container.tests.jre</code>
     * System property to be set to point to the location of a Java 8 <strong>JRE</strong>
     * install for linux-x64 as a tar.gz file. This is typically what is downloaded
     * from Oracle's Java site.
     */
    public static void ensureJavaImage(Platform platform, TemporaryFolder temporaryFolder) throws Exception
    {
        // ----- use the Images command to list the images on the platform to see if the java image already exists -----
        CapturingApplicationConsole console = new CapturingApplicationConsole();
        String                      format  = Images.FORMAT_REPOSITORY + ':' + Images.FORMAT_TAG;

        try (Application images = platform.launch(Images.list().format(format), Console.of(console)))
        {
            if (images.waitFor() == 0)
            {
                boolean present = console.getCapturedOutputLines().stream()
                                        .filter((line) -> line.equals(javaImageName))
                                        .findFirst()
                                        .isPresent();

                if (present)
                {
                    return;
                }
            }
        }

        // ----- the image does not exist so we need to build it -----

        File installerFile = findJavaInstaller();

        // ----- create a temporary working directory -----

        File workingDirectory   = temporaryFolder.newFolder();
        File dockerFile         = new File(workingDirectory, "Dockerfile");
        Path dockerFileTemplate = Paths.get(DockerFunctionalTest.class.getResource("/JavaDockerFile").toURI()) ;

        // ----- copy the Dockerfile template to the working directory -----

        Files.copy(dockerFileTemplate,
                   dockerFile.toPath(),
                   StandardCopyOption.REPLACE_EXISTING);

        // ----- copy the JRE .tar.gz file to the working directory -----

        Files.copy(installerFile.toPath(),
                   new File(workingDirectory, "jre.tar.gz").toPath(),
                   StandardCopyOption.REPLACE_EXISTING);

        // ----- use the Docker build command to build the Java image -----

        try (Application application = platform.launch(Build.fromDockerFile()
                                                                  .withTags(javaImageName),
                                                             ImageCloseBehaviour.none(),
                                                             WorkingDirectory.at(workingDirectory)))
        {
            Assume.assumeThat("An error occurred building the Java Docker image", application.waitFor(), is(0));
        }
    }


    public static File findJavaInstaller() throws Exception
    {
        String installer = System.getProperty("oracle.tools.container.tests.jre");

        Assume.assumeThat("Could not create Java image: No oracle.tools.container.tests.jre property set " +
                          "pointing to a JRE tar.gz file",
                          installer, is(notNullValue()));

        Assume.assumeThat("Could not create Java image: The oracle.tools.container.tests.jre " +
                          "property does not point to a tar.gz file: " + installer,
                          installer.endsWith(".tar.gz"), is(true));

        File installerFile = new File(installer);

        Assume.assumeThat("Could not create Java image: The installer file pointed to by the property " +
                          "oracle.tools.container.tests.jre does not exist: " + installer,
                          installerFile.exists(), is(true));

        Assume.assumeThat("Could not create Java image: The installer file pointed to by the property " +
                          "oracle.tools.container.tests.jre is not a File: " + installer,
                          installerFile.isFile(), is(true));

        return installerFile;
    }
}
