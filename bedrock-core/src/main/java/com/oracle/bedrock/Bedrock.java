/*
 * File: Bedrock.java
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

package com.oracle.bedrock;

import com.oracle.bedrock.annotations.Internal;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Provides internal information about the current build of Oracle Bedrock.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
@Internal
public final class Bedrock
{
    /**
     * The last parsed / detected version number.
     */
    private static String version;


    /**
     * Detect the version number based on the current environment.
     */
    static
    {
        // we use the Bedrock class to find version information
        Class<Bedrock> versionClass = Bedrock.class;

        // attempt to determine the version using the pom.xml of the artifact (in source tree)
        try
        {
            String className         = versionClass.getName();
            String classfileName     = "/" + className.replace('.', '/') + ".class";
            URL    classfileResource = versionClass.getResource(classfileName);

            if (classfileResource != null)
            {
                Path absolutePackagePath = Paths.get(classfileResource.toURI()).getParent();

                int  packagePathSegments = className.length() - className.replace(".", "").length();

                // Remove package segments from path, plus two more levels
                // for "target/classes", giving us the standard source files in a Maven Project
                Path path = absolutePackagePath;

                for (int i = 0, segmentsToRemove = packagePathSegments + 2; i < segmentsToRemove; i++)
                {
                    path = path.getParent();
                }

                Path pom = path.resolve("pom.xml");

                version = getArtifactFromPom(pom);

                // should that fail, attempt to determine the version based on the parent pom.xml (in the source tree)
                if (version == null)
                {
                    // attempt to use the parent pom
                    path    = path.getParent();

                    version = getArtifactFromPom(path.resolve("pom.xml"));
                }
            }
        }
        catch (Exception e)
        {
            version = null;
        }

        // attempt to determine the version using the Maven generated pom properties (in artifact jar)
        try
        {
            Properties properties = new Properties();
            InputStream inputStream =
                Bedrock.class.getResourceAsStream("/META-INF/maven/com.oracle.bedrock/bedrock-core/pom.properties");

            if (inputStream != null)
            {
                properties.load(inputStream);
                version = properties.getProperty("version", null);
            }
        }
        catch (Exception e)
        {
            version = null;
        }

        // attempt to use the Java MANIFEST.MF (in artifact jar)
        if (version == null)
        {
            Package p = Bedrock.class.getPackage();

            if (p != null)
            {
                version = p.getImplementationVersion();

                if (version == null)
                {
                    version = p.getSpecificationVersion();
                }
            }
        }

        if (version == null)
        {
            version = "";
        }
    }


    /**
     * Obtains the version of an artifact based on a pom.xml
     *
     * @param path  the path to the pom.xml file
     *
     * @return the version in the pom.xml or <code>null</code> if not found or an error occurs
     */
    private static String getArtifactFromPom(Path path)
    {
        // the version we detected
        String version;

        try (InputStream inputStream = Files.newInputStream(path))
        {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);

            doc.getDocumentElement().normalize();

            version = (String) XPathFactory.newInstance().newXPath().compile("/project/version").evaluate(doc,
                                                                                                          XPathConstants
                                                                                                              .STRING);

            if (version != null)
            {
                version = version.trim();

                version = version.isEmpty() ? null : version;
            }
        }
        catch (Exception e)
        {
            version = null;
        }

        return version;
    }


    /**
     * Obtain the version number.
     *
     * @return the version number or an empty string if the version can't be determined.
     */
    public static String getVersion()
    {
        return version;
    }
}
    