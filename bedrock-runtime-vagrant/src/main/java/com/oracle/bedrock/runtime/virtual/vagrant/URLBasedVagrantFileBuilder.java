/*
 * File: URLBasedVagrantFileBuilder.java
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

import com.oracle.bedrock.Options;
import com.oracle.bedrock.runtime.remote.options.HostName;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Optional;

/**
 * A {@link VagrantFileBuilder} that uses a template VagrantFile for building
 * a VagrantFile.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
class URLBasedVagrantFileBuilder implements VagrantFileBuilder
{
    /**
     * The {@link URL} of a template for the {@link VagrantFileBuilder}.
     */
    private URL vagrantFileTemplate;


    /**
     * Constructs a {@link URLBasedVagrantFileBuilder}.
     *
     * @param vagrantFileTemplate  the {@link URL} to the {@link VagrantFileBuilder} template
     */
    URLBasedVagrantFileBuilder(URL vagrantFileTemplate)
    {
        this.vagrantFileTemplate = vagrantFileTemplate;
    }


    @Override
    public Optional<HostName> create(File file, Options createOptions) throws IOException
    {
        try (PrintWriter writer = new PrintWriter(file))
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(vagrantFileTemplate.openStream()));
            String         line   = reader.readLine();

            while (line != null)
            {
                writer.println(line);
                line = reader.readLine();
            }

            writer.flush();

            return Optional.empty();
        }
    }
}
