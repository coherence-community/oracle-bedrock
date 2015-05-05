/*
 * File: Tabularize.java
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

package com.oracle.tools.table;

import java.util.Map;
import java.util.Properties;

/**
 * Methods to support creating {@link Table}s from common data-structures.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Tabularize
{
    /**
     * Obtains a {@link Table} representation of a {@link Properties}
     * (ordered by property name).
     *
     * @param properties  the {@link Properties} to tabularize
     *
     * @return  a {@link Table} representing the {@link Properties}
     */
    public static final Table tabularize(Properties properties)
    {
        Table table = new Table();

        if (properties != null)
        {
            for (String name : properties.stringPropertyNames())
            {
                String value = properties.getProperty(name);

                table.addRow(name, value);
            }
        }

        // add ordering by the name
        table.getOptions().add(Table.orderByColumn(0));

        return table;
    }


    /**
     * Obtains a {@link Table} representation of a {@link Map}
     * (ordered by key).
     *
     * @param map  the {@link Map} to tabularize
     *
     * @return  a {@link Table} representing the {@link Map}
     */
    public static final Table tabularize(Map<?, ?> map)
    {
        Table table = new Table();

        if (map != null)
        {
            for (Object key : map.keySet())
            {
                table.addRow(key.toString(), map.get(key).toString());
            }
        }

        // add ordering by the key
        table.getOptions().add(Table.orderByColumn(0));

        return table;
    }
}
