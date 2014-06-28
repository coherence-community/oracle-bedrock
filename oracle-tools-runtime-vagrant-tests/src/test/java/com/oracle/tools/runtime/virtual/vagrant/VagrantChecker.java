/*
 * File: VagrantChecker.java
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

package com.oracle.tools.runtime.virtual.vagrant;

import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.SimpleApplicationSchema;
import com.oracle.tools.runtime.console.PipedApplicationConsole;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class to check for the existence of Vagrant.
 * This can be used to decide whether to run tests that require
 * Vagrant to be installed. Typically this would be done using
 * the JUnit check Assume.assumeTrue(VagrantChecker.vagrantExists())
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class VagrantChecker
{
    /** 
     * The RegEx pattern to use to match the expected output from running the
     * Vagrant --version command.
     */
    public static final String VAGRANT_REGEX =
        "^\\[Vagrant:out:(\\d)*\\](\\s)*(\\d)*:(\\s)*Vagrant\\s(\\d)*(\\.(\\d)*)*$";

    /**
     * Attempt to run the Vagrant --version command and
     * parse the expected output. If this succeeds then
     * Vagrant is present on the system.
     *
     * @return true if Vagrant is installed
     */
    public synchronized static boolean vagrantExists()
    {
        boolean noVagrantProperty = Boolean.getBoolean("no.vagrant");
        if (noVagrantProperty)
        {
            return false;
        }

        try
        {
            String                  command     = VagrantPlatform.getDefaultVagrantCommand();
            SimpleApplicationSchema schema      = new SimpleApplicationSchema(command).setArgument("--version");
            PipedApplicationConsole console     = new PipedApplicationConsole();
            Application             application = LocalPlatform.getInstance().realize(schema, "Vagrant", console);
            int                     exitCode    = application.waitFor();

            if (exitCode != 0)
            {
                return false;
            }

            String line = console.getOutputReader().readLine();

            application.close();

            Pattern pattern = Pattern.compile(VAGRANT_REGEX);
            Matcher matcher = pattern.matcher(line);

            return matcher.matches();
        }
        catch (Throwable e)
        {
            System.err.println("Error checking for Vagrant - assume not present. " + e.getMessage());

            return false;
        }
    }
}
