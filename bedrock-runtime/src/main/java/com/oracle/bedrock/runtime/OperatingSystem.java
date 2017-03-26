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

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.util.Pair;
import com.oracle.bedrock.util.Version;

import java.io.File;
import java.util.LinkedHashMap;

/**
 * Provides information about the {@link OperatingSystem} being used by a {@link Platform}.
 * <p>
 * While an {@link OperatingSystem} may be provided or requested as an {@link Option}, all {@link Platform}s
 * provide an instance through {@link Platform#getOperatingSystem()}.  However in cases were a {@link Platform}
 * is unavailable and {@link Option}s, the use of an {@link OperatingSystem} an {@link Option} is supported.
 * <p>
 * Copyright (c) 2017. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class OperatingSystem implements Option
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
        MAPPINGS.put("windows 8.1", Pair.of(Type.WINDOWS, "8.1"));
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
    private final String name;

    /**
     * The release name of the {@link OperatingSystem}.
     */
    private final String releaseName;

    /**
     * The {@link Type} of the {@link OperatingSystem}.
     */
    private final Type type;

    /**
     * The {@link Version} of the {@link OperatingSystem}, determined by inspecting
     * the "os.version" system property.
     */
    private final Version version;

    /**
     * The {@link String} used by a {@link OperatingSystem} to separate paths with in a fully qualified file name.
     * <p>
     * eg: On *unix this is "/", on Windows it is "\"
     */
    private final String fileSeparator;

    /**
     * The {@link String} used by a {@link OperatingSystem} to separate individual
     * paths occurring in a collection of paths.  eg: a class path
     * <p>
     * eg: On *unix this is ":", on Windows it is ";"
     */
    private final String pathSeparator;

    /**
     * The {@link String} used by the {@link OperatingSystem} to separate individual lines of text.
     * <p>
     * eg: On *unix this is "\n", on Windows it is "\r\n"
     */
    private final String lineSeparator;


    /**
     * Constructs an {@link OperatingSystem}.
     *
     * @param name           the name of the {@link OperatingSystem}
     * @param releaseName    the release name of the {@link OperatingSystem}
     * @param type           the {@link Type} of the {@link OperatingSystem}
     * @param version        the {@link Version} of the {@link OperatingSystem}
     * @param fileSeparator  the {@link String} to separate files in a path on the {@link OperatingSystem}
     * @param pathSeparator  the {@link String} to separate paths in a collection of paths on the {@link OperatingSystem}
     * @param lineSeparator  the {@link String} to separate lines of text on the {@link OperatingSystem}
     */
    private OperatingSystem(String  name,
                            String  releaseName,
                            Type    type,
                            Version version,
                            String  fileSeparator,
                            String  pathSeparator,
                            String  lineSeparator)
    {
        this.name          = name;
        this.releaseName   = releaseName;
        this.type          = type;
        this.version       = version;
        this.fileSeparator = fileSeparator;
        this.pathSeparator = pathSeparator;
        this.lineSeparator = lineSeparator;
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


        /**
         * Determines the typical {@link File#separator} used to separate path segments with in a fully qualified
         * file name for an {@link OperatingSystem} {@link Type}.
         * <p>
         * This value may be different from that ultimately detected and provided when calling
         * {@link OperatingSystem#getFileSeparator()}.
         * <p>
         * When the {@link Type} is unknown or the {@link File#separator} can't be determined, the
         * {@link File#separator} of the current {@link OperatingSystem} is returned.
         * <p>
         * eg: On *unix this is "/", on Windows it is "\"
         *
         * @return the {@link File#separator} for the {@link Type}
         */
        String getTypicalFileSeparator()
        {
            return isWindows() ? "\\" : (isUnix() || isBSD() ? "/" : File.separator);
        }


        /**
         * Determines the typical {@link File#pathSeparator} used to separate multiple paths in a collection of paths for
         * an {@link OperatingSystem} {@link Type}.  eg: a class path
         * <p>
         * This value may be different from that ultimately detected and provided when calling
         * {@link OperatingSystem#getPathSeparator()}.
         * <p>
         * When the {@link Type} is unknown or the {@link File#pathSeparator} can't be determined, the
         * {@link File#pathSeparator} of the current {@link OperatingSystem} is returned.
         * <p>
         * eg: On *unix this is ":", on Windows it is ";"
         *
         * @return the {@link File#pathSeparator} for the {@link Type}
         */
        String getTypicalPathSeparator()
        {
            return isWindows() ? ";" : (isUnix() || isBSD() ? ":" : File.pathSeparator);
        }


        /**
         * Determines the typical {@link System#lineSeparator()} used to separate lines of text for
         * an {@link OperatingSystem} {@link Type}.
         * <p>
         * This value may be different from that ultimately detected and provided when calling
         * {@link OperatingSystem#getLineSeparator()}.
         * <p>
         * When the {@link Type} is unknown or the {@link System#lineSeparator()} can't be determined, the
         * {@link System#lineSeparator()} of the current {@link OperatingSystem} is returned.
         * <p>
         * eg: On *unix this is ":", on Windows it is ";"
         *
         * @return the {@link System#lineSeparator()} for the {@link Type}
         */
        String getTypicalLineSeparator()
        {
            return isWindows() ? "\r\n" : (isUnix() || isBSD() ? "\n" : System.lineSeparator());
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
    public Type getType()
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
    public String getReleaseName()
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


    /**
     * Determines the {@link String} the {@link OperatingSystem} uses to separate paths with in a fully qualified
     * file name.
     * <p>
     * eg: On *unix this is "/", on Windows it is "\"
     *
     * @return the file separator
     */
    public String getFileSeparator()
    {
        return fileSeparator;
    }


    /**
     * Determines the {@link String} the {@link OperatingSystem} uses to separate individual paths occurring in a
     * collection of paths.  eg: a class path
     * <p>
     * eg: On *unix this is ":", on Windows it is ";"
     *
     * @return the path separator
     */
    public String getPathSeparator()
    {
        return pathSeparator;
    }


    /**
     * Determines the {@link String} the {@link OperatingSystem} uses to separate individual lines of text.
     * <p>
     * eg: On *unix this is "\n", on Windows it is "\r\n"
     *
     * @return the line separator
     */
    public String getLineSeparator()
    {
        return lineSeparator;
    }


    @Override
    public String toString()
    {
        return type + " " + version + " (" + name + (releaseName.isEmpty() ? "" : ", " + releaseName) + ")";
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof OperatingSystem))
        {
            return false;
        }

        OperatingSystem that = (OperatingSystem) o;

        if (!name.equals(that.name))
        {
            return false;
        }

        if (!releaseName.equals(that.releaseName))
        {
            return false;
        }

        if (type != that.type)
        {
            return false;
        }

        if (!version.equals(that.version))
        {
            return false;
        }

        if (!fileSeparator.equals(that.fileSeparator))
        {
            return false;
        }

        if (!pathSeparator.equals(that.pathSeparator))
        {
            return false;
        }

        return lineSeparator.equals(that.lineSeparator);
    }


    @Override
    public int hashCode()
    {
        int result = name.hashCode();

        result = 31 * result + releaseName.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + version.hashCode();
        result = 31 * result + fileSeparator.hashCode();
        result = 31 * result + pathSeparator.hashCode();
        result = 31 * result + lineSeparator.hashCode();

        return result;
    }


    /**
     * Constructs a custom {@link OperatingSystem}, using the file, path and line separators
     * defined by the {@link Type}.
     *
     * @param name           the name of the {@link OperatingSystem}
     * @param releaseName    the release name of the {@link OperatingSystem}
     * @param type           the {@link Type} of the {@link OperatingSystem}
     * @param version        the {@link Version} of the {@link OperatingSystem}
     *
     * @return a new {@link OperatingSystem}
     */
    public static OperatingSystem custom(String  name,
                                         String  releaseName,
                                         Type    type,
                                         Version version)
    {
        return new OperatingSystem(name,
                                   releaseName,
                                   type,
                                   version,
                                   type.getTypicalFileSeparator(),
                                   type.getTypicalPathSeparator(),
                                   type.getTypicalLineSeparator());
    }


    /**
     * Constructs a custom {@link OperatingSystem}.
     *
     * @param name           the name of the {@link OperatingSystem}
     * @param releaseName    the release name of the {@link OperatingSystem}
     * @param type           the {@link Type} of the {@link OperatingSystem}
     * @param version        the {@link Version} of the {@link OperatingSystem}
     * @param fileSeparator  the {@link String} to separate files in a path on the {@link OperatingSystem}
     * @param pathSeparator  the {@link String} to separate paths in a collection of paths on the {@link OperatingSystem}
     * @param lineSeparator  the {@link String} to separate lines of text on the {@link OperatingSystem}
     *
     * @return a new {@link OperatingSystem}
     */
    public static OperatingSystem custom(String  name,
                                         String  releaseName,
                                         Type    type,
                                         Version version,
                                         String  fileSeparator,
                                         String  pathSeparator,
                                         String  lineSeparator)
    {
        return new OperatingSystem(name, releaseName, type, version, fileSeparator, pathSeparator, lineSeparator);
    }

     
    /**
     * Attempts to detect the local {@link OperatingSystem} based on the currently running Java Virtual Machine.
     *
     * @return the detected {@link OperatingSystem}.
     */
    @OptionsByType.Default
    public static OperatingSystem local()
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
     * Determines the {@link OperatingSystem} based on the Java System Property
     * defined os.name and os.version.
     *
     * @param osName     the operating system name
     * @param osVersion  the operating system version
     *
     * @return the {@link OperatingSystem}
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

        return OperatingSystem.custom(osName,
                                      release,
                                      type,
                                      Version.of(osVersion),
                                      type.getTypicalFileSeparator(),
                                      type.getTypicalPathSeparator(),
                                      type.getTypicalLineSeparator());
    }


    /**
     * Obtains an {@link OperatingSystem} to represent an unknown {@link OperatingSystem}.
     *
     * @return an {@link OperatingSystem}
     */
    public static OperatingSystem unknown()
    {
        return OperatingSystem.custom("",
                                      "",
                                      Type.UNKNOWN,
                                      Version.of(""),
                                      File.separator,
                                      File.pathSeparator,
                                      System.lineSeparator());
    }
}
