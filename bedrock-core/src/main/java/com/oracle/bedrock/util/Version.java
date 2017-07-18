/*
 * File: Version.java
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

package com.oracle.bedrock.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * A utility class for representing {@link Version} numbers and comparing them.
 * <p>
 * Copyright (c) 2017. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Version implements Comparable<Version>
{
    /**
     * The qualifier aliases.
     */
    private static final HashMap<String, String> QUALIFIER_ALIASES = new HashMap<>();


    static
    {
        // initialize the standard qualifier aliases / abbreviations
        QUALIFIER_ALIASES.put("a", "alpha");
        QUALIFIER_ALIASES.put("b", "beta");
        QUALIFIER_ALIASES.put("m", "milestone");
        QUALIFIER_ALIASES.put("r", "release");
        QUALIFIER_ALIASES.put("rc", "release candidate");
        QUALIFIER_ALIASES.put("sp", "service pack");
    }


    /**
     * The parsed components of the version number, either {@link Integer}s or {@link String}s.
     */
    private ArrayList components;


    /**
     * Constructs a {@link Version} given zero or more components strings.
     *
     * @param strings  the individual components strings of the {@link Version}
     */
    private Version(String... strings)
    {
        // the components of the Version
        this.components = new ArrayList();

        if (strings == null)
        {
            // if nothing is specified, we assume version zero
            this.components.add("0");
        }
        else
        {
            // parse and add each of the strings as components
            // (when a string itself contains components, they too are added as components)
            for (String number : strings)
            {
                // ensure we have a trimmed, lowercase value to parse
                number = number == null ? "" : number.trim().toLowerCase(Locale.ENGLISH);

                // the collector of digits / text for the version
                StringBuilder collector = new StringBuilder();

                char          last      = Character.MIN_VALUE;

                for (char c : number.toCharArray())
                {
                    if (Character.isDigit(c))
                    {
                        if (!Character.isDigit(last) && collector.length() != 0)
                        {
                            // remember the collected qualifier
                            components.add(expand(collector.toString()));

                            // create a new collector
                            collector = new StringBuilder();
                        }

                        collector.append(c);

                        last = c;
                    }
                    else if (Character.isLetter(c))
                    {
                        if (!Character.isLetter(last) && collector.length() != 0)
                        {
                            // store the collected digits as an integer
                            components.add(Integer.parseInt(collector.toString()));

                            // create a new collector
                            collector = new StringBuilder();
                        }

                        collector.append(c);

                        last = c;
                    }
                    else if (c == '.' || c == '-')
                    {
                        if (last == Character.MIN_VALUE)
                        {
                            // when there's no collected value, assume 0
                            components.add(Integer.valueOf(0));
                        }
                        else if (Character.isDigit(last))
                        {
                            // store the collected digits as an integer
                            components.add(Integer.parseInt(collector.toString()));
                        }
                        else if (Character.isLetter(last))
                        {
                            // remember the collected qualifier
                            components.add(expand(collector.toString()));
                        }

                        // create a new collector
                        collector = new StringBuilder();

                        last      = Character.MIN_VALUE;
                    }
                    else
                    {
                        // when we encounter anything else, we stop parsing
                        break;
                    }
                }

                if (components.isEmpty() || last != Character.MIN_VALUE)
                {
                    if (last == Character.MIN_VALUE)
                    {
                        // when there's no collected value, assume version 0
                        components.add(Integer.valueOf(0));
                    }
                    else if (Character.isDigit(last))
                    {
                        // store the collected digits as an integer
                        components.add(Integer.parseInt(collector.toString()));
                    }
                    else if (Character.isLetter(last))
                    {
                        // remember the collected qualifier
                        components.add(expand(collector.toString()));
                    }
                }
            }
        }
    }


    /**
     * Expands the commonly abbreviated version qualifiers.
     *
     * @param qualifier  the qualifier string to expand
     *
     * @return an expanded qualifier
     */
    private String expand(String qualifier)
    {
        return QUALIFIER_ALIASES.getOrDefault(qualifier, qualifier);
    }


    /**
     * Constructs a {@link Version} by parsing the specified components of a version number.
     * If a component string itself contains components, those are parsed and included in the {@link Version}.
     *
     * @param strings  the components of the {@link Version} number
     *
     * @return a {@link Version}
     */
    public static Version of(String... strings)
    {
        return new Version(strings);
    }


    /**
     * Construct a {@link Version} representing an unknown version number.
     *
     * @return an unknown {@link Version}
     */
    public static Version unknown()
    {
        return new Version("unknown");
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof Version))
        {
            return false;
        }

        Version version = (Version) o;

        return components.equals(version.components);
    }


    @Override
    public int compareTo(Version other)
    {
        if (other == null)
        {
            return 1;
        }
        else
        {
            // compare each of the components of each version, returning early when we know the result
            for (int i = 0; i < Math.max(this.components.size(), other.components.size()); i++)
            {
                Object x = i < this.components.size() ? this.components.get(i) : Integer.valueOf(0);
                Object y = i < other.components.size() ? other.components.get(i) : Integer.valueOf(0);

                if (x instanceof Integer && y instanceof Integer)
                {
                    if (!x.equals(y))
                    {
                        return ((Integer) x).compareTo((Integer) y);
                    }
                }
                else if (x instanceof Integer && y instanceof String)
                {
                    return 1;
                }
                else if (x instanceof String && y instanceof Integer)
                {
                    return -1;
                }
                else
                {
                    // both strings
                    if (!x.equals(y))
                    {
                        return ((String) x).compareTo((String) y);
                    }
                }
            }

            // when all of the components thus far are equal, the versions are equal
            return 0;
        }
    }


    @Override
    public int hashCode()
    {
        return components.hashCode();
    }


    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();

        for (Object component : components)
        {
            if (builder.length() > 0)
            {
                builder.append(".");
            }

            builder.append(component);
        }

        return builder.toString();
    }
}
