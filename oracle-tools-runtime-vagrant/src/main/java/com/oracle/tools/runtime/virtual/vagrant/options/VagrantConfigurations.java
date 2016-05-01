/*
 * File: VagrantConfigurations.java
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

package com.oracle.tools.runtime.virtual.vagrant.options;

import com.oracle.tools.ComposableOption;
import com.oracle.tools.Option;
import com.oracle.tools.Options;
import com.oracle.tools.runtime.virtual.vagrant.VagrantPlatform;

import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * An {@link Option} to compose zero or more free-form configuration strings for
 * a {@link VagrantPlatform}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class VagrantConfigurations implements ComposableOption<VagrantConfigurations>
{
    /**
     * The freeform configurations.
     */
    private ArrayList<String> configurations;


    /**
     * Constructs an empty {@link VagrantConfigurations}.
     */
    public VagrantConfigurations()
    {
        this.configurations = new ArrayList<>();
    }


    /**
     * Constructs a {@link VagrantConfigurations} based on another {@link VagrantConfigurations}.
     *
     * @param other  the other {@link VagrantConfigurations}
     */
    public VagrantConfigurations(VagrantConfigurations other)
    {
        this.configurations = new ArrayList<>(other.configurations);
    }


    /**
     * Creates an empty {@link VagrantConfigurations}.
     *
     * @return an empty {@link VagrantConfigurations}
     */
    @Options.Default
    public static VagrantConfigurations none()
    {
        return new VagrantConfigurations();
    }


    /**
     * Creates a {@link VagrantConfigurations} using the specified configurations.
     *
     * @param configurations  the configurations
     *
     * @return  a {@link VagrantConfigurations}
     */
    public static VagrantConfigurations of(String... configurations)
    {
        VagrantConfigurations result = new VagrantConfigurations();

        if (configurations != null)
        {
            for (String configuration : configurations)
            {
                result.configurations.add(configuration);
            }
        }

        return result;
    }


    @Override
    public VagrantConfigurations compose(VagrantConfigurations other)
    {
        VagrantConfigurations result = new VagrantConfigurations(this);

        result.configurations.addAll(other.configurations);

        return result;
    }


    /**
     * Writes the {@link VagrantConfigurations} to the specified {@link PrintWriter}.
     *
     * @param writer   the {@link PrintWriter}
     * @param padding  the paddind for each line
     */
    public void write(PrintWriter writer,
                      String      padding)
    {
        for (String configuration : configurations)
        {
            writer.printf("%s%s\n", padding, configuration);
        }

        writer.println();
    }
}
