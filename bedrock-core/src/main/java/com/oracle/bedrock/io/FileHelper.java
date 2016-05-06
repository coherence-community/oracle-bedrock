/*
 * File: FileHelper.java
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

package com.oracle.bedrock.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Common {@link File} utilities.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class FileHelper
{
    /**
     * Create a Temporary Folder.
     *
     * @param prefix   the prefix to use for the temporary folder
     *
     * @return a File representing the temporary folder
     *
     * @throws IOException if the folder could not be created
     */
    public static File createTemporaryFolder(String prefix) throws IOException
    {
        // the location of the system temporary folder
        File systemTemporaryFolder = new File(System.getProperty("java.io.tmpdir"));

        // the number of attempts to create a unique temporary folder
        final int maximumAttempts   = 100;
        int       attemptsRemaining = maximumAttempts;

        // the resulting temporary folder
        File temporaryFolder = null;

        while (attemptsRemaining > 0 && temporaryFolder == null)
        {
            // we need some randomness for the folder name
            String randomness = UUID.randomUUID().toString();

            // does the folder already exist?
            temporaryFolder = new File(systemTemporaryFolder, prefix + randomness);

            if (temporaryFolder.exists())
            {
                temporaryFolder = null;
            }
            else
            {
                try
                {
                    // attempt to create the folder
                    temporaryFolder.mkdir();

                    return temporaryFolder;
                }
                catch (Exception e)
                {
                    temporaryFolder = null;
                }
            }

            // next attempt!
            attemptsRemaining--;
        }

        throw new IOException("Failed to create a unique temporary folder after " + maximumAttempts + " attempts.");
    }


    /**
     * Attempt to recursively delete the specified file or folder.
     *
     * @param file  a file representing the File or Folder to delete
     *
     * @return true if all files/folders have been deleted, false if only
     *         partial removal occurred
     */
    public static boolean recursiveDelete(File file)
    {
        if (file != null && file.exists())
        {
            if (file.isDirectory())
            {
                for (File child : file.listFiles())
                {
                    if (!recursiveDelete(child))
                    {
                        return false;
                    }
                }
            }

            return file.delete();
        }
        else
        {
            return true;
        }
    }


    /**
     * Zips the specified files, including directories and nested directories into the specified zip file.
     *
     * @param files           the {@link File}s (including directories) to zip
     * @param baseFolderName  the folder that will be created when unzipping the zip file
     * @param toZipFile       the zip file (that will be created)
     *
     * @throws IOException  when the files can't be zipped for some reason
     */
    public static void zip(Iterable<File> files,
                           String         baseFolderName,
                           String         toZipFile) throws IOException
    {
        ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(toZipFile));

        if (files != null)
        {
            for (File file : files)
            {
                if (file.exists())
                {
                    if (file.isDirectory())
                    {
                        addFolderToZip(file, baseFolderName, zip);
                    }
                    else
                    {
                        addFileToZip(file, baseFolderName, zip);
                    }
                }
            }
        }

        zip.flush();
        zip.close();
    }


    /**
     * Adds a specified {@link File} (that is not a folder) to the specified
     * {@link ZipOutputStream}, the {@link File} being located in the specified parent
     * folder.
     *
     * @param file              a file representing the {@link File} to add
     * @param parentFolderName  the parent folder of the {@link File}
     * @param zip               the {@link ZipOutputStream} to which to write the {@link File}
     *
     * @throws IOException  should any problems occur
     */
    private static void addFileToZip(File            file,
                                     String          parentFolderName,
                                     ZipOutputStream zip) throws IOException
    {
        String parent = parentFolderName == null
                        || parentFolderName.trim().isEmpty() ? "" : parentFolderName.trim() + "/";

        if (file.exists())
        {
            zip.putNextEntry(new ZipEntry(parent + file.getName()));

            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
            byte[]              buffer      = new byte[16096];
            int                 count       = 0;

            while ((count = inputStream.read(buffer)) != -1)
            {
                zip.write(buffer, 0, count);
            }

            zip.closeEntry();
        }
    }


    /**
     * Recursively adds the contents of the specified {@link File} representing a folder to the
     * specified {@link ZipOutputStream}.
     *
     * @param folder            a {@link File} representing a folder to add
     * @param parentFolderName  the parent folder of the {@link File}
     * @param zip               the {@link ZipOutputStream} to which to write the {@link File}
     *
     * @throws IOException  should any problems occur
     */
    private static void addFolderToZip(File            folder,
                                       String          parentFolderName,
                                       ZipOutputStream zip) throws IOException
    {
        String parent = parentFolderName == null
                        || parentFolderName.trim().isEmpty() ? "" : parentFolderName.trim() + "/";

        if (folder.exists())
        {
            File[] files = folder.listFiles();

            if (files != null)
            {
                for (File file : files)
                {
                    if (file.isDirectory())
                    {
                        addFolderToZip(file, parent + file.getName(), zip);
                    }
                    else
                    {
                        addFileToZip(file, parentFolderName, zip);
                    }
                }
            }
        }
    }
}
