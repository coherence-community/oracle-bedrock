/*
 * File: DockerfileDeployer.java
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

package com.oracle.bedrock.runtime.docker.options;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.remote.options.Deployer;
import com.oracle.bedrock.runtime.remote.options.FileShareDeployer;

import java.io.File;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A specialized {@link Deployer} that copied the
 * artifacts to be deployed to the folder containing a Dockerfile and for each artifact
 * adds it to a list of files ro become ADD commands in the Dockerfile.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class DockerfileDeployer extends FileShareDeployer
{
    /**
     * The {@link PrintWriter} to use to write the ADD statements to
     */
    private final SortedSet<AddFile> addList;


    /**
     * Construct a new {@link DockerfileDeployer}.
     *
     * @param workingDirectory  the folder to copy the artifacts to
     * @param options           the {@link Option}s controlling the Dockerfile
     */
    public DockerfileDeployer(String    workingDirectory,
                              Option... options)
    {
        super(workingDirectory, workingDirectory, options);

        this.addList = new TreeSet<>();
    }


    /**
     * Write an ADD statement for the specified artifact.
     *
     * @param source         the file to add
     * @param destination    the remote location to copy the artifact to in the Dockerfile
     * @param platform       the {@link Platform} to perform
     * @param optionsByType  the {@link OptionsByType} to control the deployment
     *
     * @return  this method always returns true
     */
    @Override
    protected boolean performRemoteCopy(String        source,
                                        String        destination,
                                        Platform      platform,
                                        OptionsByType optionsByType)
    {
        File   sourceFile = new File(source);
        String sourceName = sourceFile.getName();

        if (destination == null || destination.trim().isEmpty())
        {
            destination = sourceName;
        }

        addList.add(new AddFile(sourceName, destination));

        return false;
    }


    /**
     * Write the Dockerfile ADD commands.
     *
     * @param writer  the {@link PrintWriter} to write the
     *                ADD commands to
     */
    public void write(PrintWriter writer)
    {
        addList.forEach((file) -> writer.printf("ADD %-50s %s\n", file.getSource(), file.getDestination()));
    }


    /**
     * A simple holder class for files to add to a Dockerfile.
     */
    protected static class AddFile implements Comparable<AddFile>
    {
        /**
         * The source file name.
         */
        private final String source;

        /**
         * The destination in the Dockerfile.
         */
        private final String destination;


        /**
         * Create an {@link AddFile}.
         *
         * @param source       the source file name.
         * @param destination  the destination in the Dockerfile
         */
        public AddFile(String source,
                       String destination)
        {
            this.source      = Objects.requireNonNull(source);
            this.destination = Objects.requireNonNull(destination);
        }


        /**
         * Obtain the source file name.
         *
         * @return  the source file name
         */
        public String getSource()
        {
            return source;
        }


        /**
         * Obtain the destination in the Dockerfile.
         *
         * @return  the destination in the Dockerfile
         */
        public String getDestination()
        {
            return destination;
        }


        @Override
        public int compareTo(AddFile o)
        {
            int result = source.compareTo(o.source);

            if (result == 0)
            {
                result = destination.compareTo(o.destination);
            }

            return result;
        }


        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }

            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            AddFile addFile = (AddFile) o;

            if (!source.equals(addFile.source))
            {
                return false;
            }

            return destination.equals(addFile.destination);

        }


        @Override
        public int hashCode()
        {
            int result = source.hashCode();

            result = 31 * result + destination.hashCode();

            return result;
        }


        @Override
        public String toString()
        {
            return "ADD " + source + ' ' + destination;
        }
    }
}
