/*
 * File: OperatingSystem.java
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

package com.oracle.bedrock.runtime;

import com.oracle.bedrock.util.Pair;
import com.oracle.bedrock.util.Version;

import java.util.LinkedHashMap;

/**
 * Provides information about the {@link OperatingSystem} being used by a {@link Platform}.
 * <p>
 * Copyright (c) 2017. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class OperatingSystem
{
    /**
     * Ordered mappings from {@link OperatingSystem} names (with versions) into the corresponding
     * {@link Type} and release name.
     */
    private static final LinkedHashMap<String, Pair<Type, String>> MAPPINGS = new LinkedHashMap<>();


    static
    {
        MAPPINGS.put("windows 2000", Pair.of(Type.WINDOWS, "2000"));
        MAPPINGS.put("windows 2003", Pair.of(Type.WINDOWS, "2003"));
        MAPPINGS.put("windows 2008", Pair.of(Type.WINDOWS, "2008"));
        MAPPINGS.put("windows server 2008", Pair.of(Type.WINDOWS, "Server 2008"));
        MAPPINGS.put("windows server 2012", Pair.of(Type.WINDOWS, "Server 2012"));
        MAPPINGS.put("windows 95", Pair.of(Type.WINDOWS, "95"));
        MAPPINGS.put("windows 98", Pair.of(Type.WINDOWS, "98"));
        MAPPINGS.put("windows me", Pair.of(Type.WINDOWS, "ME"));
        MAPPINGS.put("windows nt", Pair.of(Type.WINDOWS, "NT"));
        MAPPINGS.put("windows xp", Pair.of(Type.WINDOWS, "XP"));
        MAPPINGS.put("windows vista", Pair.of(Type.WINDOWS, "Vista"));
        MAPPINGS.put("windows 7", Pair.of(Type.WINDOWS, "7"));
        MAPPINGS.put("windows 8", Pair.of(Type.WINDOWS, "8"));
        MAPPINGS.put("windows 10", Pair.of(Type.WINDOWS, "10"));
        MAPPINGS.put("windows", Pair.of(Type.WINDOWS, ""));

        MAPPINGS.put("aix", Pair.of(Type.AIX, ""));
        MAPPINGS.put("hp-ux", Pair.of(Type.HPUX, ""));
        MAPPINGS.put("os/2", Pair.of(Type.OS2, ""));
        MAPPINGS.put("os/400", Pair.of(Type.OS400, ""));

        MAPPINGS.put("irix", Pair.of(Type.IRIX, ""));
        MAPPINGS.put("linux", Pair.of(Type.LINUX, ""));
        MAPPINGS.put("freebsd", Pair.of(Type.FREEBSD, ""));
        MAPPINGS.put("openbsd", Pair.of(Type.OPENBSD, ""));
        MAPPINGS.put("netbsd", Pair.of(Type.NETBSD, ""));
        MAPPINGS.put("solaris", Pair.of(Type.SOLARIS, ""));
        MAPPINGS.put("sunos", Pair.of(Type.SUNOS, ""));

        MAPPINGS.put("mac os x 10.12", Pair.of(Type.MACOS, "Sierra"));
        MAPPINGS.put("mac os x 10.11", Pair.of(Type.MACOS, "El Capitan"));
        MAPPINGS.put("mac os x 10.10", Pair.of(Type.MACOS, "Yosemite"));
        MAPPINGS.put("mac os x 10.9", Pair.of(Type.MACOS, "Mavericks"));
        MAPPINGS.put("mac os x 10.8", Pair.of(Type.MACOS, "Mountain Lion"));
        MAPPINGS.put("mac os x 10.7", Pair.of(Type.MACOS, "Lion"));
        MAPPINGS.put("mac os x 10.6", Pair.of(Type.MACOS, "Snow Leopard"));
        MAPPINGS.put("mac os x 10.5", Pair.of(Type.MACOS, "Leopard"));
        MAPPINGS.put("mac os x 10.4", Pair.of(Type.MACOS, "Tiger"));
        MAPPINGS.put("mac os x 10.3", Pair.of(Type.MACOS, "Panther"));
        MAPPINGS.put("mac os x 10.2", Pair.of(Type.MACOS, "Jaguar"));
        MAPPINGS.put("mac os x 10.1", Pair.of(Type.MACOS, "Puma"));
        MAPPINGS.put("mac os x 10.0", Pair.of(Type.MACOS, "Cheetah"));
        MAPPINGS.put("mac os x", Pair.of(Type.MACOS, ""));

        // the catch all "unknown" OperatingSystem
        MAPPINGS.put("", Pair.of(Type.UNKNOWN, ""));
    }


    /**
     * The name of the {@link OperatingSystem}, determined by inspecting
     * the "os.name" system property.
     */
    private String name;

    /**
     * The release name of the {@link OperatingSystem}.
     */
    private String releaseName;

    /**
     * The {@link Type} of the {@link OperatingSystem}.
     */
    private Type type;

    /**
     * The {@link Version} of the {@link OperatingSystem}, determined by inspecting
     * the "os.version" system property.
     */
    private Version version;


    /**
     * Constructs an {@link OperatingSystem}.
     *
     * @param name         the name of the {@link OperatingSystem}
     * @param releaseName  the release name of the {@link OperatingSystem}
     * @param type         the {@link Type} of the {@link OperatingSystem}
     * @param version      the {@link Version} of the {@link OperatingSystem}
     */
    private OperatingSystem(String  name,
                            String  releaseName,
                            Type    type,
                            Version version)
    {
        this.name        = name;
        this.releaseName = releaseName;
        this.type        = type;
        this.version     = version;
    }


    /**
     * The known types of {@link OperatingSystem}.
     */
    public enum Type
    {
        AIX,
        FREEBSD,
        HPUX,
        IRIX,
        LINUX,
        MACOS,
        NETBSD,
        OPENBSD,
        OS2,
        OS400,
        SOLARIS,
        SUNOS,
        WINDOWS,
        ZOS,
        UNKNOWN;

        /**
         * Determine if the {@link OperatingSystem} {@link Type} is Unix-based.
         *
         * @return <code>true</code> if the {@link Type} is Unix-based, <code>false</code> otherwise.
         */
        public boolean isUnix()
        {
            return this == AIX
                   || this == FREEBSD
                   || this == HPUX
                   || this == IRIX
                   || this == LINUX
                   || this == MACOS
                   || this == NETBSD
                   || this == OPENBSD
                   || this == SOLARIS
                   || this == SUNOS
                   || this == ZOS;
        }


        /**
         * Determine if the {@link OperatingSystem} {@link Type} is Windows-based.
         *
         * @return <code>true</code> if the {@link Type} is Windows-based, <code>false</code> otherwise.
         */
        public boolean isWindows()
        {
            return this == WINDOWS;
        }


        /**
         * Determine if the {@link OperatingSystem} {@link Type} is BSD-based.
         *
         * @return <code>true</code> if the {@link Type} is BSD-based, <code>false</code> otherwise.
         */
        public boolean isBSD()
        {
            return this == FREEBSD || this == MACOS || this == NETBSD || this == OPENBSD;
        }
    }


    /**
     * Obtains the full name of the {@link OperatingSystem} as reported by a {@link Platform}.
     * <p>
     * This may include manufacturer information, branding and version information.
     *
     * @return the full name of the {@link OperatingSystem}
     */
    public String getName()
    {
        return name;
    }


    /**
     * Obtains the {@link Type} of the {@link OperatingSystem},
     * typically determined through analysing the {@link #getName()}.
     *
     * @return  the {@link Type} of the {@link OperatingSystem}
     */
    Type getType()
    {
        return type;
    }


    /**
     * Obtains the release / marketing name for a version of an {@link OperatingSystem},
     * typically determined through analysing the {@link #getName()}.
     * <p>
     * For example, MacOS 11.11 release name was "El Capitan"
     *
     * @return the release name
     */
    String getReleaseName()
    {
        return releaseName;
    }


    /**
     * Obtains the {@link Version} number of the {@link OperatingSystem},
     * typically determined through analysing the {@link #getName()}.
     *
     * @return the {@link Version}
     */
    public Version getVersion()
    {
        return version;
    }


    @Override
    public String toString()
    {
        return type + " " + version + " (" + name + (releaseName.isEmpty() ? "" : ", " + releaseName) + ")";

    }


    /**
     * Constructs a custom {@link OperatingSystem}.
     *
     * @param name         the name of the {@link OperatingSystem}
     * @param releaseName  the release name of the {@link OperatingSystem}
     * @param type         the {@link Type} of the {@link OperatingSystem}
     * @param version      the {@link Version} of the {@link OperatingSystem}
     *
     * @return a new {@link OperatingSystem}
     */
    public static OperatingSystem custom(String  name,
                                         String  releaseName,
                                         Type    type,
                                         Version version)
    {
        return new OperatingSystem(name, releaseName, type, version);
    }


    /**
     * Attempts to detect the {@link OperatingSystem} based on the currently running Java Virtual Machine.
     *
     * @return the detected {@link OperatingSystem}.
     */
    public static OperatingSystem detect()
    {
        try
        {
            return OperatingSystem.from(System.getProperty("os.name"), System.getProperty("os.version"));
        }
        catch (Exception e)
        {
            return OperatingSystem.unknown();
        }
    }


    /**
     * Attempts to detect the {@link OperatingSystem} based on the Java System Property
     * defined os.name and os.version.
     *
     * @return the detected {@link OperatingSystem}
     */
    public static OperatingSystem from(String osName,
                                       String osVersion)
    {
        // ensure we have lower-case values
        osName    = osName == null ? "" : osName.trim();
        osVersion = osVersion == null ? "" : osVersion.trim();

        // create a name including the version number so we can match against both
        String name = osName.trim().toLowerCase() + (osVersion.isEmpty() ? "" : " " + osVersion);

        // detect the type and release of the operating system based on the name
        // (assume unknown)
        Type   type    = Type.UNKNOWN;
        String release = "";

        // using the mapping table, attempt to match the operating system name to the type and release name
        for (String mapping : MAPPINGS.keySet())
        {
            if (name.matches(mapping) || name.startsWith(mapping))
            {
                Pair<Type, String> pair = MAPPINGS.get(mapping);

                type    = pair.getX();
                release = pair.getY();
                break;
            }
        }

        return OperatingSystem.custom(osName, release, type, Version.of(osVersion));
    }


    /**
     * Obtains an {@link OperatingSystem} to represent an unknown {@link OperatingSystem}.
     *
     * @return an {@link OperatingSystem}
     */
    public static OperatingSystem unknown()
    {
        return OperatingSystem.custom("", "", Type.UNKNOWN, Version.of(""));
    }
}
