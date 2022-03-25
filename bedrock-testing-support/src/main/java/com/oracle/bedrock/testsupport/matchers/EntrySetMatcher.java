/*
 * File: EntrySetMatcher.java
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A Hamcrest-based {@link TypeSafeMatcher} for {@link Set}s of {@link Map.Entry}s.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class EntrySetMatcher<K, V> extends TypeSafeMatcher<Set<Map.Entry<K, V>>>
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
     * Constructs a {@link EntrySetMatcher}.
     *
     * @param entries  the {@link Set} of {@link Map.Entry}s to compare with
     */
    private EntrySetMatcher(Set<Map.Entry<K, V>> entries,
                            Equivalence<V>       valueEquivalence)
    {
        this.map              = mapOf(entries);
        this.valueEquivalence = valueEquivalence == null ? Equivalence.EQUALS : valueEquivalence;
    }


    /**
     * Obtains a {@link Map} representation of a {@link Set} of {@link Map.Entry}s.
     *
     * @param entries  the {@link Set} of {@link Map.Entry}s
     *
     * @return a {@link Map}
     */
    private Map<K, V> mapOf(Set<Map.Entry<K, V>> entries)
    {
        if (entries == null)
        {
            return null;
        }
        else
        {
            HashMap<K, V> map = new HashMap<>();

            for (Iterator<Map.Entry<K, V>> iterator = entries.iterator(); iterator.hasNext(); )
            {
                Map.Entry<K, V> entry = iterator.next();

                map.put(entry.getKey(), entry.getValue());
            }

            return map;
        }
    }


    @Override
    public boolean matchesSafely(Set<Map.Entry<K, V>> entries)
    {
        Map<K, V>        otherMap = mapOf(entries);

        MapMatcher<K, V> matcher  = (MapMatcher<K, V>) MapMatcher.sameAs(map, valueEquivalence);

        return matcher.matchesSafely(otherMap);
    }


    @Override
    public void describeMismatchSafely(Set<Map.Entry<K, V>> entries,
                                       Description          mismatchDescription)
    {
        MapMatcher<K, V> matcher = (MapMatcher<K, V>) MapMatcher.sameAs(map, valueEquivalence);

        matcher.describeMismatchSafely(mapOf(entries), mismatchDescription);
    }


    @Override
    public void describeTo(Description description)
    {
        MapMatcher<K, V> matcher = (MapMatcher<K, V>) MapMatcher.sameAs(map, valueEquivalence);

        matcher.describeTo(description);
    }


    public static <K, V> Matcher<Set<Map.Entry<K, V>>> sameAs(Set<Map.Entry<K, V>> entries,
                                                              Equivalence<V>       equivalence)
    {
        return new EntrySetMatcher<K, V>(entries, equivalence);
    }


    public static <K, V> Matcher<Map<? extends K, ? extends V>> sameAs(Set<Map.Entry<K, V>> entries)
    {
        return sameAs(entries, Equivalence.EQUALS);
    }
}
