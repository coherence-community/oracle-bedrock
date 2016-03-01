package com.oracle.tools.runtime;

import com.oracle.tools.runtime.options.Argument;
import com.oracle.tools.runtime.options.Arguments;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link AbstractApplicationSchema}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class AbstractApplicationSchemaTest
{
    @Test
    public void shouldAddStringArgument() throws Exception
    {
        AbstractApplicationSchema schema = new AbstractApplicationSchemaStub();

        schema.addArgument("foo");

        assertArguments(schema, "foo");
    }


    @Test
    public void shouldAddMultipleStringArguments() throws Exception
    {
        AbstractApplicationSchema schema = new AbstractApplicationSchemaStub();

        schema.addArgument("foo");
        schema.addArgument("bar");

        assertArguments(schema, "foo", "bar");
    }


    @Test
    public void shouldAddListOfStringArguments() throws Exception
    {
        AbstractApplicationSchema schema = new AbstractApplicationSchemaStub();

        schema.addArguments(Arrays.asList("foo1", "foo2"));

        assertArguments(schema, "foo1", "foo2");
    }


    @Test
    public void shouldAddMultipleListOfStringArguments() throws Exception
    {
        AbstractApplicationSchema schema = new AbstractApplicationSchemaStub();

        schema.addArguments(Arrays.asList("foo1", "foo2"));
        schema.addArguments(Arrays.asList("bar1", "bar2"));

        assertArguments(schema, "foo1", "foo2", "bar1", "bar2");
    }


    @Test
    public void shouldAddVarArgsStringArguments() throws Exception
    {
        AbstractApplicationSchema schema = new AbstractApplicationSchemaStub();

        schema.addArguments("foo1", "foo2");

        assertArguments(schema, "foo1", "foo2");
    }


    @Test
    public void shouldAddMultipleVarArgsStringArguments() throws Exception
    {
        AbstractApplicationSchema schema = new AbstractApplicationSchemaStub();

        schema.addArguments("foo1", "foo2");
        schema.addArguments("bar1", "bar2");

        assertArguments(schema, "foo1", "foo2", "bar1", "bar2");
    }


    @Test
    public void shouldAddArgument() throws Exception
    {
        AbstractApplicationSchema schema = new AbstractApplicationSchemaStub();

        schema.addArgument(Argument.of("foo"));

        assertArguments(schema, "foo");
    }


    @Test
    public void shouldAddMultipleArguments() throws Exception
    {
        AbstractApplicationSchema schema = new AbstractApplicationSchemaStub();

        schema.addArgument(Argument.of("foo"));
        schema.addArgument(Argument.of("bar"));

        assertArguments(schema, "foo", "bar");
    }


    @Test
    public void shouldAddListOfArguments() throws Exception
    {
        AbstractApplicationSchema schema = new AbstractApplicationSchemaStub();

        schema.addArguments(Arrays.asList(Argument.of("foo1"), Argument.of("foo2")));

        assertArguments(schema, "foo1", "foo2");
    }


    @Test
    public void shouldAddMultipleListOfArguments() throws Exception
    {
        AbstractApplicationSchema schema = new AbstractApplicationSchemaStub();

        schema.addArguments(Arrays.asList(Argument.of("foo1"), Argument.of("foo2")));
        schema.addArguments(Arrays.asList(Argument.of("bar1"), Argument.of("bar2")));

        assertArguments(schema, "foo1", "foo2", "bar1", "bar2");
    }


    @Test
    public void shouldAddVarArgsArguments() throws Exception
    {
        AbstractApplicationSchema schema = new AbstractApplicationSchemaStub();

        schema.addArguments(Argument.of("foo1"), Argument.of("foo2"));

        assertArguments(schema, "foo1", "foo2");
    }


    @Test
    public void shouldAddMultipleVarArgsArguments() throws Exception
    {
        AbstractApplicationSchema schema = new AbstractApplicationSchemaStub();

        schema.addArguments(Argument.of("foo1"), Argument.of("foo2"));
        schema.addArguments(Argument.of("bar1"), Argument.of("bar2"));

        assertArguments(schema, "foo1", "foo2", "bar1", "bar2");
    }

    public void assertArguments(AbstractApplicationSchema schema, String... args)
    {
        Arguments arguments = schema.getOptions().get(Arguments.class);

        assertThat(arguments, is(notNullValue()));

        List<String> argList = arguments.realize(LocalPlatform.getInstance(), schema);

        assertThat(argList, is(Arrays.asList(args)));
    }


    /**
     * A stub of {@link AbstractApplicationSchema} to use for testing.
     */
    public static class AbstractApplicationSchemaStub extends AbstractApplicationSchema
    {
        public AbstractApplicationSchemaStub()
        {
            super("dummy");
        }

        @Override
        public Class getApplicationClass()
        {
            return null;
        }
    }
}
