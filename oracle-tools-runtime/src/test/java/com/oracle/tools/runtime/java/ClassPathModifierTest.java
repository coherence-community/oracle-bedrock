package com.oracle.tools.runtime.java;

import com.oracle.tools.Options;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

/**
 * @author jk 2015.01.15
 */
public class ClassPathModifierTest
{
    @Test
    public void shouldApplyNoModification() throws Exception
    {
    ClassPathModifier modifier = ClassPathModifier.none();

    assertThat(modifier.modify("foo"), is("foo"));
    }

    @Test
    public void shouldApplyCygwinModification() throws Exception
    {
    ClassPathModifier modifier = ClassPathModifier.forCygwin();

    assertThat(modifier.modify("foo"), is("$(cygpath -wp foo)"));
    }

    @Test
    public void shouldFindNoneOption() throws Exception
    {
        ClassPathModifier none     = ClassPathModifier.none();
        Options           options  = new Options(none);
        ClassPathModifier modifier = options.get(ClassPathModifier.class);

        assertThat(modifier, is(sameInstance(none)));
    }

    @Test
    public void shouldFindCygwinOption() throws Exception
    {
        ClassPathModifier cygwin   = ClassPathModifier.forCygwin();
        Options           options  = new Options(cygwin);
        ClassPathModifier modifier = options.get(ClassPathModifier.class);

        assertThat(modifier, is(sameInstance(cygwin)));
    }
}
