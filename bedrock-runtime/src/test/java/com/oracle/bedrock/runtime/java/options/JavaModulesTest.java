package com.oracle.bedrock.runtime.java.options;

import com.oracle.bedrock.OptionsByType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

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
    public void shouldExcludeModulesAndOpens()
    {
        JavaModules modules = JavaModules.enabled();
        modules = modules.adding("mod1");
        modules = modules.opens("mod1", "com.mod1", "foo");
        modules = modules.adding("mod2");
        modules = modules.opens("mod2", "com.mod2", "foo");
        modules = modules.excluding("mod2");
        modules = modules.adding("mod3");
        modules = modules.adding("mod4");
        modules = modules.opens("mod4", "com.mod2", "mod2");

        Iterable<String> iterable = modules.resolve(OptionsByType.empty());

        assertThat(iterable, contains("--add-modules", "mod1,mod3,mod4", "--add-opens", "mod1/com.mod1=foo"));
    }

    @Test
    public void shouldExcludeModulesAndPatches()
    {
        JavaModules modules = JavaModules.enabled();
        modules = modules.adding("mod1");
        modules = modules.patching("mod1=foo.jar");
        modules = modules.adding("mod2");
        modules = modules.patching("mod2=bar.jar");
        modules = modules.excluding("mod2");
        modules = modules.adding("mod3");

        Iterable<String> iterable = modules.resolve(OptionsByType.empty());

        assertThat(iterable, containsInAnyOrder("--add-modules", "mod1,mod3", "--patch-module", "mod1=foo.jar"));
    }

    @Test
    public void shouldExcludeModulesAndReads()
    {
        JavaModules modules = JavaModules.enabled();
        modules = modules.adding("mod1");
        modules = modules.adding("mod2");
        modules = modules.excluding("mod2");
        modules = modules.adding("mod3");
        modules = modules.adding("mod4");
        modules = modules.reading("mod1=mod3,mod2,mod4", "mod2=mod1,mod3", "mod3=mod1,mod4", "mod4=mod2");


        Iterable<String> iterable = modules.resolve(OptionsByType.empty());

        assertThat(iterable, containsInAnyOrder("--add-modules", "mod1,mod3,mod4",
                                                "--add-reads", "mod1=mod3,mod4",
                                                "--add-reads", "mod3=mod1,mod4"));
    }

    @Test
    public void shouldExcludeModulesAndExports()
    {
        JavaModules modules = JavaModules.enabled();
        modules = modules.adding("mod1");
        modules = modules.adding("mod2");
        modules = modules.excluding("mod2");
        modules = modules.adding("mod3");
        modules = modules.adding("mod4");
        modules = modules.exporting("mod1/com.mod1=mod3,mod2,mod4", "mod2/com.mod2=mod1,mod3", "mod3/com.mod3=mod1,mod4", "mod4/com.mod4=mod2");


        Iterable<String> iterable = modules.resolve(OptionsByType.empty());

        assertThat(iterable, containsInAnyOrder("--add-modules", "mod1,mod3,mod4",
                                                "--add-exports", "mod1/com.mod1=mod3,mod4",
                                                "--add-exports", "mod3/com.mod3=mod1,mod4"));
    }

    @Test
    public void shouldHaveDefaultModules()
    {
        List<String> args     = new ArrayList<>();
        JavaModules  modules  = JavaModules.automatic(true, args);
        JavaModules  expected = JavaModules.enabled();
        assertThat(modules, is(expected));
    }

    @Test
    public void shouldAddSinglePatchModules()
    {
        List<String> args     = List.of("--patch-module=foo=bar");
        JavaModules  modules  = JavaModules.automatic(true, args);
        JavaModules  expected = JavaModules.enabled().patching("foo=bar");
        assertThat(modules, is(expected));
    }

    @Test
    public void shouldAddMultiplePatchModules()
    {
        List<String> args     = List.of("--patch-module=one=two", "--patch-module", "three=four", "--patch-module", "five=six");
        JavaModules  modules  = JavaModules.automatic(true, args);
        JavaModules  expected = JavaModules.enabled().patching("one=two", "three=four", "five=six");
        assertThat(modules, is(expected));
    }

    @Test
    public void shouldAddMultiplePatchModulesWithMissingFinalArg()
    {
        List<String> args     = List.of("--patch-module=one=two", "--patch-module", "three=four", "--patch-module");
        JavaModules  modules  = JavaModules.automatic(true, args);
        JavaModules  expected = JavaModules.enabled().patching("one=two", "three=four");
        assertThat(modules, is(expected));
    }

    @Test
    public void shouldAddSingleAddModules()
    {
        List<String> args     = List.of("--add-modules=foo=bar");
        JavaModules  modules  = JavaModules.automatic(true, args);
        JavaModules  expected = JavaModules.enabled().adding("foo=bar");
        assertThat(modules, is(expected));
    }

    @Test
    public void shouldAddMultipleAddModules()
    {
        List<String> args     = List.of("--add-modules=one=two", "--add-modules", "three=four", "--add-modules", "five=six");
        JavaModules  modules  = JavaModules.automatic(true, args);
        JavaModules  expected = JavaModules.enabled().adding("one=two", "three=four", "five=six");
        assertThat(modules, is(expected));
    }

    @Test
    public void shouldAddMultipleAddModulesWithMissingFinalArg()
    {
        List<String> args     = List.of("--add-modules=one=two", "--add-modules", "three=four", "--add-modules");
        JavaModules  modules  = JavaModules.automatic(true, args);
        JavaModules  expected = JavaModules.enabled().adding("one=two", "three=four");
        assertThat(modules, is(expected));
    }

    @Test
    public void shouldAddSingleAddExports()
    {
        List<String> args     = List.of("--add-exports=foo=bar");
        JavaModules  modules  = JavaModules.automatic(true, args);
        JavaModules  expected = JavaModules.enabled().exporting("foo=bar");
        assertThat(modules, is(expected));
    }

    @Test
    public void shouldAddMultipleAddExports()
    {
        List<String> args     = List.of("--add-exports=one=two", "--add-exports", "three=four", "--add-exports", "five=six");
        JavaModules  modules  = JavaModules.automatic(true, args);
        JavaModules  expected = JavaModules.enabled().exporting("one=two", "three=four", "five=six");
        assertThat(modules, is(expected));
    }

    @Test
    public void shouldAddMultipleAddExportsWithMissingFinalArg()
    {
        List<String> args     = List.of("--add-exports=one=two", "--add-exports", "three=four", "--add-exports");
        JavaModules  modules  = JavaModules.automatic(true, args);
        JavaModules  expected = JavaModules.enabled().exporting("one=two", "three=four");
        assertThat(modules, is(expected));
    }

    @Test
    public void shouldAddSingleAddReads()
    {
        List<String> args     = List.of("--add-reads=foo=bar");
        JavaModules  modules  = JavaModules.automatic(true, args);
        JavaModules  expected = JavaModules.enabled().reading("foo=bar");
        assertThat(modules, is(expected));
    }

    @Test
    public void shouldAddMultipleAddReads()
    {
        List<String> args     = List.of("--add-reads=one=two", "--add-reads", "three=four", "--add-reads", "five=six");
        JavaModules  modules  = JavaModules.automatic(true, args);
        JavaModules  expected = JavaModules.enabled().reading("one=two", "three=four", "five=six");
        assertThat(modules, is(expected));
    }

    @Test
    public void shouldAddMultipleAddReadsWithMissingFinalArg()
    {
        List<String> args     = List.of("--add-reads=one=two", "--add-reads", "three=four", "--add-reads");
        JavaModules  modules  = JavaModules.automatic(true, args);
        JavaModules  expected = JavaModules.enabled().reading("one=two", "three=four");
        assertThat(modules, is(expected));
    }

    @Test
    public void shouldAddSingleAddOpens()
    {
        List<String> args     = List.of("--add-opens=one/two=three");
        JavaModules  modules  = JavaModules.automatic(true, args);
        JavaModules  expected = JavaModules.enabled().opens("one", "two", "three");
        assertThat(modules, is(expected));
    }

    @Test
    public void shouldAddMultipleAddOpens()
    {
        List<String> args     = List.of("--add-opens=one/two=three", "--add-opens", "four/five=six", "--add-opens", "seven/eight=nine");
        JavaModules  modules  = JavaModules.automatic(true, args);
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
        JavaModules  modules  = JavaModules.automatic(true, args);
        JavaModules  expected = JavaModules.enabled()
                .opens("one", "two", "three")
                .opens("four", "five", "six");
        assertThat(modules, is(expected));
    }
}
