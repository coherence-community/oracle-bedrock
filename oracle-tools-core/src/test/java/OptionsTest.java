/*
 * File: OptionsTest.java
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

import com.oracle.tools.Option;
import com.oracle.tools.Options;

import com.oracle.tools.options.Timeout;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.core.Is.is;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for {@link Options}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class OptionsTest
{
    /**
     * Ensure that we can get an {@link Option} that has a default.
     */
    @Test
    public void shouldGetOptionWithDefault()
    {
        Options options = new Options();

        assertThat(options.get(Timeout.class), is(Timeout.autoDetect()));
    }
    /**
     * Ensure that the default instance returned is the same value.
     */
    @Test
    public void shouldReturnSameDefaultInstance()
    {
        Options options = new Options();

        Timeout timeout = options.get(Timeout.class);

        assertThat(timeout, is(Timeout.autoDetect()));

        assertThat(options.get(Timeout.class), equalTo(timeout));
    }


    /**
     * Ensure that we can add and get a specific {@link Option}.
     */
    @Test
    public void shouldAddAndGetSpecificOption()
    {
        Timeout option = Timeout.after(5, TimeUnit.MINUTES);

        Options options = new Options(option);

        assertThat(options.get(Timeout.class), is(option));
    }


    /**
     * Ensure that we can add, get and then replace a specific {@link Option}.
     */
    @Test
    public void shouldReplaceAndGetSpecificOption()
    {
        Timeout option = Timeout.after(5, TimeUnit.MINUTES);

        Options options = new Options(option);

        Timeout otherOption = Timeout.after(1, TimeUnit.SECONDS);

        options.add(otherOption);

        assertThat(options.get(Timeout.class), is(otherOption));
    }


    /**
     * Ensure that we don't replace an existing specific {@link Option}.
     */
    @Test
    public void shouldNotReplaceAndGetSpecificOption()
    {
        Timeout option = Timeout.after(5, TimeUnit.MINUTES);

        Options options = new Options(option);

        Timeout otherOption = Timeout.after(1, TimeUnit.SECONDS);

        options.addIfAbsent(otherOption);

        assertThat(options.get(Timeout.class), is(option));
    }


    /**
     * Ensure that we can remove a specific {@link Option}.
     */
    @Test
    public void shouldRemoveASpecificOption()
    {
        Timeout option = Timeout.after(5, TimeUnit.MINUTES);

        Options options = new Options(option);

        assertThat(options.get(Timeout.class), is(option));

        assertThat(options.remove(Timeout.class), is(true));
        assertThat(options.remove(Timeout.class), is(false));

        // the timeout should now be the default
        assertThat(options.get(Timeout.class), is(Timeout.autoDetect()));
    }


    /**
     * Ensure that the class of an {@link Option} for an class
     * that directly implements the {@link Option} interface is
     * the class itself.
     */
    @Test
    public void shouldDetermineDirectlyImplementedOptionClass()
    {
        Timeout option = Timeout.after(5, TimeUnit.MINUTES);

        Class optionClass = Options.getClassOf(option);

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
        Enhanced option = new Enhanced();

        Class optionClass = Options.getClassOf(option);

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
        ExtendedEnhanced option = new ExtendedEnhanced();

        Class optionClass = Options.getClassOf(option);

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
        Class optionClass = Options.getClassOf(AbstractEnhanced.class);

        assertThat(optionClass, is(nullValue()));
    }


    /**
     * Ensure that we can create {@link Options} and request an option
     */
    @Test
    public void shouldCreateAndRequestAnOption()
    {
        Timeout timeout = Timeout.after(5, TimeUnit.MINUTES);

        Options options = Options.from(timeout);

        assertThat(options.get(Timeout.class), is(timeout));
    }


    /**
     * Ensure that {@link Options} maintain a set by concrete type.
     */
    @Test
    public void shouldMaintainASetByConcreteType()
    {
        Timeout fiveMinutes = Timeout.after(5, TimeUnit.MINUTES);
        Timeout oneSecond = Timeout.after(1, TimeUnit.SECONDS);

        Options options = Options.from(fiveMinutes, oneSecond);

        assertThat(options.get(Timeout.class), is(oneSecond));
    }


    /**
     * Ensure that {@link Options} can return a default using a
     * static method annotated with {@link Options.Default}.
     */
    @Test
    public void shouldDetermineDefaultUsingAnnotatedStaticMethod()
    {
        Options options = new Options();

        assertThat(options.get(Duration.class), is(Duration.getDefault()));
    }


    /**
     * Ensure that {@link Options} can return a default using a
     * static field annotated with {@link Options.Default}.
     */
    @Test
    public void shouldDetermineDefaultUsingAnnotatedStaticField()
    {
        Options options = new Options();

        assertThat(options.get(Device.class), is(Device.DEFAULT));
    }


    /**
     * Ensure that {@link Options} can return a default using an
     * enum annotated with {@link Options.Default}.
     */
    @Test
    public void shouldDetermineDefaultUsingAnnotatedEnum()
    {
        Options options = new Options();

        assertThat(options.get(Meal.class), is(Meal.CHICKEN));
    }


    /**
     * Ensure that {@link Options} can return a default using a
     * constructor annotated with {@link Options.Default}.
     */
    @Test
    public void shouldDetermineDefaultUsingAnnotatedConstructor()
    {
        Options options = new Options();

        assertThat(options.get(Beverage.class).toString(), is("Beer"));
    }


    /**
     * Ensure that {@link Options} can collect a single collectable,
     * including creating a collector.
     */
    @Test
    public void shouldCollectASingleCollectable()
    {
        Options options = new Options(new Message("Hello"));

        assertThat(options.get(Message.class), is(nullValue()));
        assertThat(options.get(Messages.class), is(not(nullValue())));
        assertThat(options.get(Messages.class).get(), is("Hello"));
    }


    /**
     * Ensure that {@link Options} can collect a multiple collectables,
     * including creating a collector.
     */
    @Test
    public void shouldCollectMultipleCollectables()
    {
        Options options = new Options(new Message("Hello"), new Message("G'day"));

        assertThat(options.get(Message.class), is(nullValue()));
        assertThat(options.get(Messages.class), is(not(nullValue())));
        assertThat(options.get(Messages.class).get(), is("Hello, G'day"));
    }


    /**
     * Ensure that {@link Options} can discard a single collectable.
     */
    @Test
    public void shouldDiscardASingleCollectable()
    {
        Options options = new Options(new Message("Hello"));

        assertThat(options.get(Message.class), is(nullValue()));
        assertThat(options.get(Messages.class), is(not(nullValue())));
        assertThat(options.get(Messages.class).get(), is("Hello"));

        options.remove(new Message("Hello"));

        assertThat(options.get(Message.class), is(nullValue()));
        assertThat(options.get(Messages.class), is(not(nullValue())));
        assertThat(options.get(Messages.class).get(), is(""));
    }


    /**
     * Ensure that {@link Options} can discard a multiple collectables
     */
    @Test
    public void shouldDiscardMultipleCollectables()
    {
        Options options = new Options(new Message("Hello"), new Message("G'day"));

        assertThat(options.get(Message.class), is(nullValue()));
        assertThat(options.get(Messages.class), is(not(nullValue())));
        assertThat(options.get(Messages.class).get(), is("Hello, G'day"));

        options.remove(new Message("G'day"));
        options.remove(new Message("Hello"));

        assertThat(options.get(Message.class), is(nullValue()));
        assertThat(options.get(Messages.class), is(not(nullValue())));
        assertThat(options.get(Messages.class).get(), is(""));
    }


    /**
     * Ensure that {@link Options} iterate over collectables.
     */
    @Test
    public void shouldIterateOverCollectables()
    {
        Options options = new Options(new Message("Hello"), new Message("G'day"));

        assertThat(options.get(Message.class), is(nullValue()));
        assertThat(options.get(Messages.class), is(not(nullValue())));
        assertThat(options.get(Messages.class).get(), is("Hello, G'day"));

        HashSet<Message> messages = new HashSet<>();
        for (Message message : options.getInstancesOf(Message.class))
        {
            messages.add(message);
        }

        assertThat(messages.size(), is(2));
        assertThat(messages.contains(new Message("Hello")), is(true));
        assertThat(messages.contains(new Message("G'day")), is(true));
    }


    /**
     * A simple {@link Option} using a {@link Options.Default}
     * annotation on a static getter method.
     */
    public enum Duration implements Option
    {
        SECOND,
        MINUTE,
        HOUR;


        @Options.Default
        public static Duration getDefault()
        {
            return SECOND;
        }
    }


    /**
     * A simple {@link Option} using a {@link Options.Default}
     * annotation on a static attribute.
     */
    public enum Device implements Option
    {
        CASSETTE,
        FLOPPY,
        TAPE,
        HARD_DRIVE,
        SOLID_STATE_DRIVE;

        @Options.Default
        public static Device DEFAULT = FLOPPY;
    }

    /**
     * A simple {@link Option} using a {@link Options.Default}
     * annotation on a enum.
     */
    public enum Meal implements Option
    {
        TOAST,
        SOUP,
        STEAK,

        @Options.Default
        CHICKEN,

        FISH
    }

    /**
     * A simple {@link Option} using a {@link Options.Default}
     * annotation on a public no-args constructor.
     */
    public static class Beverage implements Option
    {
        @Options.Default
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
     * A {@link Collector} for testing.
     */
    public static class Messages implements Option.Collector<Message, Messages>
    {
        private ArrayList<Message> messageList;


        @Options.Default
        public Messages()
        {
            this.messageList = new ArrayList<>();
        }


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
        public Iterator<Message> iterator()
        {
            return messageList.iterator();
        }
    }


    /**
     * A {@link Collectable} for testing.
     */
    public static class Message implements Option.Collectable
    {
        private String value;


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
            if (this == o) return true;
            if (!(o instanceof Message)) return false;

            Message message = (Message) o;

            return value != null ? value.equals(message.value) : message.value == null;

        }


        @Override
        public int hashCode()
        {
            return value != null ? value.hashCode() : 0;
        }
    }
}
