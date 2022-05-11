package com.oracle.bedrock.runtime.java.options;

import com.oracle.bedrock.OptionsByType;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

public class JavaModulesTest
{
    @Test
    public void shouldAddModules()
    {
        JavaModules modules = JavaModules.enabled();
        modules = modules.adding("foo");
        modules = modules.adding("bar");

        Iterable<String> iterable = modules.resolve(OptionsByType.empty());

        assertThat(iterable, containsInAnyOrder("--add-modules", "foo,bar"));
    }

    @Test
    public void shouldExcludeModules()
    {
        JavaModules modules = JavaModules.enabled();
        modules = modules.adding("mod1");
        modules = modules.adding("mod2");
        modules = modules.excluding("mod2");
        modules = modules.adding("mod3");

        Iterable<String> iterable = modules.resolve(OptionsByType.empty());

        assertThat(iterable, containsInAnyOrder("--add-modules", "mod1,mod3"));
    }

}
