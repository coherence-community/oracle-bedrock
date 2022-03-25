/*
 * File: MapMatcher.java
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

package com.oracle.bedrock.testsupport.matchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 * A Hamcrest-based {@link TypeSafeMatcher} for {@link Map}s.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class MapMatcher<K, V> extends TypeSafeMatcher<Map<? extends K, ? extends V>>
{
    /**
     * The {@link Map} to which comparison will occur.
     */
    private Map<K, V> map;

    /**
     * The {@link Equivalence} function for the values.
     */
    private Equivalence<V> valueEquivalence;


    /**
     * Constructs a {@link MapMatcher}.
     *
     * @param map  the {@link Map} to compare with
     */
    private MapMatcher(Map<K, V>      map,
                       Equivalence<V> valueEquivalence)
    {
        this.map              = map;
        this.valueEquivalence = valueEquivalence == null ? Equivalence.EQUALS : valueEquivalence;
    }


    @Override
    public boolean matchesSafely(Map<? extends K, ? extends V> otherMap)
    {
        if (map == otherMap)
        {
            return true;
        }
        else if (map.size() == otherMap.size())
        {
            boolean same = true;

            for (Iterator<? extends K> i = map.keySet().iterator(); same && i.hasNext(); )
            {
                K key    = i.next();
                V value1 = map.get(key);
                V value2 = otherMap.get(key);

                same = (value1 == value2) || valueEquivalence.equals(value1, value2);
            }

            for (Iterator<? extends K> i = otherMap.keySet().iterator(); same && i.hasNext(); )
            {
                K key    = i.next();
                V value1 = map.get(key);
                V value2 = otherMap.get(key);

                same = (value1 == value2) || valueEquivalence.equals(value1, value2);
            }

            return same;
        }
        else
        {
            return false;
        }
    }


    @Override
    public void describeMismatchSafely(Map<? extends K, ? extends V> map,
                                       Description                   mismatchDescription)
    {
        if (this.map != null && map == null)
        {
            mismatchDescription.appendText("The provided map was null (but the matching map wasn't)");
        }
        else if (this.map == null && map != null)
        {
            mismatchDescription.appendText("The provided map was not null (but the matching map was)");
        }
        else
        {
            // compare the two maps to determine the differences
            HashSet<K> missingKeys     = new HashSet<>();
            HashSet<K> additionalKeys  = new HashSet<>();
            HashSet<K> nonMatchingKeys = new HashSet<>();

            for (K k : this.map.keySet())
            {
                if (map.containsKey(k))
                {
                    V thisValue = this.map.get(k);
                    V thatValue = map.get(k);

                    if (thisValue != thatValue
                        || thisValue == null && thatValue != null
                        || thisValue != null && thatValue == null
                        ||!thisValue.equals(thatValue))
                    {
                        nonMatchingKeys.add(k);
                    }
                }
                else
                {
                    missingKeys.add(k);
                }
            }

            for (K k : map.keySet())
            {
                if (this.map.containsKey(k))
                {
                    V thisValue = this.map.get(k);
                    V thatValue = map.get(k);

                    if (thisValue != thatValue
                        || thisValue == null && thatValue != null
                        || thisValue != null && thatValue == null
                        ||!thisValue.equals(thatValue))
                    {
                        nonMatchingKeys.add(k);
                    }
                }
                else
                {
                    additionalKeys.add(k);
                }
            }

            if (this.map.size() == map.size())
            {
                mismatchDescription.appendText("Both maps are the same size, each containing " + map.size()
                                               + " entries");
            }
            else
            {
                mismatchDescription.appendText("Each map has a different size.  The matcher map contains "
                                               + this.map.size() + " entries.  The provided map contains " + map.size()
                                               + " entries.");
            }

            if (!missingKeys.isEmpty())
            {
                mismatchDescription.appendText("The provided map is missing the keys ").appendValueList("[",
                                                                                                        ", ",
                                                                                                        "]",
                                                                                                        missingKeys);
            }

            if (!additionalKeys.isEmpty())
            {
                mismatchDescription.appendText("The provided map additionally contains the keys ").appendValueList("[",
                                                                                                                   ", ",
                                                                                                                   "]",
                                                                                                                   additionalKeys);
            }

            if (!nonMatchingKeys.isEmpty())
            {
                mismatchDescription.appendText("The following entries in the provided map don't match the matching map ")
                .appendValueList("[",
                                 ", ",
                                 "]",
                                 nonMatchingKeys);
            }
        }
    }


    @Override
    public void describeTo(Description description)
    {
        description.appendText("map containing ").appendValueList("[", ", ", "]", map.entrySet());
    }


    public static <K, V> Matcher<Map<? extends K, ? extends V>> sameAs(Map<K, V> map)
    {
        return new MapMatcher<K, V>(map, Equivalence.EQUALS);
    }


    public static <K, V> Matcher<Map<? extends K, ? extends V>> sameAs(Map<K, V>      map,
                                                                       Equivalence<V> valueEquivalence)
    {
        return new MapMatcher<K, V>(map, valueEquivalence);
    }
}
