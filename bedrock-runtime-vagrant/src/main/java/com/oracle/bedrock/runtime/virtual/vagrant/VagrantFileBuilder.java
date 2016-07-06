/*
 * File: VagrantFileBuilder.java
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

package com.oracle.bedrock.runtime.virtual.vagrant;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.remote.options.HostName;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

/**
 * Builds a VagrantFile that can be used to establish a {@link VagrantPlatform}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public interface VagrantFileBuilder
{
    /**
     * Creates a Vagrant File containing Vagrant Configuration at the name and location specified by the
     * provided {@link File}, returning the public {@link HostName} detected during creation of the file.
     *
     * @param file           the {@link File} in which the Vagrant Configuration will be written
     * @param optionsByType  the {@link OptionsByType} for creating the file
     *
     * @return the detected {@link HostName} (when available)
     *
     * @throws IOException  when the {@link File} could not be created
     */
    Optional<HostName> create(File          file,
                              OptionsByType optionsByType) throws IOException;


    /**
     * Obtains a {@link VagrantFileBuilder} that will be produced based on a template.
     *
     * @param vagrantFileTemplate  the {@link URL} of the Vagrant File template
     *
     * @return a {@link VagrantFileBuilder}
     */
    static VagrantFileBuilder from(URL vagrantFileTemplate)
    {
        return new URLBasedVagrantFileBuilder(vagrantFileTemplate);
    }


    /**
     * Obtains a {@link VagrantFileBuilder} that will be produced based on the specified {@link Option}s.
     *
     * @param options  the {@link Option}s
     *
     * @return a {@link VagrantFileBuilder}
     *
     * @see com.oracle.bedrock.runtime.virtual.vagrant.options
     */
    static VagrantFileBuilder from(Option... options)
    {
        return new OptionsBasedVagrantFileBuilder(options);
    }
}
