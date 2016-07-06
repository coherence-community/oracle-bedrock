/*
 * File: Row.java
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

package com.oracle.bedrock.table;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A {@link Row} represents zero or more horizontally arranged {@link Cell}s
 * in a {@link Table}.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Row implements Iterable<Cell>
{
    /**
     * The {@link Cell}s that make up the {@link Row}
     */
    private ArrayList<Cell> cells;

    /**
     * The formatting {@link OptionsByType} for the {@link Row}.
     */
    private OptionsByType optionsByType;


    /**
     * Constructs a {@link Row} consisting of zero or more {@link Cell}s.
     *
     * @param cells  the {@link Cell}s across the {@link Row}
     */
    public Row(Cell... cells)
    {
        this.cells = new ArrayList<>();

        if (cells != null)
        {
            for (Cell cell : cells)
            {
                this.cells.add(cell);
            }
        }

        this.optionsByType = OptionsByType.empty();
    }


    /**
     * Obtains the number of {@link Cell}s (ie: the width) in the {@link Row}
     *
     * @return  the number of {@link Cell}s in the {@link Row}
     */
    public int width()
    {
        return cells.size();
    }


    /**
     * Adds a {@link Cell} to the {@link Row}
     *
     * @param cell  the {@link Cell} to add
     *
     * @return  the {@link Row}
     */
    public Row addCell(Cell cell)
    {
        cells.add(cell);

        return this;
    }


    /**
     * Adds {@link Cell} content to the {@link Row}
     *
     * @param cell  the {@link Cell} content to add
     *
     * @return  the {@link Row}
     */
    public Row addCell(String cell)
    {
        return addCell(new Cell(cell));
    }


    /**
     * Obtains the specified {@link Cell} from the {@link Row}
     * (or <code>null</code> if the {@link Cell} number is out-of-bounds.
     *
     * @param index  the index of the {@link Cell} to return (starting at 0)
     *
     * @return  the {@link Cell} at the specified index or <code>null</code>
     */
    public Cell getCell(int index)
    {
        if (index < 0 || index >= cells.size())
        {
            return null;
        }
        else
        {
            return cells.get(index);
        }
    }


    /**
     * Obtains the formatting {@link OptionsByType} for the {@link Row}.
     *
     * @return  the formatting {@link OptionsByType} for the {@link Row}
     */
    public OptionsByType getOptions()
    {
        return optionsByType;
    }


    @Override
    public Iterator<Cell> iterator()
    {
        return cells.iterator();
    }


    /**
     * An {@link Option} to define how {@link Row}s can be compared and thus sorted
     * in a {@link Table}.
     */
    public interface Comparator extends Option, java.util.Comparator<Row>
    {
    }
}
