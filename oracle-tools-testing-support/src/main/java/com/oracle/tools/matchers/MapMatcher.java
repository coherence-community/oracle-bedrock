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

package com.oracle.tools.matchers;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

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
        mismatchDescription.appendText("map was ").appendValueList("[", ", ", "]", map.entrySet());
    }


    @Override
    public void describeTo(Description description)
    {
        description.appendText("map containing ").appendValueList("[", ", ", "]", map.entrySet());
    }


    @Factory
    public static <K, V> Matcher<Map<? extends K, ? extends V>> sameAs(Map<K, V> map)
    {
        return new MapMatcher<K, V>(map, Equivalence.EQUALS);
    }


    @Factory
    public static <K, V> Matcher<Map<? extends K, ? extends V>> sameAs(Map<K, V>      map,
                                                                       Equivalence<V> valueEquivalence)
    {
        return new MapMatcher<K, V>(map, valueEquivalence);
    }
}
