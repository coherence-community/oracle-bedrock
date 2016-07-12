/*
 * File: Cell.java
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

/**
 * A {@link Cell} represent a unit of content in a {@link Row} that is
 * part of a {@link Table}.  Each {@link Cell} in a {@link Row} may contain
 * zero or more "lines".
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Cell
{
    /**
     * The lines of content (zero or more) in the {@link Cell}.
     */
    private ArrayList<String> lines;

    /**
     * The formatting {@link OptionsByType} for the {@link Cell}.
     */
    private OptionsByType optionsByType;


    /**
     * Constructs a {@link Cell} given an array of separate lines.
     * <p>
     * Should the lines themselves contain new-lines, they are split in to separate lines.
     *
     * @param lines  the lines of content for the {@link Cell}
     */
    public Cell(String... lines)
    {
        this.lines         = new ArrayList<>();
        this.optionsByType = OptionsByType.empty();

        if (lines != null)
        {
            for (String content : lines)
            {
                if (content == null)
                {
                    this.lines.add(null);
                }
                else
                {
                    for (String line : content.split("\\n\\r|\\n"))
                    {
                        this.lines.add(line);
                    }
                }
            }
        }
    }


    /**
     * An {@link Option} to define how the content of a {@link Cell} is to be justified.
     */
    public enum Justification implements Option
    {
        /**
         * {@link Cell} content should be left justified.
         */
        @OptionsByType.Default
        LEFT,

        /**
         * {@link Cell} content should be right justified.
         */
        RIGHT;

        /**
         * Formats the specified content in the specified width according
         * to the mode of justification.
         *
         * @param content  the content to format
         * @param width    the width of the field in which to format the content
         *
         * @return  the content formatted and justified in the specified field width
         *          (including padding with white space)
         */
        public String format(String content,
                             int    width)
        {
            return width <= 0 ? "" : this == LEFT ? String.format("%1$-" + width + "s",
                                                                  content) : String.format("%1$" + width + "s",
                                                                                           content);
        }
    }


    /**
     * Obtains the formatting {@link OptionsByType} for the {@link Cell}.
     *
     * @return  the formatting {@link OptionsByType} for the {@link Cell}
     */
    public OptionsByType getOptions()
    {
        return optionsByType;
    }


    /**
     * Obtains the content of the specified line in the {@link Cell} (without formatting)
     *
     * @param index  the index number of the line to return (starting at 0).
     *
     * @return  the content of the {@link Cell} specified by the line number
     *          (returns <code>null</code> when the index is out-of-bounds)
     */
    public String getLine(int index)
    {
        if (index < 0 || index >= lines.size())
        {
            return null;
        }
        else
        {
            return lines.get(index);
        }
    }


    /**
     * Determines if the {@link Cell} is empty (or only contains white-space)
     *
     * @return <code>true</code> if the cell is empty or only contains white-space
     */
    public boolean isEmpty()
    {
        for (String line : lines)
        {
            if (line == null || line.length() > 0 &&!line.trim().isEmpty())
            {
                return false;
            }
        }

        return true;
    }


    /**
     * Determines if the {@link Cell} contains one or more <code>null</code> values.
     *
     * @return <code>true</code> if the {@link Cell} contains one or more <code>null</code> values,
     *         <code>false</code> otherwise
     */
    public boolean containsNull()
    {
        for (String line : lines)
        {
            if (line == null)
            {
                return true;
            }
        }

        return false;
    }


    /**
     * Obtains the number of lines in the {@link Cell} (ie: the {@link Cell} height)
     *
     * @return  the number of lines in the {@link Cell}
     */
    public int height()
    {
        return lines.size();
    }


    /**
     * Obtains the width of the {@link Cell} (in characters), not taking into
     * account the specified {@link Width} option.
     *
     * @return  the number of characters for the {@link Cell}
     */
    public int width()
    {
        int maximum = 0;

        for (String line : lines)
        {
            if (line != null && line.length() > maximum)
            {
                maximum = line.length();
            }
        }

        return maximum;
    }


    /**
     * An {@link Option} to define how <code>null</code> is displayed when provided as
     * {@link Cell} content.
     */
    public static class DisplayNull implements Option
    {
        /**
         * The value to use when encountering a <code>null</code> value
         * provided as {@link Cell} content.
         */
        private String value;


        /**
         * Constructs a {@link DisplayNull} with a specific value.
         *
         * @param value  the value
         */
        private DisplayNull(String value)
        {
            this.value = value;
        }


        /**
         * Obtains the value to use for displaying <code>null</code> {@link Cell} content.
         *
         * @return the value
         */
        public String getValue()
        {
            return value;
        }


        /**
         * Obtains a {@link DisplayNull} with a specific value
         *
         * @param value  the value
         *
         * @return a {@link DisplayNull}
         */
        public static DisplayNull as(String value)
        {
            return new DisplayNull(value);
        }


        /**
         * Obtains a {@link DisplayNull} that displays <code>null</code> content
         * as an empty {@link String}.
         *
         * @return a {@link DisplayNull}
         */
        @OptionsByType.Default
        public static DisplayNull asEmptyString()
        {
            return new DisplayNull("");
        }


        /**
         * Obtains a {@link DisplayNull} that displays <code>null</code> content
         * as the {@link String} "null" (without quotes)
         *
         * @return a {@link DisplayNull}
         */
        public static DisplayNull asNull()
        {
            return new DisplayNull("null");
        }
    }


    /**
     * An {@link Option} to define the separator to use between {@link Cell}s in a {@link Row}.
     */
    public static class Separator implements Option
    {
        /**
         * The standard {@link Separator}.
         */
        private static final Separator STANDARD = new Separator(":");

        /**
         * The separator string between {@link Cell}s in a {@link Row}.
         */
        private String separator;


        /**
         * Constructs a {@link Separator} using a specific separator string.
         *
         * @param separator  the separator string
         */
        private Separator(String separator)
        {
            this.separator = separator;
        }


        /**
         * Obtains the {@link Separator} string
         *
         * @return  the {@link Separator} string
         */
        public String getSeparator()
        {
            return separator;
        }


        /**
         * Constructs a custom {@link Separator}.
         *
         * @param separator  the separator string
         *
         * @return a {@link Separator} for the specified string
         */
        public static Separator of(String separator)
        {
            return new Separator(separator);
        }


        /**
         * Obtains the standard {@link Separator}.
         *
         * @return  the standard {@link Separator}
         */
        @OptionsByType.Default
        public static Separator standard()
        {
            return STANDARD;
        }
    }


    /**
     * An {@link Option} to define how the width of a {@link Cell} is calculated.
     */
    public static class Width implements Option
    {
        private static Width AUTODETECT = new Width(-1);

        /**
         * The specific number of characters (-1 is autodetect).
         */
        private int characters;


        /**
         * Constructs a specific {@link Width} formatting {@link Option} for a {@link Cell}.
         *
         * @param characters  the number of characters
         */
        private Width(int characters)
        {
            this.characters = characters;
        }


        /**
         * Determines if the width of a {@link Cell} should be automatically detected
         * (ie: the {@link Cell} has no preference).
         *
         * @return  <code>true</code> is autodetection is enabled,
         *          <code>false</code> if a specific width is required
         */
        public boolean isAutoDetect()
        {
            return characters < 0;
        }


        /**
         * Determines the width of a {@link Cell} in characters.
         *
         * @return  the width of the {@link Cell} in characters or
         *          -1 if automatic detection is required
         */
        public int getCharacters()
        {
            return characters;
        }


        /**
         * Obtain a {@link Width} that is configured for automatic detection.
         *
         * @return  an automatic {@link Width}
         */
        @OptionsByType.Default
        public static Width autodetect()
        {
            return AUTODETECT;
        }


        /**
         * Obtain a {@link Width} for a {@link Cell} that is for a specific width
         *
         * @param characters  the desired width (in number of characters)
         *
         * @return  a specific {@link Width}
         */
        public static Width of(int characters)
        {
            return new Width(characters);
        }
    }
}
