/*
 * File: TableTest.java
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

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.core.Is.is;

/**
 * Tests for the {@link Table} and associated classes.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class TableTest
{
    /**
     * Ensure that we can represent and output an empty {@link Table}.
     */
    @Test
    public void shouldProduceAnEmptyTable()
    {
        Table table = new Table();

        Assert.assertThat(table.toString(), is(""));
    }


    /**
     * Ensure that we can represent and output a single {@link Cell}ed {@link Table}.
     */
    @Test
    public void shouldProduceSingleCellTable()
    {
        Table table = new Table();

        table.addRow("Hello World");

        Assert.assertThat(table.toString(), is("Hello World"));
    }


    /**
     * Ensure that we can represent and output two {@link Cell}s in a single {@link Row}ed
     * {@link Table}.
     */
    @Test
    public void shouldProduceTwoCellsInSingleRowTable()
    {
        Table table = new Table();

        table.addRow("Hello", "World");

        Assert.assertThat(table.toString(), is("Hello : World"));
    }


    /**
     * Ensure that we can represent and output two by two {@link Cell}ed {@link Table}.
     */
    @Test
    public void shouldProduceTwoByTwoCelledTable()
    {
        Table table = new Table();

        table.addRow("Hello", "World");
        table.addRow("Gudday", "Mate");

        Assert.assertThat(table.toString(), is("Hello  : World\nGudday : Mate"));
    }


    /**
     * Ensure that we can represent and output a single column two-{@link Row}ed {@link Table}.
     */
    @Test
    public void shouldProduceSingleColumnTwoRowedTable()
    {
        Table table = new Table();

        table.addRow("Hello");
        table.addRow("World");

        Assert.assertThat(table.toString(), is("Hello\nWorld"));
    }


    /**
     * Ensure that we can represent and output a multi-{@link Row}ed {@link Table}.
     */
    @Test
    public void shouldProduceMultiRowedTable()
    {
        Table table = new Table();

        table.addRow("Hello");
        table.addRow("World");
        table.addRow("Gudday");
        table.addRow("Mate");

        Assert.assertThat(table.toString(), is("Hello\nWorld\nGudday\nMate"));
    }


    /**
     * Ensure that we can represent and output a multi-{@link Row}ed {@link Table}
     * (using right-justification) (using cell-based justification).
     */
    @Test
    public void shouldProduceMultiRowedTableUsingCellBasedRightJustification()
    {
        Table table = new Table();

        table.addRow("Hello");
        table.addRow("World");
        table.addRow("Gudday");
        table.addRow("Mate");

        table.getRow(0).getCell(0).getOptions().add(Cell.Justification.RIGHT);
        table.getRow(1).getCell(0).getOptions().add(Cell.Justification.RIGHT);
        table.getRow(2).getCell(0).getOptions().add(Cell.Justification.RIGHT);
        table.getRow(3).getCell(0).getOptions().add(Cell.Justification.RIGHT);

        Assert.assertThat(table.toString(), is(" Hello\n World\nGudday\n  Mate"));
    }


    /**
     * Ensure that we can represent and output a multi-{@link Row}ed {@link Table}
     * (using right-justification) (using row-based justification).
     */
    @Test
    public void shouldProduceMultiRowedTableUsingRowBasedRightJustification()
    {
        Table table = new Table();

        table.addRow("Hello");
        table.addRow("World");
        table.addRow("Gudday");
        table.addRow("Mate");

        table.getRow(0).getOptions().add(Cell.Justification.RIGHT);
        table.getRow(1).getOptions().add(Cell.Justification.RIGHT);
        table.getRow(2).getOptions().add(Cell.Justification.RIGHT);
        table.getRow(3).getOptions().add(Cell.Justification.RIGHT);

        Assert.assertThat(table.toString(), is(" Hello\n World\nGudday\n  Mate"));
    }


    /**
     * Ensure that we can represent and output a multi-{@link Row}ed {@link Table}
     * (using right-justification) (using table-based justification).
     */
    @Test
    public void shouldProduceMultiRowedTableUsingTableBasedRightJustification()
    {
        Table table = new Table();

        table.addRow("Hello");
        table.addRow("World");
        table.addRow("Gudday");
        table.addRow("Mate");

        table.getOptions().add(Cell.Justification.RIGHT);

        Assert.assertThat(table.toString(), is(" Hello\n World\nGudday\n  Mate"));
    }


    @Test
    public void shouldTestATable()
    {
        Table table = new Table();

        table.addRow("Column 1", "Column 2", "Column 3", "Column 4");
        table.addRow("Lots of stuff in here\n",
                     "Not\nmuch",
                     "Oh no... this is too much stuff",
                     "There\nAre\nSome\nAdditional\nLines\nHere!\n");
        table.addRow("F", "F", "F", "F");
        table.addRow("D", "D", "D", "D");
        table.addRow("E", "E", "E", "E");

        table.getRow(0).getCell(0).getOptions().add(Cell.Width.of(30));
        table.getRow(1).getCell(3).getOptions().add(Cell.Justification.RIGHT);

        table.getOptions().add(Table.orderByColumn(0));

        System.out.println(table);
    }
}
