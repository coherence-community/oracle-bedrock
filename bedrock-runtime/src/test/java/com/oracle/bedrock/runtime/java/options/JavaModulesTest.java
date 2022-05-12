package com.oracle.bedrock.runtime.java.options;

import com.oracle.bedrock.OptionsByType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
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

    @Test
    public void shouldHaveDefaultModules()
    {
        List<String> args     = new ArrayList<>();
        JavaModules  modules  = JavaModules.automatic(args);
        JavaModules  expected = JavaModules.enabled();
        assertThat(modules, is(expected));
    }

    @Test
    public void shouldAddSinglePatchModules()
    {
        List<String> args     = List.of("--patch-module=foo=bar");
        JavaModules  modules  = JavaModules.automatic(args);
        JavaModules  expected = JavaModules.enabled().patching("foo=bar");
        assertThat(modules, is(expected));
    }

    @Test
    public void shouldAddMultiplePatchModules()
    {
        List<String> args     = List.of("--patch-module=one=two", "--patch-module", "three=four", "--patch-module", "five=six");
        JavaModules  modules  = JavaModules.automatic(args);
        JavaModules  expected = JavaModules.enabled().patching("one=two", "three=four", "five=six");
        assertThat(modules, is(expected));
    }

    @Test
    public void shouldAddMultiplePatchModulesWithMissingFinalArg()
    {
        List<String> args     = List.of("--patch-module=one=two", "--patch-module", "three=four", "--patch-module");
        JavaModules  modules  = JavaModules.automatic(args);
        JavaModules  expected = JavaModules.enabled().patching("one=two", "three=four");
        assertThat(modules, is(expected));
    }

    @Test
    public void shouldAddSingleAddModules()
    {
        List<String> args     = List.of("--add-modules=foo=bar");
        JavaModules  modules  = JavaModules.automatic(args);
        JavaModules  expected = JavaModules.enabled().adding("foo=bar");
        assertThat(modules, is(expected));
    }

    @Test
    public void shouldAddMultipleAddModules()
    {
        List<String> args     = List.of("--add-modules=one=two", "--add-modules", "three=four", "--add-modules", "five=six");
        JavaModules  modules  = JavaModules.automatic(args);
        JavaModules  expected = JavaModules.enabled().adding("one=two", "three=four", "five=six");
        assertThat(modules, is(expected));
    }

    @Test
    public void shouldAddMultipleAddModulesWithMissingFinalArg()
    {
        List<String> args     = List.of("--add-modules=one=two", "--add-modules", "three=four", "--add-modules");
        JavaModules  modules  = JavaModules.automatic(args);
        JavaModules  expected = JavaModules.enabled().adding("one=two", "three=four");
        assertThat(modules, is(expected));
    }

    @Test
    public void shouldAddSingleAddExports()
    {
        List<String> args     = List.of("--add-exports=foo=bar");
        JavaModules  modules  = JavaModules.automatic(args);
        JavaModules  expected = JavaModules.enabled().exporting("foo=bar");
        assertThat(modules, is(expected));
    }

    @Test
    public void shouldAddMultipleAddExports()
    {
        List<String> args     = List.of("--add-exports=one=two", "--add-exports", "three=four", "--add-exports", "five=six");
        JavaModules  modules  = JavaModules.automatic(args);
        JavaModules  expected = JavaModules.enabled().exporting("one=two", "three=four", "five=six");
        assertThat(modules, is(expected));
    }

    @Test
    public void shouldAddMultipleAddExportsWithMissingFinalArg()
    {
        List<String> args     = List.of("--add-exports=one=two", "--add-exports", "three=four", "--add-exports");
        JavaModules  modules  = JavaModules.automatic(args);
        JavaModules  expected = JavaModules.enabled().exporting("one=two", "three=four");
        assertThat(modules, is(expected));
    }

    @Test
    public void shouldAddSingleAddReads()
    {
        List<String> args     = List.of("--add-reads=foo=bar");
        JavaModules  modules  = JavaModules.automatic(args);
        JavaModules  expected = JavaModules.enabled().reading("foo=bar");
        assertThat(modules, is(expected));
    }

    @Test
    public void shouldAddMultipleAddReads()
    {
        List<String> args     = List.of("--add-reads=one=two", "--add-reads", "three=four", "--add-reads", "five=six");
        JavaModules  modules  = JavaModules.automatic(args);
        JavaModules  expected = JavaModules.enabled().reading("one=two", "three=four", "five=six");
        assertThat(modules, is(expected));
    }

    @Test
    public void shouldAddMultipleAddReadsWithMissingFinalArg()
    {
        List<String> args     = List.of("--add-reads=one=two", "--add-reads", "three=four", "--add-reads");
        JavaModules  modules  = JavaModules.automatic(args);
        JavaModules  expected = JavaModules.enabled().reading("one=two", "three=four");
        assertThat(modules, is(expected));
    }

    @Test
    public void shouldAddSingleAddOpens()
    {
        List<String> args     = List.of("--add-opens=one/two=three");
        JavaModules  modules  = JavaModules.automatic(args);
        JavaModules  expected = JavaModules.enabled().opens("one", "two", "three");
        assertThat(modules, is(expected));
    }

    @Test
    public void shouldAddMultipleAddOpens()
    {
        List<String> args     = List.of("--add-opens=one/two=three", "--add-opens", "four/five=six", "--add-opens", "seven/eight=nine");
        JavaModules  modules  = JavaModules.automatic(args);
        JavaModules  expected = JavaModules.enabled()
                .opens("one", "two", "three")
                .opens("four", "five", "six")
                .opens("seven", "eight", "nine");
        assertThat(modules, is(expected));
    }

    @Test
    public void shouldAddMultipleAddOpensWithMissingFinalArg()
    {
        List<String> args     = List.of("--add-opens=one/two=three", "--add-opens", "four/five=six", "--add-opens");
        JavaModules  modules  = JavaModules.automatic(args);
        JavaModules  expected = JavaModules.enabled()
                .opens("one", "two", "three")
                .opens("four", "five", "six");
        assertThat(modules, is(expected));
    }
}
