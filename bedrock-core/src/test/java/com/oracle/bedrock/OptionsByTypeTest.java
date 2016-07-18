/*
 * File: OptionsByTypeTest.java
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

package com.oracle.bedrock;

import com.oracle.bedrock.options.Decoration;
import com.oracle.bedrock.options.Timeout;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Unit tests for {@link OptionsByType}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class OptionsByTypeTest
{
    /**
     * A simple {@link Option} using a {@link OptionsByType.Default}
     * annotation on a enum.
     */
    public enum Meal implements Option
    {
        TOAST,
        SOUP,
        STEAK,
        @OptionsByType.Default
        CHICKEN,
        FISH
    }


    /**
     * A simple {@link Option} using a {@link OptionsByType.Default}
     * annotation on a static attribute.
     */
    public enum Device implements Option
    {
        CASSETTE,
        FLOPPY,
        TAPE,
        HARD_DRIVE,
        SOLID_STATE_DRIVE;

        /**
         * Field description
         */
        @OptionsByType.Default
        public static Device DEFAULT = FLOPPY;
    }


    /**
     * A simple {@link Option} using a {@link OptionsByType.Default}
     * annotation on a static getter method.
     */
    public enum Duration implements Option
    {
        SECOND,
        MINUTE,
        HOUR;

        @OptionsByType.Default
        public static Duration getDefault()
        {
            return SECOND;
        }
    }


    /**
     * Ensure that we can get an {@link Option} that has a default.
     */
    @Test
    public void shouldGetOptionWithDefault()
    {
        OptionsByType optionsByType = OptionsByType.empty();

        assertThat(optionsByType.get(Timeout.class), is(Timeout.autoDetect()));
    }


    /**
     * Ensure that the default instance returned is the same value.
     */
    @Test
    public void shouldReturnSameDefaultInstance()
    {
        OptionsByType optionsByType = OptionsByType.empty();

        Timeout       timeout       = optionsByType.get(Timeout.class);

        assertThat(timeout, is(Timeout.autoDetect()));

        assertThat(optionsByType.get(Timeout.class), equalTo(timeout));
    }


    /**
     * Ensure that we can add and get a specific {@link Option}.
     */
    @Test
    public void shouldAddAndGetSpecificOption()
    {
        Timeout       timeout       = Timeout.after(5, TimeUnit.MINUTES);

        OptionsByType optionsByType = OptionsByType.of(timeout);

        assertThat(optionsByType.get(Timeout.class), is(timeout));
    }


    /**
     * Ensure that we can add, get and then replace a specific {@link Option}.
     */
    @Test
    public void shouldReplaceAndGetSpecificOption()
    {
        Timeout       timeout       = Timeout.after(5, TimeUnit.MINUTES);

        OptionsByType optionsByType = OptionsByType.of(timeout);

        Timeout       otherTimeout  = Timeout.after(1, TimeUnit.SECONDS);

        optionsByType.add(otherTimeout);

        assertThat(optionsByType.get(Timeout.class), is(otherTimeout));
    }


    /**
     * Ensure that we don't replace an existing specific {@link Option}.
     */
    @Test
    public void shouldNotReplaceAndGetSpecificOption()
    {
        Timeout       timeout       = Timeout.after(5, TimeUnit.MINUTES);

        OptionsByType optionsByType = OptionsByType.of(timeout);

        Timeout       otherTimeout  = Timeout.after(1, TimeUnit.SECONDS);

        optionsByType.addIfAbsent(otherTimeout);

        assertThat(optionsByType.get(Timeout.class), is(timeout));
    }


    /**
     * Ensure that we can remove a specific {@link Option}.
     */
    @Test
    public void shouldRemoveASpecificOption()
    {
        Timeout       timeout       = Timeout.after(5, TimeUnit.MINUTES);

        OptionsByType optionsByType = OptionsByType.of(timeout);

        assertThat(optionsByType.get(Timeout.class), is(timeout));

        assertThat(optionsByType.remove(Timeout.class), is(true));
        assertThat(optionsByType.remove(Timeout.class), is(false));

        // the timeout should now be the default
        assertThat(optionsByType.get(Timeout.class), is(Timeout.autoDetect()));
    }


    /**
     * Ensure that the class of an {@link Option} for an class
     * that directly implements the {@link Option} interface is
     * the class itself.
     */
    @Test
    public void shouldDetermineDirectlyImplementedOptionClass()
    {
        Timeout timeout     = Timeout.after(5, TimeUnit.MINUTES);

        Class   optionClass = OptionsByType.getClassOf(timeout);

        assertThat(optionClass.equals(Timeout.class), is(true));
    }


    /**
     * Ensure that the class of an {@link Option} for an class
     * that indirectly implements the {@link Option} interface,
     * via an interface that extends {@link Option} is
     * the interface that extends {@link Option}.
     */
    @Test
    public void shouldDetermineIndirectlyImplementedOptionClass()
    {
        Enhanced option      = new Enhanced();

        Class    optionClass = OptionsByType.getClassOf(option);

        assertThat(optionClass.equals(EnhancedOption.class), is(true));
    }


    /**
     * Ensure that the class of an {@link Option} for an class
     * that extends another {@link Option} implementation is
     * the class / interface that directly extends {@link Option}.
     */

    @Test
    public void shouldDetermineExtendedOptionClass()
    {
        ExtendedEnhanced option      = new ExtendedEnhanced();

        Class            optionClass = OptionsByType.getClassOf(option);

        assertThat(optionClass.equals(EnhancedOption.class), is(true));
        assertThat(optionClass.equals(ExtendedEnhanced.class), is(false));
    }


    /**
     * Ensure that an abstract class of {@link Option} is not returned
     * as a concrete type of {@link Option}.
     */

    @Test
    public void shouldNotDetermineAbstractOptionClass()
    {
        Class optionClass = OptionsByType.getClassOf(AbstractEnhanced.class);

        assertThat(optionClass, is(nullValue()));
    }


    /**
     * Ensure that we can create {@link OptionsByType} and request an option
     */
    @Test
    public void shouldCreateAndRequestAnOption()
    {
        Timeout       timeout       = Timeout.after(5, TimeUnit.MINUTES);

        OptionsByType optionsByType = OptionsByType.of(timeout);

        assertThat(optionsByType.get(Timeout.class), is(timeout));
    }


    /**
     * Ensure that {@link OptionsByType} maintain a set by concrete type.
     */
    @Test
    public void shouldMaintainASetByConcreteType()
    {
        Timeout       fiveMinutes   = Timeout.after(5, TimeUnit.MINUTES);
        Timeout       oneSecond     = Timeout.after(1, TimeUnit.SECONDS);

        OptionsByType optionsByType = OptionsByType.of(fiveMinutes, oneSecond);

        assertThat(optionsByType.get(Timeout.class), is(oneSecond));
    }


    /**
     * Ensure that {@link OptionsByType} can return a default using a
     * static method annotated with {@link OptionsByType.Default}.
     */
    @Test
    public void shouldDetermineDefaultUsingAnnotatedStaticMethod()
    {
        OptionsByType optionsByType = OptionsByType.empty();

        assertThat(optionsByType.get(Duration.class), is(Duration.getDefault()));
    }


    /**
     * Ensure that {@link OptionsByType} can return a default using a
     * static field annotated with {@link OptionsByType.Default}.
     */
    @Test
    public void shouldDetermineDefaultUsingAnnotatedStaticField()
    {
        OptionsByType optionsByType = OptionsByType.empty();

        assertThat(optionsByType.get(Device.class), is(Device.DEFAULT));
    }


    /**
     * Ensure that {@link OptionsByType} can return a default using an
     * enum annotated with {@link OptionsByType.Default}.
     */
    @Test
    public void shouldDetermineDefaultUsingAnnotatedEnum()
    {
        OptionsByType optionsByType = OptionsByType.empty();

        assertThat(optionsByType.get(Meal.class), is(Meal.CHICKEN));
    }


    /**
     * Ensure that {@link OptionsByType} can return a default using a
     * constructor annotated with {@link OptionsByType.Default}.
     */
    @Test
    public void shouldDetermineDefaultUsingAnnotatedConstructor()
    {
        OptionsByType optionsByType = OptionsByType.empty();

        assertThat(optionsByType.get(Beverage.class).toString(), is("Beer"));
    }


    /**
     * Ensure that {@link OptionsByType} can collect a single collectable,
     * including creating a collector.
     */
    @Test
    public void shouldCollectASingleCollectable()
    {
        OptionsByType optionsByType = OptionsByType.of(new Message("Hello"));

        assertThat(optionsByType.get(Message.class), is(nullValue()));
        assertThat(optionsByType.get(Messages.class), is(not(nullValue())));
        assertThat(optionsByType.get(Messages.class).get(), is("Hello"));
    }


    /**
     * Ensure that {@link OptionsByType} can collect a multiple collectables,
     * including creating a collector.
     */
    @Test
    public void shouldCollectMultipleCollectables()
    {
        OptionsByType optionsByType = OptionsByType.of(new Message("Hello"), new Message("G'day"));

        assertThat(optionsByType.get(Message.class), is(nullValue()));
        assertThat(optionsByType.get(Messages.class), is(not(nullValue())));
        assertThat(optionsByType.get(Messages.class).get(), is("Hello, G'day"));
    }


    /**
     * Ensure that {@link OptionsByType} can discard a single collectable.
     */
    @Test
    public void shouldDiscardASingleCollectable()
    {
        OptionsByType optionsByType = OptionsByType.of(new Message("Hello"));

        assertThat(optionsByType.get(Message.class), is(nullValue()));
        assertThat(optionsByType.get(Messages.class), is(not(nullValue())));
        assertThat(optionsByType.get(Messages.class).get(), is("Hello"));

        optionsByType.remove(new Message("Hello"));

        assertThat(optionsByType.get(Message.class), is(nullValue()));
        assertThat(optionsByType.get(Messages.class), is(not(nullValue())));
        assertThat(optionsByType.get(Messages.class).get(), is(""));
    }


    /**
     * Ensure that {@link OptionsByType} can discard a multiple collectables
     */
    @Test
    public void shouldDiscardMultipleCollectables()
    {
        OptionsByType optionsByType = OptionsByType.of(new Message("Hello"), new Message("G'day"));

        assertThat(optionsByType.get(Message.class), is(nullValue()));
        assertThat(optionsByType.get(Messages.class), is(not(nullValue())));
        assertThat(optionsByType.get(Messages.class).get(), is("Hello, G'day"));

        optionsByType.remove(new Message("G'day"));
        optionsByType.remove(new Message("Hello"));

        assertThat(optionsByType.get(Message.class), is(nullValue()));
        assertThat(optionsByType.get(Messages.class), is(not(nullValue())));
        assertThat(optionsByType.get(Messages.class).get(), is(""));
    }


    /**
     * Ensure that {@link OptionsByType} iterate over collectables.
     */
    @Test
    public void shouldIterateOverCollectables()
    {
        OptionsByType optionsByType = OptionsByType.of(new Message("Hello"), new Message("G'day"));

        assertThat(optionsByType.get(Message.class), is(nullValue()));
        assertThat(optionsByType.get(Messages.class), is(not(nullValue())));
        assertThat(optionsByType.get(Messages.class).get(), is("Hello, G'day"));

        HashSet<Message> messages = new HashSet<>();

        for (Message message : optionsByType.getInstancesOf(Message.class))
        {
            messages.add(message);
        }

        assertThat(messages.size(), is(2));
        assertThat(messages.contains(new Message("Hello")), is(true));
        assertThat(messages.contains(new Message("G'day")), is(true));
    }


    /**
     * Ensure that we can add {@link Decoration}s to {@link OptionsByType}.
     */
    @Test
    public void shouldAddAnRemoveDecorations()
    {
        OptionsByType   optionsByType = OptionsByType.empty().add(Decoration.of("hello")).add(Decoration.of("world"));

        HashSet<String> decorations   = new HashSet<>();

        for (String string : optionsByType.getInstancesOf(String.class))
        {
            decorations.add(string);
        }

        assertThat(decorations.size(), is(2));

        optionsByType.remove(Decoration.of("hello"));
        optionsByType.remove(Decoration.of("world"));

        decorations = new HashSet<>();

        for (String string : optionsByType.getInstancesOf(String.class))
        {
            decorations.add(string);
        }

        assertThat(decorations.size(), is(0));
    }


    /**
     * An {@link EnhancedOption}.
     */
    public static interface EnhancedOption extends Option
    {
    }


    /**
     * An abstract {@link EnhancedOption}.
     */
    public abstract class AbstractEnhanced implements EnhancedOption
    {
    }


    /**
     * A simple {@link Option} using a {@link OptionsByType.Default}
     * annotation on a public no-args constructor.
     */
    public static class Beverage implements Option
    {
        /**
         * Constructs ...
         *
         */
        @OptionsByType.Default
        public Beverage()
        {
        }


        @Override
        public String toString()
        {
            return "Beer";
        }
    }


    /**
     * A simple {@link EnhancedOption}.
     */
    public class Enhanced extends AbstractEnhanced
    {
    }


    /**
     * An extended {@link EnhancedOption}.
     */
    public class ExtendedEnhanced extends Enhanced
    {
    }


    /**
     * A {@link Collectable} for testing.
     */
    public static class Message implements Option.Collectable
    {
        private String value;


        /**
         * Constructs ...
         *
         *
         * @param value
         */
        public Message(String value)
        {
            this.value = value;
        }


        public String get()
        {
            return value;
        }


        @Override
        public Class<Messages> getCollectorClass()
        {
            return Messages.class;
        }


        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }

            if (!(o instanceof Message))
            {
                return false;
            }

            Message message = (Message) o;

            return value != null ? value.equals(message.value) : message.value == null;

        }


        @Override
        public int hashCode()
        {
            return value != null ? value.hashCode() : 0;
        }
    }


    /**
     * A {@link Collector} for testing.
     */
    public static class Messages implements Option.Collector<Message, Messages>
    {
        private ArrayList<Message> messageList;


        /**
         * Constructs ...
         *
         */
        @OptionsByType.Default
        public Messages()
        {
            this.messageList = new ArrayList<>();
        }


        /**
         * Constructs ...
         *
         *
         * @param messages
         */
        public Messages(Message... messages)
        {
            if (messages == null)
            {
                this.messageList = new ArrayList<>();
            }
            else
            {
                this.messageList = new ArrayList<>(messages.length);

                for (Message message : messages)
                {
                    this.messageList.add(message);
                }
            }
        }


        /**
         * Constructs ...
         *
         *
         * @param messages
         */
        public Messages(Messages messages)
        {
            this.messageList = new ArrayList<>(messages.messageList.size());

            for (Message message : messages.messageList)
            {
                this.messageList.add(message);
            }
        }


        public String get()
        {
            StringBuilder builder = new StringBuilder();

            for (Message message : messageList)
            {
                if (builder.length() > 0)
                {
                    builder.append(", ");
                }

                builder.append(message.get());
            }

            return builder.toString();
        }


        @Override
        public Messages with(Message message)
        {
            Messages messages = new Messages(this);

            messages.messageList.add(message);

            return messages;
        }


        @Override
        public Messages without(Message message)
        {
            Messages messages = new Messages(this);

            messages.messageList.remove(message);

            return messages;
        }


        @Override
        public <O> Iterable<O> getInstancesOf(Class<O> requiredClass)
        {
            if (requiredClass.isAssignableFrom(Message.class))
            {
                return (Iterable<O>) messageList;
            }
            else
            {
                return Collections.EMPTY_LIST;
            }
        }


        @Override
        public Iterator<Message> iterator()
        {
            return messageList.iterator();
        }
    }
}
