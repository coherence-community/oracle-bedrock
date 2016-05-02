/*
 * File: Table.java
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
import com.oracle.bedrock.Options;

import static com.oracle.bedrock.lang.StringHelper.trimTrailingWhiteSpace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * A {@link Table} represents a collection of zero or more vertically arranged
 * {@link Row}s, each {@link Row} consisting of zero or more horizontally arranged
 * {@link Cell}s.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Table implements Iterable<Row>, Option
{
    /**
     * The {@link Row}s in the {@link Table}.
     */
    private ArrayList<Row> rows;

    /**
     * The formatting {@link Options} for the {@link Table}.
     */
    private Options options;


    /**
     * Constructs a {@link Table} given a collection of {@link Row}s.
     *
     * @param rows  the {@link Row}s in the {@link Table}
     */
    public Table(Row... rows)
    {
        this.rows = new ArrayList<>();

        if (rows != null)
        {
            for (Row row : rows)
            {
                this.rows.add(row);
            }
        }

        this.options = new Options();
    }


    /**
     * Obtains a {@link Row} {@link Row.Comparator} that uses the specific
     * {@link Cell} for comparison (uses natural ordering of the {@link Cell}
     * content to order the {@link Row}).
     *
     * @param column  the index of the {@link Cell} in the {@link Row} to compare {@link Row}s
     *
     * @return  the {@link Row} {@link Row.Comparator}
     */
    public static Row.Comparator orderByColumn(final int column)
    {
        return new Row.Comparator()
        {
            @Override
            public int compare(Row row1,
                               Row row2)
            {
                return row1.getCell(column).getLine(0).compareTo(row2.getCell(column).getLine(0));
            }
        };
    }


    /**
     * Obtain the formatting {@link Options} for the {@link Table}.
     *
     * @return  the formatting {@link Options} for the {@link Table}
     */
    public Options getOptions()
    {
        return options;
    }


    /**
     * Adds a {@link Row} to the {@link Table}
     *
     * @param row  the {@link Row} to add to the {@link Table}
     *
     * @return  the {@link Table}
     */
    public Table addRow(Row row)
    {
        rows.add(row);

        return this;
    }


    /**
     * Adds a {@link Row} consisting of a collection of {@link Cell}s to the {@link Table}
     *
     * @param cells  the {@link Cell}s in the {@link Row}
     *
     * @return  the {@link Table}
     */
    public Table addRow(Cell... cells)
    {
        return addRow(new Row(cells));
    }


    /**
     * Adds a {@link Row} consisting of a collection of {@link Cell} content to the {@link Table}
     *
     * @param cells  the {@link Cell} content in the {@link Row}
     *
     * @return  the {@link Table}
     */
    public Table addRow(String... cells)
    {
        if (cells == null)
        {
            return this;
        }
        else
        {
            Row row = new Row();

            for (String cell : cells)
            {
                row.addCell(cell);
            }

            return addRow(row);
        }
    }


    /**
     * Obtains the {@link Row} at the specified index, commencing at 0.
     *
     * @param index  the index of the {@link Row} to obtain
     *
     * @return  the {@link Row} at the specified index or <code>null</code>
     *          if the index is out-of-bounds
     */
    public Row getRow(int index)
    {
        if (index < 0 || index >= rows.size())
        {
            return null;
        }
        else
        {
            return rows.get(index);
        }
    }


    /**
     * Obtains the number of {@link Row}s in the {@link Table}
     *
     * @return  the number of {@link Row}s in the {@link Table}
     */
    public int size()
    {
        return rows.size();
    }


    @Override
    public Iterator<Row> iterator()
    {
        return rows.iterator();
    }


    @Override
    public String toString()
    {
        // determine the Cell Separator
        Cell.Separator cellSeparator = options.get(Cell.Separator.class);

        // -----------------------------------
        // determine the maximum widths of the cells each row in the table
        ArrayList<Integer> cellWidths = new ArrayList<>();

        for (Row row : this)
        {
            // ensure we have enough cells for the current row
            int rowWidth = row.width();

            while (cellWidths.size() < rowWidth)
            {
                cellWidths.add(0);
            }

            // adjust the existing cell widths based on the current row
            int i = 0;

            for (Cell cell : row)
            {
                int currentWidth = cellWidths.get(i);

                // determine the width of the cell
                // (use the width is defined by the cell, then the row, then the table)
                Cell.Width width = options.getOrDefault(Cell.Width.class,
                                               row.getOptions().getOrDefault(Cell.Width.class,
                                                                    this.getOptions().get(Cell.Width.class)));

                int cellWidth;

                if (width.isAutoDetect())
                {
                    cellWidth = cell.width();
                }
                else
                {
                    cellWidth = width.getCharacters();
                }

                if (cellWidth > currentWidth)
                {
                    cellWidths.set(i, cellWidth);
                }

                i++;
            }
        }

        // -----------------------------------
        // sort the rows (when a Row.Comparator has been provided)
        Row.Comparator comparator  = options.get(Row.Comparator.class);

        Row[]          orderedRows = new Row[rows.size()];

        rows.toArray(orderedRows);

        if (comparator != null)
        {
            Arrays.sort(orderedRows, comparator);
        }

        // -----------------------------------
        // generate the table
        StringBuilder builder = new StringBuilder();

        for (int rowIndex = 0; rowIndex < orderedRows.length; rowIndex++)
        {
            // grab the next row
            Row row = orderedRows[rowIndex];

            // append a row separator when it's not the first row
            if (rowIndex > 0)
            {
                builder.append("\n");
            }

            int line = 0;
            int rowHeight;

            do
            {
                rowHeight = 0;

                int rowWidth = row.width();

                for (int cellIndex = 0; cellIndex < rowWidth; cellIndex++)
                {
                    Cell cell = row.getCell(cellIndex);

                    // determine the cell justification
                    // (use the justification defined by the cell, then the row, then the table)
                    Cell.Justification justification = cell.getOptions().getOrDefault(Cell.Justification.class,
                                                                             row.getOptions()
                                                                                 .getOrDefault(Cell.Justification.class,
                                                                                      this.getOptions()
                                                                                          .get(Cell
                                                                                              .Justification.class)));

                    int cellHeight = cell.height();

                    rowHeight = cellHeight > rowHeight ? cellHeight : rowHeight;

                    String justifiedContent;

                    if (cell.isEmpty() || line >= cellHeight)
                    {
                        // output a cell separator?
                        if (cellIndex > 0)
                        {
                            builder.append(" ");
                            builder.append(cellSeparator.getSeparator());
                            builder.append(" ");
                        }

                        // justify the cell content
                        justifiedContent = justification.format("", cellWidths.get(cellIndex));
                    }
                    else
                    {
                        // output a cell separator?
                        if (cellIndex > 0)
                        {
                            builder.append(" ");
                            builder.append(cellSeparator.getSeparator());
                            builder.append(" ");
                        }

                        // justify the cell content
                        String content = cell.getLine(line);

                        justifiedContent = justification.format(content, cellWidths.get(cellIndex));

                    }

                    // ensure the last column doesn't have any unnecessary white space
                    if (cellIndex == rowWidth - 1)
                    {
                        justifiedContent = trimTrailingWhiteSpace(justifiedContent);
                    }

                    // output the justified cell content
                    builder.append(justifiedContent);
                }

                line++;

                if (line < rowHeight)
                {
                    builder.append("\n");
                }
            }
            while (line < rowHeight);
        }

        return builder.toString();
    }
}
